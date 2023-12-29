package com.learning.type_system

object PathDependentTypes extends App {
  /*
    Inner Types -
      - Inner Types are the type of classes, objects and type defined inside a class
  */

  // We can define classes, objects and types inside another class
  // These InnerTypes are specific to instances and not class
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

  // These are called Path Dependent Types


  /*
      Exercise
      DB keyed by Int or String, but maybe others
     */

  /*
    use path-dependent types
    abstract type members and/or type aliases
   */
  trait ItemLike {
    type Key
  }
  trait Item[K] extends ItemLike {
    override type Key = K
  }
  trait IntItem extends Item[Int]
  trait StringItem extends Item[String]

  def get[ItemType <: ItemLike](key: ItemType#Key): ItemType = ???

  get[IntItem](3)
  get[StringItem]("apple")
  // get[IntItem]("orange")  // InCorrect because IntType has Key type as Int


}
