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
      - accessor methods (defs without parentheses)
  */

  // Exercise - Custom Ordering for Person object
  case class Person(name: String, age: Int)

  val persons = List(
    Person("Matt", 23),
    Person("Rime", 36),
    Person("Ryan", 45),
    Person("Arnold", 19)
  )
  // Alphabetical Ordering
  implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((p1, p2) => p1.name < p2.name)

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
      - When defining implicit vals:
      1)
        - If there is a single possible value for it
        - and you can edit the code for the type
        Then define the implicit in the companion
      2)
        - If there are many possible values for it
        - but a single good one
        - and you can edit the code for that type
        Then define the good implicit in the companion

    */

}
