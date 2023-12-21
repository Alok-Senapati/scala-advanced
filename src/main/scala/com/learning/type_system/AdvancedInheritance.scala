package com.learning.type_system

object AdvancedInheritance extends App {
  // Convenience
  trait Writer[T] {
    def write(value: T): Unit
  }
  trait Closeable {
    def close(status: Int): Unit
  }
  trait GenericStream[T] {
    def foreach(f: T => Unit): Unit
  }

  // In the below method stream is a Type of GenericStream[T] with Closeable with Writer[T]
  // That means stream inherits GenericStream[T] with Closeable with Writer[T] and hence can use all the method inside them
  def processStream[T](stream: GenericStream[T] with Closeable with Writer[T]): Unit = {
    stream.foreach(println)
    stream.close(0)
  }

  // Diamond Problem
  // Scala Internally forms a Inheritance Lineage/Type Linearization
  /*
    Animal -> AnyRef with Animal{}
    Lion -> AnyRef with Animal with Lion{}
    Tiger -> AnyRef with Animal with Tiger{}
    Liger -> AnyRef with (AnyRef with Animal with Lion{}) with (AnyRef with Animal with Tiger{}) with Liger
          // Removes 2nd occurrence of parent class
          -> AnyRef with Animal with Lion with Tiger with Liger
    => As Liger does not have the name method implemented, The compiler will go to the left in the Inheritance chain
      and executes the name method of Tiger
  */
  trait Animal {
    def name: String
  }
  private trait Lion extends Animal {
    override def name: String = "Lion"
  }
  private trait Tiger extends Animal {
    override def name: String = "Tiger"
  }
  private class Liger extends Lion with Tiger
  println(new Liger().name)



  // super() in diamond Problem with Type Linearization
  trait Cold {
    def print: Unit = println("Cold")
  }
  trait Green extends Cold {
    override def print: Unit = {
      println("Green")
      super.print
    }
  }
  trait Blue extends Cold {
    override def print: Unit = {
      println("Blue")
      super.print
    }
  }
  trait Red {
    def print: Unit = println("Red")
  }

  class White extends Red with Blue with Green {
    override def print: Unit = {
      println("White")
      super.print
    }
  }

  /*
    Cold -> AnyRef with Cold {}
    Green -> AnyRef with Cold with Green {}
    Blue -> AnyRef with Cold with Blue {}
    Red -> AnyRef with Red {}
    White -> Red with Blue with Green with White {}
          -> (AnyRef with Red{}) with (AnyRef with Cold with Blue {}) with (AnyRef with Cold with Green {}) with White {}
          // Removes 2nd occurrence of parent class
          -> AnyRef with Red with Cold with Blue with Green with White{}

    => Executes White.print -> Green.print -> Blue.print -> Cold.print
    => Ignores Red.print as Cold.print does not have super.print
    */

  new White().print

}
