package com.learning.implicits

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object MagnetPattern extends App {
  /*
    Magnet Pattern Provided solution to some of the problems created by method overloading using TypeClasses and Implicits

  */

  // Suppose we have an Actor trait with multiple overloaded methods
  class P2PRequest
  class P2PResponse
  class Serializer[T]

  trait Actor {
    def receive(statusCode: Int): Int
    def receive(request: P2PRequest): Int
    def receive(response: P2PResponse): Int
    def receive[T: Serializer](message: T): Int
    def receive[T: Serializer](message: T, statusCode: Int): Int
    def receive(future: Future[P2PRequest]): Int
    // def receive(future: Future[P2PResponse]): Int // Will cause error due to type erasure
  }

  /*
    Issues with Method Overloading -
      1 - Type Erasure - treats as List[Int] and List[String] as same List[T], So we can't have different overloaded methods.
      2 - Lifting doesn't work for all overloads
        val receiveFV = receive _ // The compiler is not able to figure out which method to pass
      3 - Code Duplication
      4 - Type inference and default args - If two overloaded method have some default args, the compiler is unable to figure which method should execute
   */

  trait MessageMagnet[Result] {
    def apply(): Result
  }

  def receive[R](magnet: MessageMagnet[R]): R = magnet()

  implicit class FromP2PRequest(request: P2PRequest) extends MessageMagnet[Int] {
    override def apply(): Int = {
      // Logic to process request
      println("From P2PRequest")
      24
    }
  }

  implicit class FromP2PResponse(response: P2PResponse) extends MessageMagnet[Int] {
    override def apply(): Int = {
      // Logic to process P2PResponse
      println("From P2PResponse")
      42
    }
  }

  receive(new P2PRequest)
  receive(new P2PResponse)

  // In the above implementation, only one receive method is defined which takes a magnet
  // The compiler identifies which apply method to execute based on the type passed to the receive method using implicit conversion
  // As the implement revolves around the MessageMagnet and receive method, This is called MagnetPattern

  // Advantages of MagnetPattern
  // 1 - no more type erasure problems
  implicit class FromResponseFuture(future: Future[P2PResponse]) extends MessageMagnet[Int] {
    override def apply(): Int = 2
  }

  implicit class FromRequestFuture(future: Future[P2PRequest]) extends MessageMagnet[Int] {
    override def apply(): Int = 3
  }

  println(receive(Future(new P2PRequest)))
  println(receive(Future(new P2PResponse)))

  // 2 - lifting works
  trait MathLib {
    def add1(x: Int): Int = x + 1

    def add1(s: String): Int = s.toInt + 1
    // add1 overloads
  }

  // "magnetize"
  trait AddMagnet {
    def apply(): Int
  }

  def add1(magnet: AddMagnet): Int = magnet()

  implicit class AddInt(x: Int) extends AddMagnet {
    override def apply(): Int = x + 1
  }

  implicit class AddString(s: String) extends AddMagnet {
    override def apply(): Int = s.toInt + 1
  }

  val addFV = add1 _
  println(addFV(1))
  println(addFV("3"))

  // Only works with Magnets without type dependency.
  // Below won't work as the MessageMagnet requires Result type
  // val receiveFV = receive _
  // receiveFV(new P2PResponse)

  /*
    Drawbacks
      1 - verbose
      2 - harder to read
      3 - you can't name or place default arguments
      4 - call by name doesn't work correctly
   */

  class Handler {
    def handle(s: => String) = {
      println(s)
      println(s)
    }
    // other overloads
  }

  trait HandleMagnet {
    def apply(): Unit
  }

  def handle(magnet: HandleMagnet) = magnet()

  implicit class StringHandle(s: => String) extends HandleMagnet {
    override def apply(): Unit = {
      println(s)
      println(s)
    }
  }

  def sideEffectMethod(): String = {
    println("Hello, Scala")
    "hahaha"
  }

  handle(sideEffectMethod())
  handle {  // In this case magnetize only the output of the block not the entire block
    println("Hello, Scala")
    "magnet"
  }

}
