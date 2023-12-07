package com.learning.exercise

object LazyMonad extends App {
  class Lazy[+A](x: => A) {
    private lazy val internalValue = x
    def get: A = internalValue
    def flatMap[B](f: (=> A) => Lazy[B]): Lazy[B] =
      f(internalValue)
    def map[B](f: (=> A) => B): Lazy[B] = flatMap(x => Lazy(f(x)))
  }

  object Lazy {
    def apply[A](x: => A): Lazy[A] = new Lazy[A](x)
    def flatten[B](m: Lazy[Lazy[B]]): Lazy[B] = m.flatMap(x => x)
  }

  val lazyVal = Lazy {
    println("I'm feeling lazy today")
    2 * 2
  }
  println(lazyVal)
  // Law - 1 - Left Identity
  val fun1: (=> Int) => Lazy[Int] = x => Lazy(x + 10)
  println(lazyVal.flatMap(fun1).get)
  println(fun1(2 * 2).get)

  // Law - 2 - Right Identity
  println(lazyVal.flatMap(Lazy.apply).get)
  println(lazyVal.get)

  // Law - 3 - Associativity
  val fun2: (=> Int) => Lazy[Int] = y => Lazy(y * 10)
  println(lazyVal.flatMap(fun1).flatMap(fun2).get)
  println(lazyVal.flatMap(x => fun1(x).flatMap(fun2)).get)

  println(lazyVal.map(_ * 20).get)
  val lazyOfLazy = Lazy(Lazy(5 + 4))
  println(lazyOfLazy.get)
  println(Lazy.flatten(lazyOfLazy).get)

}
