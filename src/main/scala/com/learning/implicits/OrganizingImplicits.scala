package com.learning.implicits

object OrganizingImplicits extends App {
  // List Ordering
  println(List(3, 2, 4, 1, 6, 7, 5).sorted)  // sorted takes a implicit Ordering value
  // The default Ordering is present in scala.Predef
  // We can create our own Ordering implicit value
  implicit val reverseOrder: Ordering[Int] = Ordering.fromLessThan(_ > _)
  println(List(3, 2, 4, 1, 6, 7, 5).sorted)  // The sorted method takes reverseOrder instead of default Ordering
  // Here we can see that the reverseOrder implicit val takes precedence than the default ordering defined in scala.PreDef

  /*
    Implicits can be
      - var/val
      - object
      - accessor methods (def without parentheses)
  */

  // Exercise - Custom Ordering for Person object
  case class Person(name: String, age: Int)

  val persons = List(
    Person("Matt", 17),
    Person("Rime", 20),
    Person("Ryan", 35),
    Person("Arnold", 25)
  )

  // Alphabetical Ordering
  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((p1, p2) => p1.name < p2.name)
  }

  println(persons.sorted)


  /*
    Implicit Scopes and Priority -
      - Normal Scope/Local Scope
      - Imported Scope
      - Companions of all types of the method signature
      - For Example a method with implicit val def sorted[B >: A](implicit ord: Ordering[B]): List[B]
        - The compiler will check for required implicits in PreDef, Ordering, List, Companion object of B
        - If the required implicit is not found in scope then will throw an error

    Best Practice -
      - When defining implicit val:
      1)
        - If there is a single possible value for it
        - and you can edit the code for the type
        Then define the implicit in the companion
      2)
        - If there are many possible values for it
        - but a single good one
        - and you can edit the code for that type
        Then define the good implicit in the companion
      3)
        - If there are multiple possible values for it
        - All are equally important
        Create different objects for all of those implicit values and import object._
        Note - Multiple objects having same implicits for a function cannot be imported or else will result in ambiguity
    */

  object AlphabeticOrdering {
    implicit val alphabeticOrdering: Ordering[Person] = Ordering.fromLessThan((p1, p2) => p1.name.compareTo(p2.name) < 0)
  }

  object AgeWiseOrdering {
    implicit val ageWiseOrdering: Ordering[Person] = Ordering.fromLessThan((p1, p2) => p1.age < p2.age)
  }

  // For AlphabeticOrdering
  // import AlphabeticOrdering._
  // println(persons.sorted)

  // For Age-wise Ordering
  import AgeWiseOrdering._
  println(persons.sorted)

  /*
    Exercise -
      Define Ordering for Purchase
      - totalPrice = most used in code (50%)
      - by unit count = 25%
      - by unit price = 25%

     */
  case class Purchase(nUnits: Int, unitPrice: Double)

  object Purchase {
    implicit val orderByTotalPrice: Ordering[Purchase] = Ordering.fromLessThan((p1, p2) =>
      (p1.nUnits * p1.unitPrice) < (p2.nUnits * p2.unitPrice))
  }

  object OrderByUnitCount {
    implicit val orderPurchaseByUnitCount: Ordering[Purchase] = Ordering.fromLessThan((p1, p2) => p1.nUnits < p2.nUnits)
  }

  object OrderByUnitPrice {
    implicit val orderPurchaseByUnitPrice: Ordering[Purchase] = Ordering.fromLessThan((p1, p2) => p1.unitPrice < p2.unitPrice)
  }

  val purchases = List(
    Purchase(20, 100),
    Purchase(22, 530),
    Purchase(51, 960),
    Purchase(96, 59)
  )
  println(purchases.sorted)

  // import OrderByUnitCount._
  // println(purchases.sorted)

  import OrderByUnitPrice._
  println(purchases.sorted)


}
