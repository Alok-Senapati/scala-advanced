package com.learning.implicits

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ImplicitsIntro extends App {
  /*
    Implicits
      - Scala Implicits are a way of defining and passing optional parameters
      - If not passed the compiler tries to find any matching implicit types and passes it to the function implicitly
    NOTE - Type annotation is must for implicit values and functions
  */
  val aPair = 1 -> "One" // Here, -> is not a function from either Int or String class
  // Still the compiler is able to identify it
  // Basically the compiler tries to look for any implicit methods which might be there in the scope satisfying the required function signature

  // -> is an implicit method present in PreDef class which gets automatically imported in a scala code
  println(aPair)

  // Example
  case class Person(name: String) {
    def greet: String = s"Hi !! My name is $name"
  }

  implicit def fromStringToPerson(name: String): Person = Person(name)

  /*
    case class Friend(friendOf: String) {
      def greet: String = s"Hi !! I'm friend of $friendOf"
    }

    implicit def fromStringToFriend(friendOf: String): Friend = Friend(friendOf)
  */

  // If the above commented block is uncommented, The compiler will throw Exception as ambiguous implicits are in same scope
  // That is there are 2 implicit functions that convert a string to an Object having greet method

  println("Alok".greet) // Here, the compiler searches for a implicit val/function which can convert a String to an object having greet method


  // Implicit Parameters
  implicit val addWith: Int = 5
  // implicit val ambiguousAddWith: Int = 10   // Will cause exception
  def implicitSum(x: Int)(implicit y: Int) = x + y
  println(implicitSum(2))

  // The above code will
  //  1. Throw exception if there is no implicit val defined of Int type
  //  2. If the implicit val of type Int is defined then the compiler will automatically pass that
  //  3. Will throw ambiguity error if there are multiple implicits defined of type Int in same scope

  // The Future's apply method have a implicit parameter for executionContext for which we can import scala.concurrent.ExecutionContext.Implicits.global
  val aFuture: Future[Unit] = Future {
    Thread.sleep(2000)
    println("Inside Future")
  }

  // Same as
  //  val aFuture: Future[Unit] = Future {
  //    Thread.sleep(2000)
  //    println("Inside Future")
  //  }(global)

  Thread.sleep(3000)

}
