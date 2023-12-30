package com.learning.type_system

import java.nio.ByteBuffer
import scala.collection.mutable

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


  // Both below method calls are valid because it expects a parameter of type Outer#InnerClass, which means InnerClass object of any Outer object
  o.printUniversal(new o.InnerClass)
  oo.printUniversal(new o.InnerClass)


  /*
    Example 1 - Types Key-Value Data Store
      Assume we have a key-value data store. All keys are String, but the ValueType of each Key may differ from others.
      We can encode the ValueType of each key-value in the Key type. When setting the key-value, we can encode the value
      with the proper encoder of the key.ValueType. Thus making key.ValueType a path-dependent type.
   */

  // First, let’s create an abstract class that contains the name of the key and the value type:
  abstract class Key(val name: String) {
    type ValueType
  }

  // Whenever we have an instance of Key, we can access the value type of that key by referencing the ValueType member.
  // Now let’s introduce two general operations of common key-value stores, set and get, in the Operations trait:
  trait Encoder[T] {
    def encode(t: T): Array[Byte]
  }

  trait Decoder[T] {
    def decode(d: Array[Byte]): T
  }

  trait Operations {
    def set(key: Key)(value: key.ValueType)(implicit enc: Encoder[key.ValueType]): Unit
    def get(key: Key)(implicit decoder: Decoder[key.ValueType]): Option[key.ValueType]
  }

  case class Database() extends Operations {
    private val db = mutable.Map.empty[String, Array[Byte]]
    override def get(key: Key)(implicit decoder: Decoder[key.ValueType]): Option[key.ValueType] = {
      db.get(key.name).map(v => decoder.decode(v))
    }

    override def set(key: Key)(value: key.ValueType)(implicit enc: Encoder[key.ValueType]): Unit = {
      db.update(key.name, enc.encode(value))
    }
  }

  object Database {
    def key[Data](v: String) = new Key(v) {
      override type ValueType = Data
    }
  }

  object Encoder {
    implicit val stringEncoder: Encoder[String] = (t: String) => t.getBytes()
    implicit val doubleEncoder: Encoder[Double] = (t: Double) => {
      val bytes = new Array[Byte](8)
      ByteBuffer.wrap(bytes).putDouble(t)
      bytes
    }
  }

  object Decoder {
    implicit val stringDecoder: Decoder[String] = (bytes: Array[Byte]) => new String(bytes)
    implicit val doubleDecoder: Decoder[Double] = (bytes: Array[Byte]) => ByteBuffer.wrap(bytes).getDouble
  }

  val db = Database()
  import Encoder.doubleEncoder
  val key = Database.key[Double]("key1")
  db.set(key)(20.0)
  println(db.get(key))


  /*
    Example 2 - Parental Award and Punishment Discipline
      Assume we want to model parental disciplines of punishment and reward. All parents can reward any child,
      but they can’t punish the children of others
   */
  case class Parent(name: String) {
    class Child
    def child = new this.Child
    def punish(c: this.Child): Unit = println(s"$name is punishing $c")
    def reward(c: Parent#Child): Unit = println(s"$name is rewarding $c")
  }

  val p1 = Parent("John")
  val p2 = Parent("Marry")
  p1.punish(p1.child)
  // p1.punish(p2.child)  // Won't compile as it expects p1.child
  p2.reward(p1.child)  // Will compile
  p2.reward(p2.child)

}
