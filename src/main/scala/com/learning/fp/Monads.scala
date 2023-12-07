package com.learning.fp

object Monads extends App {
  /**
   *  A monad is a general concept that helps with operations between pure functions
   *  to deal with side effects (which is when a function changes a variable that’s non-local or outside of its scope).
   *
   *  Monads are nothing more than a mechanism to sequence computations around values augmented with some additional feature. Such features are called effects.
   *  Some well-known effects are managing the nullability of a variable or managing the asynchronicity of its computation.
   *  In Scala, the corresponding monads to these effects are the Option[T] type and the Future[T] type.
   *  A monad adds an effect to a value wrapping it around a context.
   *
   *  Monads are basically consists of below components
   *  1. Wrapping values inside a Monad - Unit Function
   *    - Monads must provide a function that allows wrapping a generic value with the monad’s context.
   *    - We usually call such a function unit. It’s said that the unit function lifts the value in the monadic context.
   *    - In Scala, we can use the apply method of a companion object to implement the unit function
   *  2. Sequencing Computations Over a Value - the flatmap Function
   *    - However, the sole capability to add an effect to a monad isn’t worth the complexity added to the code.
   *    - Moreover, we don’t want to extract the monad’s wrapped value to apply functions to it. It’s cumbersome and unmaintainable.
   *    - We need a mechanism to sequence computations over a value wrapped inside a monad.
   *    - To overcome this problem, monads must provide the flatMap function.
   *    - This function takes as input another function from the value of the type wrapped by the monad to the same monad applied to another type
   *    - It transforms the value inside a monad into another value without performing any extraction to make it simpler
   *  3. Make It More Imperative: Using the For-comprehension
   *    - We can define the map function for any monad in terms of the flatMap function
   *    - Why should we do that? Because for any type providing both the map and the flatMap functions in Scala, we can use the for-comprehension construct
   *    - The for-comprehension is quite useful for concatenating computations on the same monad
   *
   *  However, Only implementing the above functionality is not sufficient for a Monad. A Monad must fulfill below 3 Mathematical laws
   *  1. Left Identity -
   *    - The first of the three laws, called “left identity”, says that applying a function f using the flatMap function
   *      to a value x lifted by the unit function is equivalent to applying the function f directly to the value x
   *              Monad.unit(x).flatMap(f) = f(x)
   *  2. Right Identity -
   *    - The second monadic law is called “right identity”. It states that application of the flatMap function using the
   *      unit function as the function f results in the original monadic value
   *              x.flatMap(y => Monad.unit(y)) = x
   *  3. Associativity -
   *    - This law says that applying two functions f and g to a monad value using a sequence of flatMap calls is equivalent
   *      to applying g to the result of the application of the flatMap function using f as the parameter
   *              x.flatMap(f).flatMap(g) = o.flatMap(x => f(x).flapMap(g))
   *
   *  Examples of Monads in Scala - List, Option, Set, Try etc
   *
   *  Let's Implement our version of Try as a Monad
   */

  trait Attempt[+A] {
    def flatMap[B](f: A => Attempt[B]): Attempt[B]
    def toString: String
  }
  object Attempt {
    def apply[A](x: => A): Attempt[A] = {
      try {
        Success(x)
      } catch {
        case e: Exception => Failure(e)
      }
    }
  }

  class Success[A](x: => A) extends Attempt[A] {
    lazy val value: A = x
    override def flatMap[B](f: A => Attempt[B]): Attempt[B] = {
      try {
        f(value)
      } catch {
        case e: Exception => Failure(e)
      }
    }

    override def toString: String = try {
      s"Success($value)"
    } catch {
      case e: Exception => Failure(e).toString
    }
  }

  class Failure(e: Exception) extends Attempt[Nothing] {
    override def flatMap[B](f: Nothing => Attempt[B]): Attempt[B] = Failure(e)

    override def toString: String = s"Failure($e)"
  }

  // Testing the 3 laws
  val successAttempt = Attempt {
    2 + 3
  }
  val failureAttempt = Attempt {
    5 / 0
  }

  println(successAttempt)
  println(failureAttempt)

  // left - identity
  val fun1: Int => Attempt[Int] = x => Attempt(x * 2)
  println(successAttempt.flatMap(fun1))
  println(fun1(2 + 3))

  // right - identity
  println(successAttempt.flatMap(Attempt.apply))
  println(successAttempt)

  // associative
  val fun2: Int => Attempt[Int] = x => Attempt(x + 2)
  println(successAttempt.flatMap(fun1).flatMap(fun2))
  println(successAttempt.flatMap(x => fun1(x).flatMap(fun2)))


  /*
      EXERCISE:
      1) implement a Lazy[T] monad = computation which will only be executed when it's needed.
        unit/apply
        flatMap

      2) Monads = unit + flatMap
         Monads = unit + map + flatten

         Monad[T] {

          def flatMap[B](f: T => Monad[B]): Monad[B] = ... (implemented)

          def map[B](f: T => B): Monad[B] = ???
          def flatten(m: Monad[Monad[T]]): Monad[T] = ???

          (have List in mind)
     */
  // Solution -> Check com.learning.exercise.LazyMonad

}
