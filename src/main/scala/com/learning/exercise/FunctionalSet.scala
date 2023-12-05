package com.learning.exercise

import scala.annotation.targetName

trait FunctionalSet[A] extends (A => Boolean) {
  override def apply(v1: A): Boolean = contains(v1)

  def contains(element: A): Boolean
  @targetName("add")
  def +(element: A): FunctionalSet[A]
  @targetName("concat")
  def ++(anotherSet: FunctionalSet[A]): FunctionalSet[A]  // union

  def map[B](f: A => B): FunctionalSet[B]
  def flatMap[B](f: A => FunctionalSet[B]): FunctionalSet[B]
  def filter(predicate: A => Boolean): FunctionalSet[A]
  def foreach(f: A => Unit): Unit

  @targetName("remove")
  def -(element: A): FunctionalSet[A]

  @targetName("difference")
  def --(anotherSet: FunctionalSet[A]): FunctionalSet[A]

  @targetName("intersection")
  def &(anotherSet: FunctionalSet[A]): FunctionalSet[A]

  @targetName("not")
  def unary_! : FunctionalSet[A] // We need to implement a PropertyBasedSet to get the not of a Set which contains all other element from the domain of A except the content of the Set
}

class EmptySet[A] extends FunctionalSet[A] {
  override def contains(element: A): Boolean = false

  @targetName("add")
  override def +(element: A): FunctionalSet[A] = NonEmptySet[A](element, this)

  @targetName("concat")
  override def ++(anotherSet: FunctionalSet[A]): FunctionalSet[A] = anotherSet

  override def map[B](f: A => B): FunctionalSet[B] = new EmptySet[B]

  override def flatMap[B](f: A => FunctionalSet[B]): FunctionalSet[B] = new EmptySet[B]

  override def filter(predicate: A => Boolean): FunctionalSet[A] = this

  override def foreach(f: A => Unit): Unit = ()

  @targetName("remove")
  override def -(element: A): FunctionalSet[A] = this

  @targetName("difference")
  override def --(anotherSet: FunctionalSet[A]): FunctionalSet[A] = this

  @targetName("intersection")
  override def &(anotherSet: FunctionalSet[A]): FunctionalSet[A] = this

  @targetName("not")
  override def unary_! : FunctionalSet[A] = new PropertyBasedSet[A](x => true)  // Set with all other elements from Domain of A
}

class PropertyBasedSet[A](property: A => Boolean) extends FunctionalSet[A] {
  override def contains(element: A): Boolean = property(element)

  @targetName("add")
  override def +(element: A): FunctionalSet[A] = new PropertyBasedSet[A](x => property(x) || x == element)  // The new property should return true for the added element

  @targetName("concat")
  override def ++(anotherSet: FunctionalSet[A]): FunctionalSet[A] = new PropertyBasedSet[A](x => property(x) || anotherSet(x))

  override def map[B](f: A => B): FunctionalSet[B] = politelyFail // As we cannot determine the domain of the mapped set i.e. if the output is finite or not and what elements it will hold

  override def flatMap[B](f: A => FunctionalSet[B]): FunctionalSet[B] = politelyFail

  override def filter(predicate: A => Boolean): FunctionalSet[A] = new PropertyBasedSet[A](x => property(x) && predicate(x))

  override def foreach(f: A => Unit): Unit = politelyFail

  @targetName("remove")
  override def -(element: A): FunctionalSet[A] = filter(x => x != element)

  @targetName("difference")
  override def --(anotherSet: FunctionalSet[A]): FunctionalSet[A] = filter(!anotherSet)

  @targetName("intersection")
  override def &(anotherSet: FunctionalSet[A]): FunctionalSet[A] = filter(anotherSet)

  @targetName("not")
  override def unary_! : FunctionalSet[A] = new PropertyBasedSet[A](x => !property(x))

  private def politelyFail = throw new IllegalArgumentException("Really deep rabbit tole.")
}

class NonEmptySet[A](head: A, tail: FunctionalSet[A]) extends FunctionalSet[A] {
  override def contains(element: A): Boolean = element == head | tail.contains(element)

  @targetName("add")
  override def +(element: A): FunctionalSet[A] = {
    if this contains element then this
    else new NonEmptySet[A](element, this)
  }

  @targetName("concat")
  override def ++(anotherSet: FunctionalSet[A]): FunctionalSet[A] = tail ++ anotherSet + head

  override def map[B](f: A => B): FunctionalSet[B] = tail.map(f) + f(head)

  override def flatMap[B](f: A => FunctionalSet[B]): FunctionalSet[B] = tail.flatMap(f) ++ f(head)

  override def filter(predicate: A => Boolean): FunctionalSet[A] = {
    val tailFilter = tail.filter(predicate)
    if predicate(head) then tailFilter + head
    else tailFilter
  }

  override def foreach(f: A => Unit): Unit = {
    f(head)
    tail.foreach(f)
  }

  @targetName("remove")
  override def -(element: A): FunctionalSet[A] = this.filter(_ != element)

  @targetName("difference")
  override def --(anotherSet: FunctionalSet[A]): FunctionalSet[A] = this.filter(!anotherSet)

  @targetName("intersection")
  override def &(anotherSet: FunctionalSet[A]): FunctionalSet[A] = this.filter(anotherSet)

  @targetName("not")
  override def unary_! : FunctionalSet[A] = new PropertyBasedSet[A](x => !this.contains(x))
}


object FunctionalSet {
  def apply[A](elements: A*): FunctionalSet[A] = {
    def setBuilder(elements: Seq[A], builtSet: FunctionalSet[A]): FunctionalSet[A] = {
      if elements.isEmpty then builtSet
      else setBuilder(elements.tail, builtSet + elements.head)
    }
    setBuilder(elements, new EmptySet[A])
  }
}


object TestFunctionalSet extends App {
  val set: FunctionalSet[Int] = FunctionalSet(1, 2, 3, 4, 3)
  set.foreach(println)
  println("-------------------------------")
  set + 5 foreach println
  println("-------------------------------")
  set ++ FunctionalSet(4, 5, 6) foreach println
  println("-------------------------------")
  println(set contains 3)
  println(set(11))
  println("-------------------------------")
  set.map(_ * 2).foreach(println)
  println("-------------------------------")
  set.flatMap(x => FunctionalSet(x, x * 10)).foreach(println)
  println("-------------------------------")
  set.filter(_ % 2 == 0).foreach(println)

  println("-------------------------------")
  val notSet = !set
  println(notSet(1))
  println(notSet(2))
  println(notSet(3))
  println(notSet(5))
  println(notSet(6))
  println("-------------------------------")
  val notSetEven = notSet.filter(_ % 2 == 0)
  println(notSetEven(5))
  println(notSetEven(6))
  println(notSetEven(2))
  println("-------------------------------")
  val removeElem = set - 2
  removeElem.foreach(println)
  println("-------------------------------")
  val difference = set -- FunctionalSet(1, 2)
  difference.foreach(println)
  println("-------------------------------")
  val intersection = set & FunctionalSet(2, 4, 5)
  intersection.foreach(println)
  println("-------------------------------")


}