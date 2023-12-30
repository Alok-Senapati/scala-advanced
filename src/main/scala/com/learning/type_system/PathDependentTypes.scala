package com.learning.type_system

object PathDependentTypes extends App {
  /*
    Inner Types -
      - Inner Types are the type of classes, objects and type defined inside a class

    Path Dependent Types -
      - A dependent type is a type whose definition depends on a value.
      - Suppose we can write a List that has a constraint on the size of the List,
        for example, NonEmptyList. Also, assume another type like AtLeastTwoMemberList.
      - A path-dependent type is a specific kind of dependent type where the dependent-upon value is a path.
      - Scala has a notion of a type dependent on a value. This dependency is not expressed in the type signature but rather in the type placement.

   */

  // We can define classes, objects and types inside another class
  // These InnerTypes are specific to instances and not class
  trait Input {
    type Output
    val value: Output
  }
  // Here the Input trait has the Output type member. The type of value is Output, which is path-dependent.
  // This means that the type of value varies based on the implementation of the Input trait.


  // Dependent types can be used as type parameters. The following function is a dependent function because the output of our function is dependent on its input:
  def dependentFunc(i: Input): i.Output = i.value

  def valueOf[T](v: T) = new Input {
    type Output = T
    val value: T = v
  }

  val intValue = valueOf(1)
  val stringValue = valueOf("One")

  assert(dependentFunc(intValue) == 1)
  assert(dependentFunc(stringValue) == "One")


  // In below code, The type of InnerClass, InnerObject and InnerType is bound to the Outer object, making them type path-dependent type
  // because their type is dependent on a value (any instance of Outer class)
  class Outer {
    class InnerClass
    object InnerObject
    type InnerType
    def print(i: InnerClass): Unit = {
      println(i)
    }

    // Outer#InnerClass is a universal type
    def printUniversal(i: Outer#InnerClass): Unit = {
      println(i)
    }
  }

  val o = new Outer
  // val oInnerClass = Outer.Inner  // Incorrect
  // val oInnerClass = new Outer.Inner  // Incorrect
  // The correct declarations are
  val oInnerClass = new o.InnerClass
  val oInnerObject = o.InnerObject
  type oInnerType = o.InnerType

  val oo = new Outer
  // val ooInnerClass: oo.InnerClass = new o.InnerClass  // Compilation Error - new o.InnerClass is of object o while ooInnerClass is of type InnerClass of object oo


  o.print(new o.InnerClass)  // Valid Method Call
  // oo.print(new o.InnerClass)  // Invalid as it expects parameter of type oo.InnerClass


  // Both below mehod calls are valid because it expects a parameter of type Outer#InnerClass, which means InnerClass object of any Outer object
  o.printUniversal(new o.InnerClass)
  oo.printUniversal(new o.InnerClass)
//
//
//  /*
//      Exercise
//      DB keyed by Int or String, but maybe others
//     */
//
//  /*
//    use path-dependent types
//    abstract type members and/or type aliases
//   */
//  trait ItemLike {
//    type Key
//  }
//  trait Item[K] extends ItemLike {
//    override type Key = K
//  }
//  trait IntItem extends Item[Int]
//  trait StringItem extends Item[String]
//
//  def get[ItemType <: ItemLike](key: ItemLike#Key): ItemType = ???
//
//  get[IntItem](3)
//  get[StringItem]("apple")
//  // get[IntItem]("orange")  // InCorrect because IntType has Key type as Int
//

}
