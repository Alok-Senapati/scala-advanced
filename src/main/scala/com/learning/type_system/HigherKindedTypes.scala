package com.learning.type_system

import com.learning.type_system.HigherKindedTypes.{MonadList, MonadOption}

object HigherKindedTypes extends App {
  /*
    Higher Kinded Types -
    - A higher-kinded type is a type that abstracts over some type that, in turn, abstracts over another type.
    - It’s a way to generically abstract over entities that take type constructors.
    - They allow us to write modules that can work with a wide range of objects.
   */

  trait Collection[F[_]] {
    def wrap[A](x: A): F[A]
    def first[B](x: F[B]): B
  }

  // We just defined Collection, a parameterized interface that takes a type T as a parameter;
  // T, in turn, takes another type as a parameter. So, T[_] implies a type T of type _ (anything).
  // We can as well define the trait as below and still have the same result. But, using _ makes it obvious that T accepts any type.
  trait Collection1[F[Z]] {
    // Definition
  }

  var collection = new Collection[List] {
    override def wrap[A](a: A): List[A] = List(a)

    override def first[B](b: List[B]): B = b.head
  }


  /*
    UseCases -
    1. Library Design and Implementation
      - Most of the use cases of higher-kinded types are found in library design and implementation.
      - It provides the client more control over the exposed interfaces, while reducing code duplication.
      - Scalaz, one of the most popular Scala projects, uses higher-kinded types to extend the core Scala library for functional programming.

    2. Polymorphic Containers
      - One use case of higher-kinded types is in creating polymorphic containers.
      - Higher-kinded types are useful when we want to create a container that can hold any type of items;
      - we don’t need a different type for each specific content type.
      - As we already saw, Collection (in our previous example) allows various entity types.

    3. Building Data Pipelines
      - Data engineering involves reading, transforming and writing varieties of data.
      - As the data variety and volume increase, the processes involved also increase.
      - If we want to design a pipeline for the extraction, transformation and loading (ETL) of data,
        we probably want a framework that can work on different types of datasets
   */

  /*
    Exercise
   */
  trait Monad[F[_], A] {
    def flatMap[B](f: A => F[B]): F[B]
    def map[B](f: A => B): F[B]
  }

  // Making it below classes Implicit let's the compiler implicitly convert List/Option to MonadList and MonadOption
  implicit class MonadList[A](list: List[A]) extends Monad[List, A] {
    override def flatMap[B](f: A => List[B]): List[B] = list.flatMap(f)
    override def map[B](f: A => B): List[B] = list.map(f)
  }

  implicit class MonadOption[A](option: Option[A]) extends Monad[Option, A] {
    override def flatMap[B](f: A => Option[B]): Option[B] = option.flatMap(f)
    override def map[B](f: A => B): Option[B] = option.map(f)
  }

  private def multiply[F[_], A, B](ma: Monad[F, A], mb: Monad[F, B]): F[(A, B)] = {
    for {
      i <- ma
      j <- mb
    } yield i -> j
  }

  println(multiply(MonadList(List(1, 2)), MonadList(List("a", "b"))))
  println(multiply(MonadOption(Some(4)), MonadOption(Some("z"))))

  println(multiply(List(1, 2), List("a", "b")))
  println(multiply(Some(4), Some("z")))
}
