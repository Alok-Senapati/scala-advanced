package com.learning.fp

import scala.util.Random

object LazyEvaluation extends App {
  /**
   * Lazy evaluated variables/functions are only evaluated when they are actually used
   * We can define using lazy keyword
   * Once the variable is evaluated, it won't evaluate again
   */
  lazy val exception = throw new RuntimeException()
  println("Not thrown any exceptions yet")

  println("Below line will throw exception as val exception is getting used")
  // println(exception)

  println("Defining x")
  lazy val x: Int = {
    println("x is being evaluated")
    42
  }
  println("Printing x")
  println(x)  // Evaluates x hence prints x is being evaluated
  println(x)  // Won't evaluate again

  def sideEffectCondition: Boolean = {
    println("Boo")
    true
  }
  def simpleCondition: Boolean = false
  lazy val lazyCondition = sideEffectCondition
  println(if simpleCondition && lazyCondition then "yes" else "no")  // Won't print "Boo" as the lazyCondition is not being evaluated as simpleCondition is false
  println(if lazyCondition && simpleCondition then "yes" else "no")  // Here Boo will get printed as lazyCondition will get evaluated
  println(if lazyCondition && simpleCondition then "yes" else "no")  // Here Boo will not get printed as lazyCondition is already evaluated

  /**
   * CALL BY NEED ->
   * In CallByName, the input will get evaluated every time it's being used inside the code
   * To avoid that we can store the input in a lazy val where it will only get evaluated when it's getting used for the first time
   * This is called callByNeed
   */
  def callByName(x: => Int): Int = x * x * x + 1
  def getSomeNumber: Int = {
    println("Sleeping for 1 second")
    Thread.sleep(1000)
    25
  }
  println(callByName(getSomeNumber))  // Will Sleep for 3 secs as the getSomeNumber will get called 3 times

  def callByNeed(x: => Int): Int = {
    lazy val t = x
    t * t * t + 1
  }
  println(callByNeed(getSomeNumber))  // Will sleep for only 1sec as the getSomeNumber will get called once

  def greaterThan20(x: Int): Boolean = {
    println(s"Checking if $x is greater than 20")
    x > 20
  }

  def lessThan30(x: Int): Boolean = {
    println(s"Checking if $x is less than 30")
    x < 30
  }

  val aList = List(1, 2, 12, 22, 34, 27, 28, 21)
  val gt20 = aList.filter(greaterThan20)
  val lt30 = gt20.filter(lessThan30)
  println(lt30)  // Prints all the side effects of both greaterThan20 and lessThan30 for all the element
  // First applies greaterThan20 on all elements and then applies lessThan30 on the result List




}
