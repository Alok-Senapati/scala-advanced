package com.learning.fp

object CurryingAndPartiallyAppliedFunction extends App {
  /**
   * Methods are functions of classes
   * Functions are instances of FunctionX themselvesCurried Functions - Functions returning another function
   * Curried Method - Methods having multiple parameter list
   * Note - Method != Function
   * Note - We cannot used methods in HOFs, We must convert it to a function which is known as lifting or ETA - Expansion
   */
  val superAdder: Int => Int => Int = (x: Int) => (y: Int) => x + y  // Curried Function
  val add3 = superAdder(3)  // Partially Applied Function
  println(superAdder(3)(4))
  println(add3(4))

  def superAdderMethod(x: Int)(y: Int): Int = x + y  // Curried Method
  val add4: Int => Int = superAdderMethod(4) // When we provide type annotation the compiler does the lifting implicitly
  val add5 = superAdderMethod(5) // In Scala 2 it will throw missing argument list exception as it won't be able to do ETA expansion implicitly
  val add5Scala2 = superAdderMethod(5)_ // To let the compiler know to perform ETA expansion the _ is necessary
  // In Scala - 3, Both of the above will work
  println(add5(3))
  println(add5Scala2(3))

  println(List(1, 2, 3).map(superAdderMethod(5)))  // Performs ETA expansion even without placeholder _ both in scala 2 and 3
  println(List(1, 2, 3).map(superAdderMethod(5)_)) // Warning - Unnecessary Placeholder


  // EXERCISE
  val simpleAddFunction = (x: Int, y: Int) => x + y
  def simpleAddMethod(x: Int, y: Int) = x + y
  def curriedAddMethod(x: Int)(y: Int) = x + y

  // add7: Int => Int = y => 7 + y
  // as many different implementations of add7 using the above
  val add7 = (y: Int) => simpleAddFunction(7, y)
  val add7_2 = simpleAddFunction.curried(7)  // Converts to a curried function
  val add7_3 = simpleAddMethod.curried(7) // Converts to curried function
  val add7_4 = simpleAddFunction(7, _: Int)
  val add7_5 = simpleAddMethod(7, _:Int)
  val add7_6 = curriedAddMethod(7)_
  val add7_7 = curriedAddMethod(7) // In Scala - 3
  val add7_8 = curriedAddMethod(7)(_)

  // Underscores are powerful
  def concatenate(a: String, b: String, c: String) = a + b + c

  val insertName = concatenate("Hello, I'm ", _: String, ", how are you?")
  println(insertName("Daniel"))

  val fillInTheBlanks = concatenate("Hello, ", _: String, _: String)
  println(fillInTheBlanks("Daniel", " Scala is awesome!"))

  // EXERCISES
  /*
    1.  Process a list of numbers and return their string representations with different formats
        Use the %4.2f, %8.6f and %14.12f with a curried formatter function.
   */
  def curriedFormatter(s: String)(number: Double): String = s.format(number)

  val numbers = List(Math.PI, Math.E, 1, 9.8, 1.3e-12)

  val simpleFormat = curriedFormatter("%4.2f")
  val seriousFormat = curriedFormatter("%8.6f")
  val preciseFormat = curriedFormatter("%14.12f")

  println(numbers.map(curriedFormatter("%14.12f")))
  println(numbers.map(preciseFormat))


  /*
      2.  difference between
          - functions vs methods
          - parameters: by-name vs 0-lambda
     */
  def byName(n: => Int) = n + 1
  def byFunction(f: () => Int) = f() + 1
  def method: Int = 42
  def parenMethod(): Int = 42

  /*
    calling byName and byFunction
    - int
    - method
    - parenMethod
    - lambda
    - PAF
   */
  byName(23) // ok
  byName(method) // ok
  byName(parenMethod())
  // byName(parenMethod) // Scala 2: ok but beware ==> byName(parenMethod()); Scala 3 forbids calling the method with no parens
  // byName(() => 42) // not ok
  byName((() => 42)()) // ok
  // byName(parenMethod _) // not ok

  // byFunction(45) // not ok
  // byFunction(method) // not ok!!!!!! does not do ETA-expansion!
  byFunction(parenMethod) // compiler does ETA-expansion
  byFunction(() => 46) // works
  byFunction(parenMethod) // also works, but warning- unnecessary
}
