package me.functional.functions;

public interface Try1<A,B,E extends Exception> {
  B call(A a) throws E;
}
