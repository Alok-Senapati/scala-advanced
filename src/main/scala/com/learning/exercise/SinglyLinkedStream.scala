package com.learning.exercise

import scala.annotation.targetName

abstract class SinglyLinkedStream[+A] {
  def isEmpty: Boolean
  def head: A
  def tail: SinglyLinkedStream[A]

  @targetName("prepend")
  def #::[B >: A](element: B): SinglyLinkedStream[B] // prepend operator
  @targetName("concat")
  def ++[B >: A](anotherStream: SinglyLinkedStream[B]): SinglyLinkedStream[B] // concatenate

  def foreach(f: A => Unit): Unit
  def map[B](f: A => B): SinglyLinkedStream[B]
  def flatMap[B](f: A => SinglyLinkedStream[B]): SinglyLinkedStream[B]
  def filter(predicate: A => Boolean): SinglyLinkedStream[A]

  def take(n: Int): SinglyLinkedStream[A]  // Takes first n elements out of the stream
  def takeAsList(n: Int): List[A]
}

object SinglyLinkedStream {
  def from[A](x: A)(generator: A => A): SinglyLinkedStream[A] = {

  }
}

case object EmptyStream extends SinglyLinkedStream[Nothing] {
  override def isEmpty: Boolean = true
  override def head: Nothing = throw new RuntimeException("Empty stream has no head")
  override def tail: SinglyLinkedStream[Nothing] = throw new RuntimeException("Empty stream has no tail")

  @targetName("prepend")
  override def #::[B >: Nothing](element: B): SinglyLinkedStream[B] = new NonEmptyStream[B](element, this)
  @targetName("concat")
  override def ++[B >: Nothing](anotherStream: SinglyLinkedStream[B]): SinglyLinkedStream[B] = anotherStream

  override def foreach(f: Nothing => Unit): Unit = ()
  override def map[B](f: Nothing => B): SinglyLinkedStream[B] = EmptyStream
  override def flatMap[B](f: Nothing => SinglyLinkedStream[B]): SinglyLinkedStream[B] = EmptyStream
  override def filter(predicate: Nothing => Boolean): SinglyLinkedStream[Nothing] = EmptyStream
}

case class NonEmptyStream[+A](h: A, t: SinglyLinkedStream[A]) extends SinglyLinkedStream[A] {
  override def isEmpty: Boolean = false
  override def head: A = this.h
  override def tail: SinglyLinkedStream[A] = this.t

  @targetName("prepend")
  override def #::[B >: A](element: B): SinglyLinkedStream[B] = {
    ???
  }
  @targetName("concat")
  override def ++[B >: A](anotherStream: SinglyLinkedStream[B]): SinglyLinkedStream[B] = {
    ???
  }

  override def foreach(f: A => Unit): Unit = ???
  override def map[B](f: A => B): SinglyLinkedStream[B] = ???
  override def flatMap[B](f: A => SinglyLinkedStream[B]): SinglyLinkedStream[B] = ???
  override def filter(predicate: A => Boolean): SinglyLinkedStream[A] = ???
}

