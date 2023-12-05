package com.learning.fp

object PartialFunctions extends App {

  val aFunction = (x: Int) => x + 1
  // The above function is called as a Total Function as it works on the entire domain of Int

  // Partial Function - Function that works on certain values or sub domain of the input data type
  // Partial functions can only have one parameter
  val aPartialFunction = (x: Int) => {
    if x == 1 then 42
    else if x == 2 then 56
    else if x == 5 then 999
    else throw new FunctionNotApplicableException
  }
  class FunctionNotApplicableException extends RuntimeException
  // The above function works for only for 1, 2, 5 hence it's an partial function

  // More clean way to write this implementation using pattern matching
  // No need for throwing exception for other values, as It will anyway throw MatchError for other values
  val partialFunctionUsingPM: Int => Int = (x: Int) => x match {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  }
  println(partialFunctionUsingPM(2))

  // Scala also provides a PartialFunction[A, B] class which can be used
  val scalaPartialFunction: PartialFunction[Int, Int] = {  // No need to write match statement
    case 1 => 42
    case 2 => 55
    case 5 => 999
    // case _ => throw new FunctionNotApplicableException  // Must avoid using default pattern or else isDefinedAt will return true even if for a input for which the function is not applicable
  }
  println(scalaPartialFunction(5))

  // Utilities
  // Check if the partial function is applicable for an input
  println(scalaPartialFunction.isDefinedAt(10))
  println(scalaPartialFunction.isDefinedAt(2))

  // Convert partial function to total function which will return Option
  val partial2TotalFunction = scalaPartialFunction.lift  // Int => Option[Int]
  println(partial2TotalFunction(2))
  println(partial2TotalFunction(10))

  // Chaining of partial function
  val pfChain = scalaPartialFunction.orElse[Int, Int] {
    case 45 => 67
  }
  println(pfChain(2))
  println(pfChain(45))


  // Partial Functions extend Total function
  // A total function can be assigned with a partial function literal
  val aTotalFunction: Int => Int = {
    case 20 => 30
    case 30 => 40
    case 40 => 50
  }
  println(aTotalFunction(20))

  // HOFs can also take partial functions
  val aMappedList = List(1, 2, 3).map {
    case 1 => 2
    case 2 => 4
    case 3 => 5
  }
  println(aMappedList)

  /**
   * Exercises -
   *
   * 1. Construct a PF instance using anonymous class
   * 2. Dumb Chatbot using PF
   */

  // 1. Solution -
  // Need to override apply and isDefinedAs abstract method
  val anAnonymousPartialFunction = new PartialFunction[Int, Int] {
    override def apply(v1: Int): Int = v1 match {
      case 1 => 100
      case 2 => 200
      case 3 => 300
    }

    override def isDefinedAt(x: Int): Boolean = x == 1 || x == 2 || x == 3
  }
  println(anAnonymousPartialFunction(2))
  println(anAnonymousPartialFunction.isDefinedAt(2))
  println(anAnonymousPartialFunction.isDefinedAt(20))

  // 2. Solution
  val chatbotPF: PartialFunction[String, String] = {
    case "hello" => "Hello, My name is chatbot"
    case "goodbye" => "Thank you for chatting with me"
    case "search" => "Searching your query..."
  }
  scala.io.Source.stdin.getLines().map(chatbotPF).foreach(println)

}
