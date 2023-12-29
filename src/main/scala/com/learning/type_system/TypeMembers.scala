package com.learning.type_system

import com.learning
import com.learning.type_system

object TypeMembers extends App {
  /*
    TypeMembers are abstract types that can be defined inside a class and can be used as types
    That can help compiler to do type inference
    There is no constructor, So we can't build an object from it
   */

  class Animal
  class Cat extends Animal
  class Dog extends Animal
  class Kitty extends Cat {
    def sayHello(): Unit = println("Hello, I'm Kitty")
  }

  class AnimalCollection {
    type AnimalType
    type BoundedAnimalType <: Animal  // Sub types of Animal
    type SuperBoundedType >: Dog <: Animal  // Upper and Lower Bounded. Supertypes of Dog and Subtypes of Animals
    type CatAnimal = Cat  // Type alias for Cat
  }

  val ac = new AnimalCollection
  // val dog: ac.BoundedAnimalType = new Dog  // Cannot inference as the compiler doesn't know which subtype to choose
  val dog: ac.SuperBoundedType = new Dog  // This is possible
  // val cat: ac.SuperBoundedType = new Cat  // Compiler can't compile

  val cat: ac.CatAnimal = new Cat

  // We can define type members outside of a class also
  type CatAlias = Cat
  val anotherCat: CatAlias = new Cat

  // Abstract Type Members are sometimes used in APIs that looks similar to Generics
  trait MyList {
    type T
    def add(element: T): MyList
  }

  class NonEmptyIntList(value: Int) extends MyList {
    override type T = Int

    override def add(element: Int): MyList = new NonEmptyIntList(element)
  }


  // .type method can be used to get the corresponding type of an object
  val aCat = new Cat
  type CopyCatAlias = aCat.type
  val copyCat: CopyCatAlias = aCat
  // val newCat: CopyCatAlias = new Cat  // A new Cat object can't be assigned. This will not compile

  /*
    Exercise - Enforce a type to be applicable to SOME TYPES only
      - Below MList is developed by some other developer hence cannot be changed
      - Our task is to put a type Constraint so that the list can only be extended for Integers
    */

  // LOCKED
  trait MList {
    type A
    def head: A
    def tail: MList
  }

  trait ApplicableToNumbers {
    type A <: Int
  }

  class IntList(hd: Int, tl: IntList) extends MList with ApplicableToNumbers {
    override type A = Int
    override def head: Int = hd
    override def tail: MList = tl
  }

  // Below List of Strings won't compile
  /*
    class StringList(hd: String, tl: StringList) extends MList with ApplicableToNumbers {
      override type A = String
      override def head: String = hd
      override def tail: MList = tl
    }
  */

}
