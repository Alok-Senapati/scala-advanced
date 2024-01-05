package com.learning.type_system

object FBoundedPolymorphism extends App {
  // Problem
  trait Animal {
    def breed: List[Animal]
  }

  class Cat extends Animal {
    override def breed: List[Animal] = ???  // But I want to return List[Cat]
  }

  class Dog extends Animal {
    override def breed: List[Animal] = ???  // I want to return List[Dog]
  }

  // In the above code we can change the return type as List[Cat] and List[Dog] as List is covariant
  // But compiler does not forces type correctness
  // Also, List[Dog] can also be returned from breed method of Cat which is logically incorrect

  // Solution - 1
  trait NewAnimal[A <: NewAnimal[A]] {  // Recursive Type OR F-Bounded Polymorphism, Here A is a subtype of NewAnimal[A]
    def breed: List[NewAnimal[A]]
  }

  class NewDog extends NewAnimal[NewDog] {
    override def breed: List[NewAnimal[NewDog]] = ???
  }

  class NewCat extends NewAnimal[NewCat] {
    override def breed: List[NewAnimal[NewCat]] = ???
  }

  // Here the problem is that the compiler allows a class Crocodile to extend Animal[NewDog] and hence the breed method to return Animal[NewDog]
  class NewCrocodile extends NewAnimal[NewDog] {
    override def breed: List[NewAnimal[NewDog]] = ???
  }

  // To fix this issue, we can use FBP + Self Type
  trait AnotherAnimal[A <: AnotherAnimal[A]] { self: A => // Enforces that a self type A must be implemented
    def breed: List[AnotherAnimal[A]]
  }

  class AnotherCat extends AnotherAnimal[AnotherCat] {
    override def breed: List[AnotherAnimal[AnotherCat]] = ???
  }

  /* Below code will not compile as AnotherCrocodile can only extend AnotherAnimal[AnotherCrocodile]
    class AnotherCrocodile extends AnotherAnimal[AnotherCat] {
      override def breed: List[AnotherAnimal[AnotherCat]] = ???
    }
  */

  // The above approach works well for 1 level of inheritance but not for multilevel
  class Fish extends AnotherAnimal[Fish] {
    override def breed: List[AnotherAnimal[Fish]] = ???
  }

  class Whale extends Fish {
    override def breed: List[AnotherAnimal[Fish]] = List[Dolphin]() // Wrong Logic but compiler does not restricts
  }

  class Dolphin extends Fish {
    override def breed: List[AnotherAnimal[Fish]] = ???
  }

  trait Animal1
  trait CanBreed[A] {
    def breed(x: A): List[A]
  }

  class Dog1 extends Animal1
  object Dog1 {
    implicit object DogsCanBreed extends CanBreed[Dog1] {
      def breed(x: Dog1): List[Dog1] = List()
    }
  }

//  implicit class CanBreedOps[A](animal: A) {
//    def breed(implicit canBreed: CanBreed[A]): List[A] = canBreed.breed(animal)
//  }

  val dog = new Dog1
//  dog.breed

  class Cat1 extends Animal1
  object Cat1 {
    implicit object CatsCanBreed extends CanBreed[Dog1] {
      override def breed(x: Dog1): List[Dog1] = List()
    }
  }

  val cat = new Cat1
  // cat.breed  // Compiler will restrict as the CatsCanBreed extends CanBreed[Dog1]
  // The Compiler restricts the calling but not the wrong implementation of CatsCanBreed

  // Here, we can see that the Animal class does not have any methods. Which contradicts the concept of OOP
  // To fix that, we can declare Animal class as a TypeClass
  trait AnimalType[A] {
    def breed(a: A): List[A]
  }

  class DogType
  object DogType {
    implicit object DogAnimal extends AnimalType[DogType] {
      override def breed(a: DogType): List[DogType] = List()
    }
  }

  implicit class AnimalTypeOps[A](animal: A) {
    def breed(implicit animalTypeInstance: AnimalType[A]): List[A] = animalTypeInstance.breed(animal)
  }

  val newDog = new DogType
  println(newDog.breed)

  class CatType
  object CatType {
    implicit object CatAnimal extends AnimalType[DogType] {
      override def breed(a: DogType): List[DogType] = ???
    }
  }

  val newCat = new CatType
  // println(newCat.breed)  // Compiler won't allow


}