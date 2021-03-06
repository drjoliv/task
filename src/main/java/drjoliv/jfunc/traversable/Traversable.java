package drjoliv.jfunc.traversable;

import drjoliv.jfunc.applicative.Applicative;
import drjoliv.jfunc.applicative.ApplicativeFactory;
import drjoliv.jfunc.foldable.Foldable;
import drjoliv.jfunc.function.F1;
import drjoliv.jfunc.monad.Monad;
import drjoliv.jfunc.monad.MonadFactory;

/**
 * A structure that can be traversed from left to right.
 * @author Desonte 'drjoliv' Jolivet : drjoliv@gmail.com
 */
public interface Traversable<M, A> extends Foldable<M,A> {

  /**
   * Maps each element of this traversable to an applicative, the applicatives are evaluteded from left to right to collect the result.
   * @param fn a function from {@code A} to {@code Applicative<N,B>}.
   * @param pure a strategy for creating applicatives.
   * @return an applicative cotaining a traversable.
   */
  public  <N, F, B> Applicative<N,? extends Traversable<M,B>> mapA(F1<A, ? extends Applicative<N,B>> fn, ApplicativeFactory<N> pure);

  /**
   * Maps each element of this traverable to a monad, the monads are then evaluated from left to rigt to collect the result.
   *
   * @param fn a function from {@code A} to {@code Monad<N,B>}.
   * @param ret a strategy for creating monads.
   * @return a monad containing a monad.
   */
  public default <N,B> Monad<N,? extends Traversable<M,B>> mapM(F1<A, ? extends Monad<N,B>> fn, MonadFactory<N> ret) {
    return (Monad<N,Traversable<M,B>>)mapA(fn, new ApplicativeFactory<N>(){
      @Override
      public <W> Applicative<N, W> pure(W a) {
        return ret.unit(a);
      }
    });
  }

  /**
   * Evaluates the applicatives within the given traversable from left to right returning a applicative containing a traverable.
   * @param tma a traverable containing applicatives.
   * @param pure a strategy to for lifting values into a applicative.
   * @return an applicative cotaining a traversable.
   */
  public static <N, M, A, B> Applicative<N, Traversable<M,B>> sequenceA(Traversable<M, ? extends Applicative<N,B>> tma, ApplicativeFactory<N> pure) {
    return (Applicative<N, Traversable<M,B>>)tma.mapA(F1.identity(), pure); 
  }

  /**
   *
   * Evaluates the monads within the given traversable from left to right returning a monad containing a traverable.
   * @param tma a traverable containing monads.
   * @param ret a stregty for lifting values into a monad.
   * @return a monad containing a monad.
   */
  public static <N, M, A, B> Monad<N, Traversable<M,B>> sequence(Traversable<M,? extends Monad<N,B>> tma, MonadFactory<N> ret) {
    return (Monad<N, Traversable<M,B>>)tma.mapM(F1.identity(), ret);
  }
}
