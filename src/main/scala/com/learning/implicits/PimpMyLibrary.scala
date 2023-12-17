package com.learning.implicits

import scala.annotation.{tailrec, targetName}
import scala.util.Random

object PimpMyLibrary extends App {
  /*
    Implicit Classes -
      - We can create implicit classes for a Type to enrich functionalities of that type
      - Implicit classes must take one and only argument of the type it wants to enhance
      - This is also called Type Enrichment or Pimping
   */

  implicit class RichInt(val n: Int) extends AnyVal {   // Extending AnyVal is optional but done for optimization purposes
    def isEven: Boolean = n % 2 == 0

    def sqrt: Double = Math.sqrt(n)
  }

  println(10.isEven)  // Compiler searches for implicit classes that can wrap Int to a class having isEven method
                      // and converts it to new RichInt(10).isEven
  println(144.sqrt)

  // Other examples present in scala
  println(1 to 10)  // Here to is a method of a Implicit class of type Int (RichInt class of package scala)

  import scala.concurrent.duration._
  println(3.seconds)

  // Compiler can do multiple search
  implicit class RicherInt(val x: Int) extends AnyVal {
    def isOdd: Boolean = x % 2 != 0
  }
  println(11.isOdd)


  /*
    Example -
    Enrich the String class
    - asInt
    - encrypt
      "John" -> Lqjp

    Keep enriching the Int class
    - times(function)
      3.times(() => ...)
    - *
      3 * List(1,2) => List(1,2,1,2,1,2)
   */

  implicit class RichString(val s: String) extends AnyVal {
    def asInt: Int = Integer.valueOf(s)
    def encrypt(cypherDistance: Int): String = s.map(c => (c + cypherDistance).asInstanceOf[Char])
  }
  println("23".asInt + 30)
  println("John".encrypt(Random.nextInt(10)))

  implicit class EnrichedInt(val i: Int) extends AnyVal {
    def times(func: () => Unit): Unit = {
      @tailrec
      def auxTimes(n: Int, func: () => Unit): Unit = {
        if n <= 0 then ()
        else {
          func()
          auxTimes(n - 1, func)
        }
      }

      auxTimes(i, func)
    }


    @targetName("multiplyList")
    def *[T](list: List[T]): List[T] = {
      @tailrec
      def auxMul(n: Int, list: List[T], acc: List[T]): List[T] = {
        if n <= 0 then acc
        else auxMul(n - 1, list, acc ++ list)
      }

      auxMul(i, list, Nil)
    }
  }

  3.times(() => println("Hello Scala"))
  println(5 * List("A", "B", "C"))

  // Performing implicit conversion of string to int
  implicit def stringToInt(s: String): Int = Integer.valueOf(s)
  println("10" + 6)
  println("10" / 2)

  /*
    Best Practices -
      - Keep Type enrichment to implicit classes and type classes as much as possible
      - Avoid implicit defs as much as possible, because any logical error in an implicit def can cause uncertain behavior and hard to track the issue
      - Package implicits clearly and bring into scope only what we need
      - If any conversion needed, Make then specific
   */
}
