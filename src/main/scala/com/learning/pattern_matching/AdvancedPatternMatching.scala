package com.learning.pattern_matching

import scala.annotation.tailrec

object AdvancedPatternMatching extends App {
  val numbers = List(2)
  val description = numbers match {
    case x :: Nil => s"The list contains only one element which is $x" // Nil is an Empty List
    case _ => "wild card"
  }
  println(description)

  /*
    We have seen that Pattern Matching can be applied on patterns such as:
      - Constants
      - Case Classes
      - Wildcards
      - Tuples
      - Some Special Magic Like Above
   */

  // Making a Class compatible with pattern matching
  class Person(val name: String, val age: Int)

  // To make it compatible with pattern matching we can create an object (with/without same name) and implement unapply method
  // The unapply method should generally return an Option. How ever it can return other types too
  // If the unapply method returns None or False then the pattern is not matched
  // We can have multiple overloaded unapply methods for multiple patterns
  object Person {
    def unapply(person: Person): Option[(String, Int)] = Some((person.name, person.age))

    def unapply(age: Int): Option[String] =
      if age > 18 then Some("Adult")
      else None
  }

  val bob = new Person("Bob", 20)
  println(
    bob match {
      case Person(n, a) => s"Hi, I'm $n. I'm $a years old" // The compiler passes the bob instance to Person.unapply and the returned Tuple is extracted into n and a
      case _ => "Default String"
    }
  )

  println(
    bob.age match {
      case Person(status) => s"${bob.name} is an $status"
      case _ => s"${bob.name} is an Minor"
    }
  )

  val x = 10
  x match {
    case x if x % 2 == 0 => println(s"$x is an even number")
    case x if x > -10 && x < 10 => println(s"$x is an single digit number")
    case _ => println(s"${x} is neither even nor single digit")
  }
  // The above code can be rewritten as
  object evenOption {
    def unapply(x: Int): Option[Boolean] = if x % 2 == 0 then Some(true) else None
  }
  // A problem with this is as we are returning Option[Boolean], Some(false) also will match the pattern which is logically incorrect.
  // So we can write it as
  object even {
    def unapply(x: Int): Boolean = x % 2 == 0
  }

  object singleDigitOption {
    def unapply(x: Int): Option[Boolean] = if x > -10 && x < 10 then Some(true) else None
  }

  object singleDigit {
    def unapply(x: Int): Boolean = x > -10 && x < 10
  }

  val y = 12
  println(
    y match {
      case evenOption(_) => s"$y is an even number"
      case singleDigitOption(_) => s"$y is a single digit number"  // As the unapply method returns Option[Boolean] _ is a placeholder for the Boolean value extracted from the Option
      case _ => s"$y is neither even nor single digit"
    }
  )

  val z = 5
  println(
    z match {
      case even() => s"$z is an even number"
      case singleDigit() => s"$z is a single digit number" // _ is not required
      case _ => s"$z is neither even nor single digit"
    }
  )


  /*
    Infix Patterns Such as head :: List(1, 2)
   */
  // We can define a case class which contains 2 attributes only and can work as infix pattern
  case class Or[A, B](x: A, y: B)
  val orVar = Or[Int, String](2, "Two")
  println(
    orVar match {
      case number Or string => s"$number is written as $string"
      case _ => "default"
    }
  )


  /*
    Decomposing Sequence: Pattern such as List(1, _*)
   */
  // Example of decomposing sequence pattern
  println(
    List(1, 2, 3) match {
      case List(1, _*) => "The list starts with 1"
      case l => s"$l"
    }
  )


  // Let's try to implement a custom List
  abstract class MyList[+T] {
    def head: T
    def tail: MyList[T]
    def isEmpty: Boolean
  }

  case object Empty extends MyList[Nothing] {
    override def head: Nothing = throw new RuntimeException("Empty List does not have a head")

    override def tail: MyList[Nothing] = throw new RuntimeException("Empty List does not have a tail")

    override def isEmpty: Boolean = true
  }

  case class Cons[+T](h: T, t: MyList[T]) extends MyList[T] {
    override def head: T = this.h

    override def tail: MyList[T] = this.t

    override def isEmpty: Boolean = false
  }

  // For making it compatible with decomposing sequence pattern, we must implement unapplySeq method which takes MyList as parameter
  // and returns an Option of Seq
  object MyList {
    def unapplySeq[T](list: MyList[T]): Option[Seq[T]] = {
      @tailrec
      def unapplySeqHelper(l: MyList[T], deSeq: Seq[T]): Seq[T] = {
        if l.isEmpty then deSeq
        else unapplySeqHelper(l.tail, deSeq :+ l.head)
      }
      Some(unapplySeqHelper(list, Seq.empty))
    }
  }

  val myList: MyList[Int] = Cons(1, Cons(2, Cons(3, Empty)))
  println(
    myList match {
      case MyList(1, 2, _*) => "MyList starts with 3, 2"
      case MyList(1, _*) => "MyList starts with 1"
      case l: MyList[Int] => s"$l"
    }
  )


  /*
    Custom Return Types for unapply.
      - unapply method can return a custom return type other than Option
      - But the return type must implement below methods
        - isEmpty: Boolean
        - get: Something
   */

  abstract class Wrapper[T] {
    def isEmpty: Boolean
    def get: T
  }

  object CustomPersonPattern {
    def unapply(person: Person): Wrapper[(String, Int)] = new Wrapper[(String, Int)] {
      override def isEmpty: Boolean = false

      override def get: (String, Int) = (person.name, person.age)
    }
  }

  println(
    bob match {
      case CustomPersonPattern(n, a) => s"Hi, I'm $n, I'm $a years old"
      case _ =>
    }
  )
}
