package com.learning.exercise

import scala.annotation.{tailrec, targetName}

abstract class SinglyLinkedStream[+A] {
  def isEmpty: Boolean
  def head: A
  def tail: SinglyLinkedStream[A]

  @targetName("prepend")
  def #::[B >: A](element: B): SinglyLinkedStream[B] // prepend operator
  @targetName("concat")
  def ++[B >: A](anotherStream: => SinglyLinkedStream[B]): SinglyLinkedStream[B] // concatenate

  def foreach(f: A => Unit): Unit
  def map[B](f: A => B): SinglyLinkedStream[B]
  def flatMap[B](f: A => SinglyLinkedStream[B]): SinglyLinkedStream[B]
  def filter(predicate: A => Boolean): SinglyLinkedStream[A]

  def take(n: Int): SinglyLinkedStream[A]  // Takes first n elements out of the stream
  def takeAsList(n: Int): List[A]

  @tailrec
  final def toList[B >: A](acc: List[B] = Nil): List[B] = {
    if isEmpty then acc.reverse
    else tail.toList(head :: acc)
  }
}

object SinglyLinkedStream {
  def from[A](x: A)(generator: A => A): SinglyLinkedStream[A] = {
    new NonEmptyStream[A](x, SinglyLinkedStream.from(generator(x))(generator))
  }
}

object EmptyStream extends SinglyLinkedStream[Nothing] {
  override def isEmpty: Boolean = true
  override def head: Nothing = throw new RuntimeException("Empty stream has no head")
  override def tail: SinglyLinkedStream[Nothing] = throw new RuntimeException("Empty stream has no tail")

  @targetName("prepend")
  override def #::[B >: Nothing](element: B): SinglyLinkedStream[B] = new NonEmptyStream[B](element, this)
  @targetName("concat")
  override def ++[B >: Nothing](anotherStream: => SinglyLinkedStream[B]): SinglyLinkedStream[B] = anotherStream

  override def foreach(f: Nothing => Unit): Unit = ()
  override def map[B](f: Nothing => B): SinglyLinkedStream[B] = EmptyStream
  override def flatMap[B](f: Nothing => SinglyLinkedStream[B]): SinglyLinkedStream[B] = EmptyStream
  override def filter(predicate: Nothing => Boolean): SinglyLinkedStream[Nothing] = EmptyStream

  override def take(n: Int): SinglyLinkedStream[Nothing] = EmptyStream
  override def takeAsList(n: Int): List[Nothing] = Nil
}

class NonEmptyStream[+A](h: A, t: => SinglyLinkedStream[A]) extends SinglyLinkedStream[A] {
  override def isEmpty: Boolean = false
  override val head: A = this.h
  override lazy val tail: SinglyLinkedStream[A] = this.t  // Performs Lazy Evaluation on tail

  @targetName("prepend")
  override def #::[B >: A](element: B): SinglyLinkedStream[B] = new NonEmptyStream[B](element, this)
  @targetName("concat")
  override def ++[B >: A](anotherStream: => SinglyLinkedStream[B]): SinglyLinkedStream[B] = new NonEmptyStream[B](head, tail ++ anotherStream)

  override def foreach(f: A => Unit): Unit =
    f(head)
    tail.foreach(f)
  override def map[B](f: A => B): SinglyLinkedStream[B] =
    new NonEmptyStream[B](f(head), tail.map(f))
  override def flatMap[B](f: A => SinglyLinkedStream[B]): SinglyLinkedStream[B] =
    f(head) ++ tail.flatMap(f)
  override def filter(predicate: A => Boolean): SinglyLinkedStream[A] =
    if predicate(head) then new NonEmptyStream[A](head, tail.filter(predicate))
    else tail.filter(predicate)

  override def take(n: Int): SinglyLinkedStream[A] =
    if n <= 0 then EmptyStream
    else if n == 1 then new NonEmptyStream[A](head, EmptyStream)
    else new NonEmptyStream[A](head, tail.take(n - 1))
  override def takeAsList(n: Int): List[A] = take(n).toList()
}

object TestSinglyLinkedStream extends App {
  val naturals = SinglyLinkedStream.from(1)(_ + 1)
  println(naturals.take(200))
  naturals.take(10000).foreach(println)
  val startFrom0 = 0 #:: naturals
  println(startFrom0.takeAsList(10))

  println(startFrom0.map(_ * 2).takeAsList(20))
  println(startFrom0.flatMap(x => new NonEmptyStream(x, new NonEmptyStream(x + 1, EmptyStream))).takeAsList(20))
  // println(startFrom0.filter(_ < 20).takeAsList(30))  // Will result in a StackOverflowError
  // As in startFrom0 after the element becomes greater than or equals to 20. If we try to take more elements
  // The code tries to recursively get n no of elements, But after the value of 20 there won't be any element satisfying the predicate
  // Hence the above line will fail
  println(startFrom0.filter(_ < 20).takeAsList(20))

  /*
    Exercises on streams
    1 - stream of Fibonacci numbers
    2 - stream of prime numbers with Eratosthenes' sieve
    [ 2 3 4 ... ]
    filter out all numbers divisible by 2
    [ 2 3 5 7 9 11 ...]
    filter  out all numbers divisible by 3
    [ 2 3 5 7 11 13 17 ... ]
    filter out all numbers divisible by 5
      ...
   */
  def fibonacciStream(first: BigInt, second: BigInt): SinglyLinkedStream[BigInt] = {
    new NonEmptyStream[BigInt](first, fibonacciStream(second, first + second))
  }

  val fibSeries = fibonacciStream(0, 1)
  println(fibSeries.takeAsList(20))

  def eratosthenesPrimeSeries(series: SinglyLinkedStream[Int]): SinglyLinkedStream[Int] = {
    new NonEmptyStream[Int](series.head, eratosthenesPrimeSeries(series.tail.filter(_ % series.head != 0)))
  }

  val primes = eratosthenesPrimeSeries(SinglyLinkedStream.from(2)(_ + 1))
  println(primes.takeAsList(30))

  def fibonacciLazyList(first: BigInt, second: BigInt): LazyList[BigInt] = {
    LazyList.cons(first, fibonacciLazyList(second, first + second))
  }
  val fibLazyList = fibonacciLazyList(0, 1)
  println(fibLazyList)
  fibLazyList.take(40).foreach(x => print(s"$x "))

}
