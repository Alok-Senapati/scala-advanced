package com.learning.implicits

import java.sql.Date

object TypeClasses extends App {
  /*
    TypeClasses traits that takes a Type and describe what operations can be performed on the Type
      E.g. - Ordering
   */
  // Let's implement a HTMLWritable to perform server side rendering
  trait HTMLWritable {
    def toHTML: String
  }

  // Let us assume we have a case class representing User
  case class User(name: String, age: Int, email: String) extends HTMLWritable {
    override def toHTML: String = s"<div>Name: $name, age: $age, email: <a href=$email /></div>"
  }

  val john = User("John", 24, "john_jacob@gmail.com")
  println(john.toHTML)

  /*
    Disadvantages of the above approach -
      1. Can be only used for the types we write. For example, here the toHTML method can be called for User object only
      2. Provides only one implementation, E.g. - We can't use it if we want to display only the name if the user is not logged in.
          To achieve this, either we need to provide multiple implementation of User or toHTML method
   */

  // Another option is to use pattern matching
  object HTMLSerializerPM {
    def serializeToHTML(value: Any): String = value match {
      case User(n, a, e) => s"<div>Name: $n, age: $a, email: <a href=$e /></div>"
      case x: Int => s"<div>$x</div>"
      case _ => "<div>No matching case</div>"
    }
  }
  println(HTMLSerializerPM.serializeToHTML(john))
  println(HTMLSerializerPM.serializeToHTML(42))
  println(HTMLSerializerPM.serializeToHTML("Something"))

  /*
    Disadvantages of above approach -
      1. Loses type safety. As value if of Any type
      2. Provides only one implementation.
      3. Need to modify the code everytime addition of a new Type
   */

  // Better Approach
  trait HTMLSerializer[T] {  // Type Class
    def serialize(value: T): String
  }

  object UserSerializer extends HTMLSerializer[User] {
    override def serialize(user: User): String = s"<div>Name: ${user.name}, age: ${user.age}, email: <a href=${user.email} /></div>"
  }


  // Advantage - 1: Multiple Implementation Can be defined
  implicit object PartialUserSerializer extends HTMLSerializer[User] {
    override def serialize(user: User): String = s"<div>Name: ${user.name}</div>"
  }

  // Advantage - 2: Can be implemented for any type
  implicit object IntSerializer extends HTMLSerializer[Int] {
    override def serialize(n: Int): String = s"<div>$n</div>"
  }

  // Advantage - 3: Retains Type Safety

  // In the above approach, HTMLSerializer is called a Type Class and UserSerializer, PartialUserSerializer and
  // IntSerializer are called Type Class Instances and often defined using singleton objects

  println(HTMLSerializerPM.serializeToHTML(john))
  println(HTMLSerializerPM.serializeToHTML(42))

  // Adding power of implicits with Type Classes
  object HTMLSerializer {
    def serialize[T](value: T)(implicit serializer: HTMLSerializer[T]): String = serializer.serialize(value)

    // By implementing this we can use all method of the serializer unlike the serialize method which uses only the serializer.serialize
    def apply[T](implicit serializer: HTMLSerializer[T]): HTMLSerializer[T] = serializer
  }

  // Now we can let the compiler figure out the serializer that need to be taken using implicits,
  // We just need to define the scope of the Type Class Instances Properly
  println(HTMLSerializer.serialize(42))  // Takes the implicit IntSerializer
  println(HTMLSerializer.serialize(john))  // Takes the implicit UserSerializer

  // Using apply
  println(HTMLSerializer[User].serialize(john))
  /*
    Exercise - 1: Implement Equality type class and use implicits
   */
  trait Equal[T] {
    def apply(v1: T, v2: T): Boolean
  }

  // UserEquality based on Name
  implicit object EqualUserName extends Equal[User] {
    override def apply(v1: User, v2: User): Boolean = v1.name.equals(v2.name)
  }

  // UserEquality based on email
  object EqualUserEmail extends Equal[User] {
    override def apply(v1: User, v2: User): Boolean = v1.email.equals(v2.email)
  }

  // IntEquality
  implicit object IntegerEqual extends Equal[Int] {
    override def apply(v1: Int, v2: Int): Boolean = v1 == v2
  }

  // Equality Implicit
  object Equal {
    def apply[T](a: T, b: T)(implicit equalizer: Equal[T]): Boolean = equalizer(a, b)
  }

  val anotherJohn = User("John", 43, "john_potter@gmail.com")
  println(Equal(john, anotherJohn))  // Takes the implicit EqualUserName
  println(Equal(42, 43))

}
