package com.learning.syntax_sugars

import scala.annotation.targetName
import scala.util.Try

object DarkSugars extends App {
  /*
    1. Syntactic Sugar #1: Method with single param
   */
  def aSingleArgMethod(x: Int): String = s"$x ducks"

  // Evaluates the block (output of the last expression in the block) and passes to aSingleArgMethod as parameter
  // Output of the block must match the arg datatype of the function
  val description = aSingleArgMethod {
    // Some piece of code
    println("Inside aSingleArgMethod block")
    45
  }

  // Try uses similar syntactic sugar
  val aTryBlock = Try {  // Passes the output of the block to Try.apply method
    throw new RuntimeException()
  }

  // List.map
  val aListMap = List(1, 2, 3).map { x => // map expects a function and the block returns a function which gets passed into map
    x + 1
  }
  println(aListMap)
  val anotherListMap = List(4, 5, 6).map {
    println("Below Lambda x => x * 2 will get passed to map")
    x => x * 10
  }
  println(anotherListMap)


  /*
    2. Syntactic Sugar #2: Single Abstract Method - Instances of traits/abstract classes having single abstract method can be reduced to Lamdas
   */

  trait Animal {
    def act(x: Int): Int
  }
  val animal: Animal = new Animal {
    override def act(x: Int): Int = x * 10
  }
  // Can be converted to
  val animalSingleAbstractMethod: Animal = (x: Int) => x * 10 // The Lambda passes works as a implementation of the abstract method
  // The signature of the lambda must be same as of the abstract method. Type annotation is required
  println(animalSingleAbstractMethod.act(10))

  abstract class Vehicle {
    def start: String = "Vroom Vroom"
    def park(): String  // If an abstract method doesn't take parameters then it must have empty () to use single abstract method syn. sugar
  }

  val vehicle: Vehicle = () => "Parking the Vehicle"
  println(vehicle.start)
  println(vehicle.park())

  // Example - Runnables
  val aThread: Thread = new Thread(new Runnable {
    override def run(): Unit = println("Hello, Scala")
  })
  // As Runnable have only single abstract method run, So the above can also be written as
  val anotherThread: Thread = new Thread( () => println("Hello, Scala") )
  anotherThread.run()


  /*
    3. Syntactic Sugar #3 - The :: and #:: methods are special
   */
  val aList: List[Int] = 1 :: List(1, 2)
  // The above should mean 1.::(List(1, 2)), but integer doesn't have :: method
  // The above code executes as List(1, 2).::(1)
  // Scala Specification - The char at the end (:) defines the associativity
  // +: is right associative
  // :+ is left associative
  println(1 :: 2 :: 3 :: List(4, 2))  // Similar as List(4, 2).::(3).::(2).::(1)
  println(List(4, 2).::(3).::(2).::(1))

  class MyStream(val stream: String = "") {
    @targetName("prependToStream")
    def -->:(s: String): MyStream = {
      if stream.isBlank then new MyStream(s)
      else new MyStream(s"$s --> ${this.stream}")
    }

    @targetName("appendToStream")
    def :-->(s: String): MyStream = {
      if stream.isBlank then new MyStream(s)
      else new MyStream(s"${this.stream} --> $s")
    }

    override def toString: String = stream
  }
  val stream: MyStream = new MyStream()
  println("a" -->: "b" -->: stream)
  println(stream :--> "c" :--> "d")
  println("a" -->: "b" -->: stream :--> "c" :--> "d")


  /*
    4. Syntactic Sugar #4: Multi-word method naming
   */
  // We can create methods having multiple word enclosed inside ``
  class Girl(name: String) {
    def `and then said`(message: String): Unit = println(s"$name said $message")
  }
  val mary = new Girl("Mary")
  mary `and then said` "she wants to leave"


  /*
    5. Syntactic Sugar #5: Infix Types
   */
  // We we define a class of 2 Generic Types [A, B]. In Type annotation we can write them as A className B
  class Compose[A, B]

  val compose: Int Compose String = new Compose[Int, String]


  /*
    6. Syntactic Sugar #6: update() method is special like apply
   */
  val arr: Array[Int] = Array(1, 2, 3)
  arr(1) = 5  // Internally gets compiled to arr.update(1, 5)
  arr.update(2, 6)
  println(arr.mkString("{", ", ", "}"))


  /*
    7. Syntactic Sugar #7: Getter and Setter method for private attributes for mutable containers
   */
  class Mutable {
    private var internalMember: Int = 0

    def member: Int = internalMember  // Getter Method
    def member_=(value: Int): Unit = {  // Setter Method
      internalMember = value
    }
  }
  val mutable: Mutable = new Mutable
  println(mutable.member)
  mutable.member = 10
  println(mutable.member)
}
