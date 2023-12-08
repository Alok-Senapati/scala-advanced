package com.learning.concurrency

import java.util.concurrent.Executors

object JVMConcurrencyIntro extends App {
  // Using Java Classes
  // Thread constructor takes an instance of Runnable
  // Runnable is an Interface with abstract run method
  val aThread = new Thread(new Runnable {
    override def run(): Unit = {
      println("JVM Concurrency")
    }
  })
  aThread.start()  // Runs the code inside run in another thread

  // As the Runnable Interface has only one abstract method, we can use single abstract method syntactic sugar
  val anotherThread = new Thread(() => println("JVM Concurrency with Single Abstract Method Syntax Sugar"))
  anotherThread.start()  // Runs in a new thread

  val helloThread = new Thread(() => (1 to 5).foreach(x => println("hello")))
  val goodbyeThread = new Thread(() => (1 to 5).foreach(x => println("goodbye")))
  helloThread.start()
  goodbyeThread.start()
  // Even when the goodbyeThread started later. the print statement will executed at different sequence in every run as
  // helloThread and goodbyeThread runs in different threads

  // Executors - Helps creating a pool of threads
  val pool = Executors.newFixedThreadPool(10)
  pool.execute(() => println("This is running in a thread pool")) // takes a Runnable

  pool.execute(() => {
    Thread.sleep(1000)
    println("Done Running Code - 1 in Thread Pool")
  })

  pool.execute(() => {
    Thread.sleep(1000)
    println("Almost Done")
    Thread.sleep(1000)
    println("Done Running Code - 2 in Thread Pool")
  })

  pool.shutdown()  // Shuts down the pool. No further action can be run on the pool
  // pool.execute(() => println("Thread Pool is shutdown"))  // Will throw an RuntimeException -  java.util.concurrent.RejectedExecutionException
  // pool.shutdownNow()  // Attempts to stop all actively executing tasks, halts the processing of waiting tasks, and returns a list of the tasks that were awaiting execution.
  // This method does not wait for actively executing tasks to terminate.
}
