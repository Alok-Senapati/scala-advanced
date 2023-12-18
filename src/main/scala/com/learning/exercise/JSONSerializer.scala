package com.learning.exercise

import java.util.Date

object JSONSerializer extends App {
  /*
    Exercise ro implement a JSONSerializer for a Social Media Scenario
    Data Structures - Users, Posts, Feeds
  */

  case class User(name: String, age: Int, email: String)
  case class Post(content: String, createdAt: Date)
  case class Feed(user: User, posts: List[Post])

  /*
    We can solve using 3 steps
      - Define Intermediate types from where we can serialize
      - Type Classes and Type Instances for converting to Intermediate Classes from Int, String, List, Date etc.
      - Serialize to JSON
   */

  trait JSONValue {
    def stringify: String
  }

  final case class JSONInt(value: Int) extends JSONValue {
    override def stringify: String = value.toString
  }

  final case class JSONString(value: String) extends JSONValue {
    override def stringify: String = s"\"$value\""
  }

  final case class JSONDate(value: Date) extends JSONValue {
    override def stringify: String = value.toString
  }

  final case class JSONArray(value: List[JSONValue]) extends JSONValue {
    override def stringify: String = value.map(_.stringify).mkString("[ ", ", ", " ]")
  }

  final case class JSONObject(value: Map[String, JSONValue]) extends JSONValue {
    override def stringify: String = value.map {
      case (key, value) => s"\"$key\": ${value.stringify}"
    }.mkString("{", ", ", "}")
  }

  val data = JSONObject(Map(
    "user" -> JSONString("Harry"),
    "posts" -> JSONArray(List(
      JSONString("Scala Rocks!!!"),
      JSONInt(656)
    ))
  ))

  println(data.stringify)



  trait JSONConverter[T] {
    def convert(value: T): JSONValue
  }

  implicit object IntConverter extends JSONConverter[Int] {
    override def convert(value: Int): JSONValue = JSONInt(value)
  }

  implicit object StringConverter extends JSONConverter[String] {
    override def convert(value: String): JSONValue = JSONString(value)
  }

  implicit object UserConverter extends JSONConverter[User] {
    override def convert(value: User): JSONValue = JSONObject(Map(
      "name" -> JSONString(value.name),
      "age" -> JSONInt(value.age),
      "email" -> JSONString(value.email)
    ))
  }

  implicit object PostConverter extends JSONConverter[Post] {
    override def convert(value: Post): JSONValue = JSONObject(Map(
      "content" -> JSONString(value.content),
      "createdAt" -> JSONDate(value.createdAt)
    ))
  }

  implicit object FeedConverter extends JSONConverter[Feed] {
    override def convert(value: Feed): JSONValue = JSONObject(Map(
      "user" -> value.user.toJSON,
      "posts" -> JSONArray(value.posts.map(_.toJSON))
    ))
  }

  // Below Implicit class will implicitly take required converter to convert any Type to JSONValue
  implicit class JSONOps[T](value: T) {
    def toJSON(implicit converter: JSONConverter[T]): JSONValue = converter.convert(value)
  }

  val now = new Date(System.currentTimeMillis())
  val john = User("John", 34, "john@gmail.com")
  val feed = Feed(john, List(
    Post("Hello", now),
    Post("Scala is Awesome", now)
  ))

  println(feed.toJSON.stringify)
}
