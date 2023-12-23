package com.learning.type_system


object Variance extends App {
  /*
    Variance lets you control how type parameters behave with regards to subtyping.
    Scala supports variance annotations of type parameters of generic classes, to allow them to be
    covariant, contravariant, or invariant if no annotations are used.
    The use of variance in the type system allows us to make intuitive connections between complex types.
   */

  trait Animal
  class Cat extends Animal
  class Dog extends Animal
  class Crocodile extends Animal

  // What is Variance Problem ???
  class Cage[T](val content: T)
  // Suppose we have a Cage class which can take type parameter
  // The Variance problem is given as: Given Cat is a subtype of Animal, Is Cage[Cat] also a subtype of Cage[Animal] ?
  // There can be 3 possible scenarios defined as: Invariance, Covariance, Contravariance


  // 1. Invariance - ICage[Cat] cannot be a subtype of ICage[Animal]
  class ICage[T](val content: T)  // Constructor parameters can be val or var
  val catCage = ICage[Cat](new Cat)
  // val animalCage: ICage[Animal] = catCage  // Will result in compilation error


  // 2. Covariance - CCage[Cat] is a subtype of CCage[Animal]
  class CCage[+T](val content: T)  // Constructor parameters can only be val
  val cCatCage = CCage[Cat](new Cat)
  val cAnimalCage: CCage[Animal] = cCatCage
  val cat: Cat = cAnimalCage.content.asInstanceOf[Cat]
  println(cat)
  // In the above, the val cat expects the AnimalCage to contain a Cat, But if var is used below line of code can run
  // which will put a Dog inside the Cage which is supposed to hold Cat, Hence a var in constructor will break the type
  // safety and restricted by the compiler throwing Error: Covariant type T occurs in contravariant position in type T of value content
  // cAnimalCage.content = CCage[Dog]("Tommy")


  // 3. Contravariance - (Opposite) - If A is subtype of B then ContraCage[B] is a type of ContraCage[A]
  class ContraCage[-T]  // Constructor parameters can't be val or var
  val contraAnimalCage: ContraCage[Animal] = ContraCage[Animal]
  val contraCatCage: ContraCage[Cat] = contraAnimalCage

  // Why constructor parameter is not var or val ?
  // If that happens we can have a ContraCage[Animal](new Crocodile) assigned to ContraCage[Cat] which is not possible
  // Another example of Contravariance
  abstract class Serializer[-A]:
    def serialize(a: A): String

  val animalSerializer: Serializer[Animal] = new Serializer[Animal]():
    def serialize(animal: Animal): String = s"""{ "name": "${animal.getClass}" }"""

  val catSerializer: Serializer[Cat] = animalSerializer
  println(catSerializer.serialize(new Cat))



  // Covariance - Method Parameter Types
  /*
    class CovariantAnimalList[+T]{
       def add(animal: T): CovariantAnimalList[T] = CovariantAnimalList[T]
    }
   */
  // The above implementation is not possible, because if it is possible below code can also be possible which will break the type safety
  // val animalCageList: CovariantAnimalList[Animal] = new CovariantAnimalList[Cat]
  // animalCageList.add(Dog("Tommy"))  // List of Cat can't contain a Dog
  // To solve this problem we can implement
  class CovariantAnimalList[+T] {
    def add[B >: T](animal: B): CovariantAnimalList[B] = new CovariantAnimalList[B]  // B super type of T
  }
  class Kitty extends Cat
  val emptyList = new CovariantAnimalList[Kitty]  // CovariantAnimalList[Kitty]
  val kittyList = emptyList.add(new Kitty)  // Results a CovariantAnimalList[Kitty]
  val catsList = kittyList.add(new Cat)  // Results a CovariantAnimalList[Cat] as Cat is supertype of Kitty
  val animalList = kittyList.add(new Animal {})  // Results a CovariantAnimalList[Animal] as Animal is supertype of Kitty
  val animalListFromCats = catsList.add(new Animal {})  // Results a CovariantAnimalList[Animal] as Animal is supertype of Animal
  val dogList = kittyList.add(new Dog)  // Results a CovariantAnimalList[Animal] to make it compatible to store Dogs also


  // ContraVariance - Return Types
  class PetShop[-T] {
    // def get(isItaPuppy: Boolean): T // This is not possible as it will make the below code possible which is incorrect
    /*
      val catShop = new PetShop[Animal] {
        def get(isItaPuppy: Boolean): Animal = new Cat
      }

      val dogShop: PetShop[Dog] = catShop
      dogShop.get(true)   // EVIL CAT!
     */

    def get[S <: T](isItaPuppy: Boolean, defaultAnimal: S): S = defaultAnimal  // S subtype of T
  }

  val shop: PetShop[Dog] = new PetShop[Animal]
  // val evilCat = shop.get(true, new Cat)  // Will throw Error as it can only except subtypes of Dog
  class TerraNova extends Dog
  val bigFurry = shop.get(true, new TerraNova)

  /*
    Big rule
    - method arguments are in CONTRAVARIANT position
    - return types are in COVARIANT position
   */

  /**
   * Exercise -
   * 1. Invariant, covariant, contravariant
   * Parking[T](things: List[T]) {
   * park(vehicle: T)
   * impound(vehicles: List[T])
   * checkVehicles(conditions: String): List[T]
   * }
   *
   * 2. used someone else's API: IList[T]
   * 3. Parking = monad!
   *     - flatMap
   */
  class Vehicle

  class Bike extends Vehicle

  class Car extends Vehicle

  class IList[T]
  class IParking[T](vehicles: IList[T]) {
    def park(vehicle: T): IParking[T] = ???
    def impound(vehicle: T): IParking[T] = ???
    def checkVehicles(conditions: String): IList[T] = ???
    def flatMap[R](f: T => IParking[R]): IParking[R] = ???
  }

  class CParking[+T](vehicles: IList[T]) {
    def park[V >: T](vehicle: V): CParking[V] = ???
    def impound[V >: T](vehicle: V): CParking[V] = ???
    def checkVehicles[V >: T](conditions: String): IList[V] = ???
    def flatMap[R](f: T => CParking[R]): CParking[R] = ???
  }

  class XParking[-T](vehicles: IList[T]) {
    def park(vehicle: T): XParking[T] = ???
    def impound(vehicle: T): XParking[T] = ???
    def checkVehicles[V <: T](conditions: String): IList[V] = ???
    def flatMap[V <: T, R](f: V => XParking[R]): XParking[R] = ???
  }

  class CList[+T]
  class CIParking[T](vehicles: CList[T]){
    def park(vehicle: T): CIParking[T] = ???
    def impound(vehicle: T): CIParking[T] = ???
    def checkVehicles(conditions: String): CList[T] = ???
    def flatMap[R](f: T => CIParking[R]): CIParking[R] = ???
  }

  class CCParking[+T](vehicles: CList[T]) {
    def park[V >: T](vehicle: V): CCParking[V] = ???
    def impound[V >: T](vehicle: V): CCParking[V] = ???
    def checkVehicles(conditions: String): CList[T] = ???
    def flatMap[R](f: T => CCParking[R]): CCParking[R] = ???
  }

  class CXParking[-T](vehicles: CList[T]) {
    def park(vehicle: T): CXParking[T] = ???
    def impound(vehicle: T): CXParking[T] = ???
    def checkVehicles[V <: T](conditions: String): CList[V] = ???
    def flatMap[V <: T, R](f: V => CXParking[R]): CXParking[R] = ???
  }

  class ConList[-T]
  class IConParking[T](vehicles: ConList[T]) {
    def park(vehicle: T): IConParking[T] = ???
    def impound(vehicle: T): IConParking[T] = ???
    def checkVehicles(conditions: String): ConList[T] = ???
    def flatMap[R](f: T => IConParking[R]): IConParking[R] = ???
  }

  class CConParking[+T](vehicles: ConList[T]) {
    def park[V >: T](vehicle: V): CConParking[V] = ???
    def impound[V >: T](vehicle: V): CConParking[V] = ???
    def checkVehicles[V >: T](conditions: String): ConList[V] = ???
    def flatMap[R](f: T => CConParking[R]): CConParking[R] = ???
  }

  class XConParking[-T](vehicles: ConList[T]) {
    def park(vehicle: T): XConParking[T] = ???
    def impound(vehicle: T): XConParking[T] = ???
    def checkVehicles(conditions: String): ConList[T] = ???
    def flatMap[V <: T, R](f: V => XConParking[R]): XConParking[R] = ???
  }
  /*
    Rule of thumb
    - Use Covariance = Collection of Things
    - Use Contravariance = Group of Actions
   */
}
