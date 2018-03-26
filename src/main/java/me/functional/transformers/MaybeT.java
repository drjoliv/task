package me.functional.transformers;

import java.util.function.Function;

import me.functional.data.Maybe;
import me.functional.functions.F2;
import me.functional.hkt.Hkt;
import me.functional.hkt.Hkt2;
import me.functional.hkt.Witness;
import me.functional.type.Monad;
import me.functional.type.MonadUnit;

public class MaybeT<M extends Witness,A> implements Monad<Hkt<MaybeT.μ,M>,A>, Hkt2<MaybeT.μ,M,A> {

  public static class μ implements Witness{}

  private final Monad<M,Maybe<A>> runMaybeT;
  private final MonadUnit<M> mUnit;

  @Override
  public <B> MaybeT<M,B> fmap(Function<? super A, B> fn){
    return maybeT(runMaybeT.fmap(maybe_value -> maybe_value.fmap(fn)));
  }

  @Override
  public <B> MaybeT<M,B> semi(Monad<Hkt<μ, M>, B> mb) {
    return mBind(a -> mb);
  }

  @Override
  public <B> MaybeT<M,B> mBind(Function<? super A, ? extends Monad<Hkt<μ, M>, B>> fn) {
    return maybeT(runMaybeT.mBind(maybe_value -> {
     if(maybe_value.isSome()) {
       return asMaybeT(fn.apply(maybe_value.value())).runMaybeT;
     }
     else
       return mUnit.unit(Maybe.nothing());
   }));
  }

  @Override
  public MonadUnit<Hkt<μ, M>> yield() {
    return new MonadUnit<Hkt<μ, M>>() {
      @Override
      public <B> Monad<Hkt<μ, M>, B> unit(B b) {
        return MaybeT.<M,B>maybeT().call(mUnit,b);
      }
    };
  }


  private MaybeT(Monad<M,Maybe<A>> runMaybeT) {
    this.runMaybeT = runMaybeT;  
    this.mUnit = runMaybeT.yield();
  }

  public static <M extends Witness,A> MaybeT<M,A> liftMaybeT(Monad<M,A> m) {
    return new MaybeT<M,A>(m.fmap(a -> Maybe.maybe(a)));
  }

  public static <M extends Witness, A> F2<MonadUnit<M>,A,MaybeT<M,A>> maybeT() {
    return (mUnit, a) -> new MaybeT<M, A>(mUnit.unit(Maybe.maybe(a)));
  }

  public static <M extends Witness,A> MaybeT<M,A> maybeT(Monad<M,Maybe<A>> runMaybeT) {
    return new MaybeT<M,A>(runMaybeT);
  }

  public static <M extends Witness,A> MaybeT<M,A> maybeT(A a, MonadUnit<M> mUnit) {
    return liftMaybeT(mUnit.unit(a));
  }

  public Monad<M,Maybe<A>> runMaybeT() {
    return runMaybeT;
  }

  public static <M extends Witness, A> MaybeT<M, A> asMaybeT(Monad<Hkt<MaybeT.μ, M>, A> wider) {
    return (MaybeT<M, A>) wider;
  }
}
