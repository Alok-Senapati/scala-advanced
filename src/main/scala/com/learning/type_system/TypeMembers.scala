package com.learning.type_system

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
    type SuperBoundedType >: Animal  // Super types of Animal
    type CatAnimal = Cat  // Type alias for Cat
  }

  val ac = new AnimalCollection
  // val dog: ac.BoundedAnimalType = new Dog  // Cannot inference as the compiler doesn't know which subtype to choose
  val dog: ac.SuperBoundedType = new Dog  // This is possible
  val kitty: ac.SuperBoundedType = new Kitty



}
