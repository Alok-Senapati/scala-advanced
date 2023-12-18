package com.learning.exercise

import com.learning.implicits.TypeClasses.{User, john}

object EqualityTypeClass extends App {
  trait Equal[T] {
    def apply(v1: T, v2: T): Boolean
  }

  // UserEquality based on Name
  implicit object EqualUserName extends Equal[User] {
    override def apply(v1: User, v2: User): Boolean = v1.name.equals(v2.name)
  }

  // UserEquality based on email
  object EqualUserEmail extends Equal[User] {
    override def apply(v1: User, v2: User): Boolean = v1.email.equals(v2.email)
  }

  // IntEquality
  implicit object IntegerEqual extends Equal[Int] {
    override def apply(v1: Int, v2: Int): Boolean = v1 == v2
  }

  // Equality Implicit
  object Equal {
    def apply[T](a: T, b: T)(implicit equalizer: Equal[T]): Boolean = equalizer(a, b)
  }

  val anotherJohn = User("John", 43, "john_potter@gmail.com")
  println(Equal(john, anotherJohn)) // Takes the implicit EqualUserName
  println(Equal(42, 43))

  // Enhancing Equal type class using Implicit classes
  implicit class RichEqual[T](value: T) {
    def ===(anotherValue: T)(implicit equal: Equal[T]): Boolean = equal(value, anotherValue)

    def !==(anotherValue: T)(implicit equal: Equal[T]): Boolean = !equal(value, anotherValue)
  }

  println("Testing Implicit Classes --------------->>>")

  println(5 === 6)
  println(5 !== 6)

  val mark = User("Mark", 25, "mark@gmail.com")
  val marie = User("Marie", 28, "marie@gmail.com")
  val anotherMark = User("Mark", 32, "mark2@gmail.com")
  println(mark === marie)
  println(mark !== marie)
  println(mark === anotherMark)  // Implicitly uses EqualUserName
  println(mark.===(anotherMark)(EqualUserEmail))

}
