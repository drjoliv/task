package me.functional.data;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.Predicate;

import me.functional.QuadFunction;
import me.functional.TriFunction;
import me.functional.hkt.Hkt;

/**
 * 
 *
 * @author drjoliv@gmail.com
 */
public abstract class FList<A> implements Hkt<FList.μ,A> {

  public static class μ{}

  private FList(){}

  /**
   * Appends a value to this FList.
   *
   * @param a An element to append to this FLiist.
   * @return A new FList with the given value appended to it.
   */
  public final FList<A> concat(A a) {
    return FList.functions.concat(this, () -> of(a));
  }

  /**
   * Appends the given FList to this FList.
   *
   * @param a The FList that will be appended to this FList.
   * @return A new FList with the given FList appended to it.
   */
  public final FList<A> concat(FList<A> a) {
    return FList.functions.concat(this, () -> a);  
  }

  /**
   * Creates a new FList in which the given element is the head of the new FList.
   * <pre>
   * {@code
   * FList<Integer> list = of(2,3);
   * print(list.addd(1));
   * //prints
   * // [1, 2, 3]
   * }
   * </pre>

   * @param a The element that will be prepend to the head of this list.
   * @return a new FList in which the given element is at the head.
   */
  public final FList<A> add(A a) {
    return of(a, () -> this);
  }

  /**
   * Returns the element at the given index. This method is unsafe and will return null if
   * the given index is not within this FList.
   *
   * @param index The index of the element to retrieve.
   * @return The element at the given index.
   */
  public A unsafeGet(int index) {
    if(index == 0)
      return unsafeHead();
    else if(isEmpty())
      return null;
    else {
      FList<A> list = this;
      for(int j = index; j > 0; j--)
        list = list.tail(); 
      return list.unsafeHead();
    }
  }

  public FList<A> drop(int i) {
    if(i == 0)
      return this;
    else if(isEmpty())
      return this;
    else {
      FList<A> list = this;
      for(int j = i; j > 0; j--)
        list = list.tail(); 
      return list;
    }
  }

  /**
   * Returns the last value within this FList. This is unsafe because it may
   * return null if this list is empty.
   *<pre>
   * {@code
   * Integer a = of(2,3).unsafeLast();
   * system.out.println(a);
   * //prints
   * // 3
   *
   * Integer b = Flist.empty().unsafeLast();
   * System.out.println(b);
   * //prints
   * // null
   * }
   * </pre>
   * @return The last value within this FList. 
   */
  public A unsafeLast() {
    if(isEmpty())
      return null;
    else {
      FList<A> list = this;
      FList<A> tail = list.tail();
      while(!tail.isEmpty()) {
        list = list.tail();
        tail = list.tail();
      }
      return list.unsafeHead();
    }
  }

  /**
   * Obtains the element of this FList at the given index.
   *
   * @param index The index of this FList to return.
   * @return The element
   */
  public final Maybe<A> get(int index) {
    if(index == 0)
      return head();
    else if(isEmpty())
      return Maybe.of(null);
    else {
      FList<A> list = this;
      for(int j = index; j > 0; j--)
        list = list.tail(); 
      return list.head();
    }
  }

  /**
   * Return a new FList with the given FList appended to it.
   *
   * @param a The FList that will be appended the end of this FList.
   * @return A new FList with the given FList appended to it.
   */
  public final FList<A> add(FList<A> a) {
    return FList.functions.concat(a, () -> this);  
  }

  /**
   * Returns a new FList containing the elements obtained by mapping the given function over this FList.
   *
   * @param fn The function that will be applied to every element within this FList.
   * @return A new FList containing the elements obtained by mapping the given function over this FList.
   */
  public final <B> FList<B> map(Function<A,B> fn) {
     if(isEmpty()) {
      return Nil.instance();
    } else {
      return of(fn.apply(unsafeHead()), () -> tail().map(fn));
    }
  }

  /**
   * Return the Higher Kinded Type of FList
   *
   * @return The Higher Kinded version of this FList.
   */
  public final Hkt<FList.μ, A> widen() {
    return (Hkt<FList.μ, A>) this;
  }

  /**
   *
   *
   * @param fn
   * @return
   */
  public final <B> FList<B> bind(Function<A,FList<B>> fn) {
    if(isEmpty()) {
      return Nil.instance();
    } else {
      return FList.functions.flatten(map(fn));
    }
  }

  /**
   *
   *
   * @param consumer
   */
  public final void forEach(Consumer<A> consumer) {
    for(A a : FList.functions.iterable(this))
      consumer.accept(a); 
  }

  /**
   * Returns true if this FList has zero elements, or false otherwise.
   *
   * @return True if this FList has zero elements, or false otherwise.
   */
  public abstract boolean isEmpty();

  @Override
  public final String toString() {
    return FList.functions.toString(this);
  }

  /**
   * Removes all elements within the FList that evaluate to true when applied to the predicate.
   * <pre>
   * {@code
   *  FList<Integer> oneToTweenty = of(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20);
   *  String s = oneToTweenty.filter(i -> i % 2 == 0)
   *    .toString(); 
   *
   *  print(s); // [1, 3, 5, 7, 9, 11, 13, 15, 17, 19 ]
   *
   * // Finding sum of all natural number below 1000 that are divisible by 3 or 5.
   *
   *  FList<Integer> list = range(1,999)
   *    .filter(i -> i % 3 == 0 || i % 5 == 0);
   *
   *  BiFunction<Integer,Integer,Integer> sum = (i, i2) -> i + i2;
   *
   *  System.out.println(list.reduce(0,sum)); //
   * }
   * </pre>
   * @param predicate
   * @return a new FList in which elements have been filtered out.
   */
  public final FList<A> filter(Predicate<A> p) {
     if(isEmpty())
      return Nil.instance();
    else if(p.test(unsafeHead()))
      return of(unsafeHead(), () -> tail().filter(p));
    else
      return FList.functions.concat(Nil.instance(), () -> tail().filter(p));
  }

  /**
   * Returns a new FList containing the elements of this FList up to element at the ith index.
   *
   * @param i The index up to where the elements will be taken.
   * @return A new FList containing the elements of this FList up to element at the ith index.
   */
  public final FList<A> take(int i) {
    if(isEmpty() || i == 0) {
      return Nil.instance();
    } else {
      return of(unsafeHead(), () -> tail().take(i-1));
    }
  }

  /**
   *
   *
   * @param a
   * @param reducer
   * @return A value 
   */
  public final A reduce(final A a, final BiFunction<A,A,A> reducer) {
    A reducedValue = a;
    Iterator<A> it = FList.functions.iterable(this).iterator();
    while(it.hasNext()) {
      reducedValue = reducer.apply(a,it.next());
    }
    return reducedValue;
  }

  /**
   * Returns the number of elements within this FList.
   *
   * @return The number of elements within this FList.
   */
  public abstract int size();

  /**
   * Returns a Flist with the head of this FList dropped.
   *
   * @return A FList with the head of this FList dropped.
   */
  public abstract FList<A> tail();

  /**
   * Returns the element at the head of this FList as a Maybe.
   * This function should be used instae of unsafeHead, because it ensures that
   * a null pointer exception is not possible.
   *
   * @return Returns the element at the head of this FList as a Maybe.
   */
  public abstract Maybe<A> head();

  /**
   * Returns the element at the head of this FList. This function is not safe
   * because it will return null if this FList is empty.
   *
   * @return The element at the head of this FList.
   */
  public abstract A unsafeHead();

  private static class Cons<A> extends FList<A> {

    private final A datum; //The data withing this FList

    /*
     * A reference to the next FList or null if the Cons#tail() has not been called.
     */
    private volatile FList<A> tail;

    //A supplier that return the next FList in this LinkedList like structure
    private final Supplier<FList<A>> tailSupplier;

    private Cons(A datum, Supplier<FList<A>> tailSupplier) {
      Objects.requireNonNull(datum);
      Objects.requireNonNull(tailSupplier);
      this.datum = datum;
      this.tailSupplier = tailSupplier;
    }

    @Override
    public FList<A> tail() {
      if(isTailNull()) {
        synchronized(this) {
          if(isTailNull()){
              tail = tailSupplier.get();
          }
        }
        return tail;
      }
      else{
        return tail;
      }
    }

    private  Boolean isTailNull() {
      return tail == null;
    }

    @Override
    public Maybe<A> head() {
      return Maybe.of(datum);
    }

    @Override
    public int size() {
      return 1 + tail.size();
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public A unsafeHead() {
      return datum;
    }
  }

  private static class Nil<A> extends FList<A> {

    private static Nil<?> instance = new Nil();

    private Nil(){}

    @SuppressWarnings("unchecked")
    public static <B> Nil<B> instance() {
      return (Nil<B>)instance;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public FList<A> tail() {
      return instance();
    }

    @Override
    public Maybe<A> head() {
      return Maybe.of(null);
    }

    @Override
    public boolean equals(Object o) {
      if(! (o instanceof Nil)) {
        return false;
      }
      else {
        return true;
      }
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public A unsafeHead() {
      return null;
    }
  }

  /**
   * Creates an empty FList.
   *
   * @return An empty FList.
   */
  public static <A> FList<A> empty() {
    return Nil.instance();
  }



  /**
   * Creats a new FList from an array of elements.
   *
   * @param elements The elements that will be turned into a FList.
   * @return A new FList.
   */
  @SafeVarargs
  public static <B> FList<B> of(B... elements) {
    FList<B> list = Nil.instance();
    for (int i = elements.length - 1; i >= 0; i--) {
      list = list.add(elements[i]);
    }
    return list;
  }

  /**
   * Creates a new FList.
   *
   * @param b The element to placed at the head of this FList.
   * @param supplier A supplier that is used to generated the tail of this FList.
   * @return A new FList.
   */
  public static <B> FList<B> of(B b, Supplier<FList<B>> supplier) {
    return new Cons<B>(b, supplier);
  }

  /**
   * Creates a new FList.
   *
   * @param b The element placed at the head of the created FList.
   * @param b1 The second element placed within the created FList.
   * @param supplier A supplier that is used to generate the tail of this FList.
   * @return A new FList.
   */
  public static <B> FList<B> of(B b, B b1, Supplier<FList<B>> supplier) {
    return of(b, () -> of(b1, supplier));
  }

  /**
   *
   *
   * @param b
   * @param b1
   * @param b2
   * @param supplier
   * @return
   */
  public static <B> FList<B> of(B b, B b1, B b2, Supplier<FList<B>> supplier) {
    return of(b, () -> of(b1, b2, supplier));
  }

  /**
   *
   *
   * @param b
   * @param b1
   * @param b2
   * @param b3
   * @param supplier
   * @return
   */
  public static <B> FList<B> of(B b, B b1, B b2, B b3, Supplier<FList<B>> supplier) {
    return of(b, () -> of(b1, b2, b3, supplier));
  }


  /**
  * A static class containing functions that can be used on FLists.
  */
  public static class functions {

    /**
     *
     *
     * @param flist
     * @return
     */
  public static <A> String toString(FList<A> flist) {
    System.err.println("creating string");
    StringBuilder builder = new StringBuilder(); 
    builder.append("[ ");
    Consumer<A> stringfy = a -> {
      builder.append(a.toString()).append(" ");
    };
    Iterator<A> it = FList.functions.iterable(flist).iterator();
    while(it.hasNext())
      stringfy.accept(it.next());
    builder.append("]");
    return builder.toString();
  }

    /**
     *
     *
     * @param flist
     * @return
     */
      public static final <A> Iterable<A> iterable(final FList<A> flist){

      return new Iterable<A>() {
        @Override
        public Iterator<A> iterator() {
          return new Iterator<A>() {
            { this.list = flist; }

            private FList<A> list;

            @Override
            public boolean hasNext() {
              return !list.isEmpty();
            }

            @Override
            public A next() {
              final A a = list.unsafeHead();
              list = list.tail();
              return a;
            }
          };
        }
      };
    }

  /**
   *
   *
   * @param listOfList
   * @return
   */
  public static final <A> FList<A> flatten(FList<FList<A>> listOfList) {
    if(listOfList.isEmpty())
      return Nil.instance();
    else
      return concat(listOfList.unsafeHead(), () -> flatten(listOfList.tail()));
  }

    /**
     * Concats two of the given FLists together.
     *
     * @param l1 The FList that will be used as the beginning of the created FList.
     * @param l2 A supplier that generated the end of the created FList.
     * @return A new FList with l1 as the beginning of theo FList and l2 as the end of the FList.
     */
    public static final <A> FList<A> concat(final FList<A> l1, Supplier<FList<A>> l2) {
    if(l1.isEmpty()) {
      return l2.get();
    } else {
      return of(l1.unsafeHead(), () -> concat(l1.tail(), l2));
    }
  }

  /**
   *
   *
   * @param list
   * @param fn
   * @return
   */
  public static <A,B> FList<B> For(FList<A> list, Function<A, FList<B>> fn) {
    return list.bind(fn);
  } 

    /**
     *
     *
     * @param list
     * @param fn
     * @param fn2
     * @return
     */
    public static <A,B,C> FList<C> For(FList<A> list, Function<A, FList<B>> fn, BiFunction<A, B,FList<C>> fn2) {
    return list.bind(a -> {
      return fn.apply(a).bind(b -> {
        return fn2.apply(a,b); 
      });
    });
  } 

    /**
     *
     *
     * @param list
     * @param fn
     * @param fn2
     * @param fn3
     * @return
     */
    public static <A,B,C,D> FList<D> For(FList<A> list, Function<A, FList<B>> fn, BiFunction<A, B,FList<C>> fn2,
      TriFunction<A,B,C,FList<D>> fn3) {
    return list.bind(a -> {
      return fn.apply(a).bind(b -> {
        return fn2.apply(a,b).bind( c -> {
         return fn3.apply(a,b,c); 
        }); 
      });
    });
  }

    /**
     *
     *
     * @param list
     * @param fn
     * @param fn2
     * @param fn3
     * @param fn4
     * @return
     */
    public static <A,B,C,D,E> FList<E> For(FList<A> list, Function<A, FList<B>> fn, BiFunction<A, B,FList<C>> fn2,
      TriFunction<A,B,C,FList<D>> fn3, QuadFunction<A,B,C,D,FList<E>> fn4) {
    return list.bind(a -> {
      return fn.apply(a).bind(b -> {
        return fn2.apply(a,b).bind( c -> {
         return fn3.apply(a,b,c).bind( d -> {
          return fn4.apply(a,b,c,d); 
         }); 
        }); 
      });
    });
  }

    /**
     *
     *
     * @param list
     * @param p
     * @return
     */
      public static <A> FList<A> takeWhile(FList<A> list, Predicate<A> p) {
      if(list.isEmpty())
        return Nil.instance(); 
      else if (p.test(list.unsafeHead()))
        return of(list.unsafeHead(), () -> takeWhile(list.tail(), p));
      else
        return Nil.instance();
    }

    /**
     *
     *
     * @param list
     * @param p
     * @return
     */
      public static <A> boolean allTrue(FList<A> list, Predicate<A> p) {
      if(list.isEmpty())
        return true;
      else if(p.test(list.unsafeHead()))
        return allTrue(list.tail(), p);
      else
        return false;
    }

    public static <A> boolean allTrueWhile(FList<A> list, Predicate<A> w, Predicate<A> p) {
      if(list.isEmpty())
        return true;
      while(!list.isEmpty() && w.test(list.unsafeHead())) {
        if(!p.test(list.unsafeHead()))
          return false;
        list = list.tail();
      }
         return true;
    }

    /**
     * Returns a new Flist generated by applying the binary function to pairs of elements from the two
     * given FLists.
     *
     * @param fn A binary function that will whose operand types match the given two list.
     * @param l1 The FList used in the first operand postion within the binary function.
     * @param l2 The FList used in the second operand position within the binary function.
     * @return A new Flist generated by applying the binary function to pairs of elements from the two
     * given FLists.
     */
      public static <A,B,C> FList<C> zipWith(BiFunction<A,B,C> fn, FList<A> l1, FList<B> l2) {
      if(l1.isEmpty() ||  l2.isEmpty())
        return Nil.instance();
      return of(fn.apply(l1.unsafeHead(), l2.unsafeHead()), () -> zipWith(fn, l1.tail(), l2.tail()));
    }

    public static <A> boolean equals(FList<A> l1, FList<A> l2) {
      if(l1.isEmpty() != l2.isEmpty()) {
        return false;
      } else if(l1.isEmpty()) {
        return true;
      } else if(l1.head() == l2.head()){
        return equals(l1.tail(), l2.tail());
      } else {
        return false;
      }
    }

    /**
     *
     * Creates an infinite FList.
     * @param seed The first value to be created within the generated sequence.
     * @param generator A function that use the previous value within the FList to create the next value.
     * @return A infinite FList.
     */
      public static <A> FList<A> sequence(A seed, Function<A,A> generator) {
      return of(seed , () -> sequence(generator.apply(seed), generator));
    }

    public static <A> void print(FList<A> list) {
      System.out.println(list.toString());
    }
  }
}