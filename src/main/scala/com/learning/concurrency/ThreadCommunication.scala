package com.learning.concurrency

import scala.util.Random

object ThreadCommunication extends App {

  /**
   * Producer - Consumer Problem
   *  Suppose we have two threads
   *  - Producer Thread - Produces some data to a container
   *  - Consumer Thread - Consumes the data produced by Producer from the container
   *  How the communication will work?
   */

  class SimpleContainer {
    private var value = 0
    def isEmpty: Boolean = value == 0  // If x == default value then returns true
    def get: Int = {
      val result = value
      value = 0
      result
    }
    def set(x: Int): Unit = {
      value = x
    }
  }


  // Busy Waiting as the consumer waits till the producer produces the data
  def naiveProdCons(): Unit = {
    val container = new SimpleContainer

    val consumerThread = new Thread(() => {
      println("[consumer] waiting")
      while (container.isEmpty) {
        println("[consumer] still waiting...")
      }
      println(s"[consumer] I have consumed value ${container.get}")
    })

    val producerThread = new Thread(() => {
      println("[producer] computing value...")
      val x: Int = Random.nextInt()
      Thread.sleep(500)
      println(s"[producer] after long computation producing value $x to the container")
      container.set(x)
    })

    producerThread.start()
    consumerThread.start()
  }

  naiveProdCons()
  Thread.sleep(1000)
  println("-----------------------------------------------------------------------------")
  /**
   * Synchronized -
   *  - Entering a synchronized expression on an object locks the object from other threads.
   *  - JVM Internally uses a Monitor Data Structure to keep track of the locks
   *  - After the block is completed executing the lock is released so that the object is now available for other threads
   *  - Only AnyRefs can have synchronized blocks
   *  - General Principals
   *    - Do not make any assumption on who gets the lock first
   *    - Keep locking to the minimum
   *    - Make thread safety at all times in parallel applications
   *
   * wait() -
   *  - waiting on an object's monitor suspend the thread indefinitely
   *          someObject.synchronized {  <-- Locks the object's monitor
   *            // Code Part 1
   *            someObject.wait()        <-- Releases the lock and wait..
   *            // Code Part 2           <-- When allowed to proceed locks the monitor again and continue
   *          }                          <-- Releases the lock
   * notify() -
   *  - notifies only a single waiting thread that they may continue
   *  - no guaranty which thread will be notified
   *  - to notify all waiting threads use notifyAll()
   *          someObject.synchronized {  <-- Locks the object's monitor
   *            // Code Part 1
   *            someObject.notify()      <-- Notifies only ONE single thread that they may proceed
   *            // Code Part 2           <-- When allowed to proceed locks the monitor again and continue
   *          }                          <-- Releases the lock
   */

  def smartProdCons(): Unit = {
    val container = new SimpleContainer
    val consumerThread = new Thread(() => {
      println("[smartConsumer] waiting for data")
      container.synchronized {
        container.wait()
      }
      println(s"[smartConsumer] I have consumed value ${container.get}")
    })

    val producerThread = new Thread(() => {
      println("[smartProducer] computing value...")
      val x: Int = Random.nextInt()
      Thread.sleep(500)
      container.synchronized {
        println(s"[smartProducer] after long computation producing value $x to the container")
        container.set(x)
        container.notify()
      }
    })

    consumerThread.start()
    producerThread.start()
  }
  smartProdCons()


   /*
      Exercises.
        1. think of an example where notifyALL acts in a different way than notify ?
        2. create a deadlock -> Threads completely blocks by each other
        3. create a livelock -> Threads are not completely blocked by each other but they are unable to proceed from a step as they are yielding execution to each other
  */

  // notifyall
  def testNotifyAll(): Unit = {
    val bell = new Object

    (1 to 10).foreach(i => new Thread(() => {
      bell.synchronized {
        println(s"[thread $i] waiting...")
        bell.wait()
        println(s"[thread $i] hooray!")
      }
    }).start())

    new Thread(() => {
      Thread.sleep(2000)
      println("[announcer] Rock'n roll!")
      bell.synchronized {
        bell.notifyAll()
      }
    }).start()
  }

  testNotifyAll()

  // 2 - deadlock
  case class Friend(name: String) {
    def bow(other: Friend) = {
      this.synchronized {
        println(s"$this: I am bowing to my friend $other")
        other.rise(this)
        println(s"$this: my friend $other has risen")
      }
    }

    def rise(other: Friend) = {
      this.synchronized {
        println(s"$this: I am rising to my friend $other")
      }
    }

    var side = "right"

    def switchSide(): Unit = {
      if (side == "right") side = "left"
      else side = "right"
    }

    def pass(other: Friend): Unit = {
      while (this.side == other.side) {
        println(s"$this: Oh, but please, $other, feel free to pass...")
        switchSide()
        Thread.sleep(1000)
      }
    }
  }

  val sam = Friend("Sam")
  val pierre = Friend("Pierre")

  //  new Thread(() => sam.bow(pierre)).start() // sam's lock,    |  then pierre's lock
  //  new Thread(() => pierre.bow(sam)).start() // pierre's lock  |  then sam's lock

  // 3 - livelock
  //  new Thread(() => sam.pass(pierre)).start()
  //  new Thread(() => pierre.pass(sam)).start()

}
