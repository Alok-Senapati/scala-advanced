package com.learning.concurrency

object JVMConcurrencyProblems extends App {
  // Using Mutable Variables - Race Condition
  def runInParallel(): Unit = {
    var x = 1
    val thread1 = new Thread(() => {
      x *= 10
    })
    val thread2 = new Thread(() => {
      x *= 2
    })
    thread1.start()
    thread2.start()
    println(x)  // Every run might produce different result
    // As both the thread runs parallelly. The value of x may or may not be updated when the other thread computes the value of x
  }
  runInParallel()

  case class BankAccount(var amount: Int)

  def buy(account: BankAccount, product: String, price: Int): Unit = {
    account.amount -= price
  }

  def safeBuy(account: BankAccount, product: String, price: Int): Unit = {
    account.synchronized { // synchronized do not allow multiple threads to run the critical section at the same time
      account.amount -= price
    }
  }

  def demoBankingApplication(): Unit = {
    (1 to 10000).foreach(x => {
      val account = BankAccount(50000)
      val thread1 = new Thread(() => buy(account, "Shoe", 3000))
      val thread2 = new Thread(() => buy(account, "IPhone", 4000))
      thread1.start()
      thread2.start()
      thread1.join()
      thread2.join()
      if (account.amount != 43000) {println(s"Amount mismatch: ${account.amount}")}
    })
  }
  def demoSafeBankingApplication(): Unit = {
    (1 to 10000).foreach(x => {
      val account = BankAccount(50000)
      val thread1 = new Thread(() => safeBuy(account, "Shoe", 3000))
      val thread2 = new Thread(() => safeBuy(account, "IPhone", 4000))
      thread1.start()
      thread2.start()
      thread1.join()
      thread2.join()
      if (account.amount != 43000) {println(s"Amount mismatch in safeBuy: ${account.amount}")}
    })
  }
  demoBankingApplication()
  demoSafeBankingApplication()

  /**
   * Exercises
   *  1 - create "inception threads"
   *    thread 1
   *      -> thread 2
   *        -> thread 3
   *          ....
   *    each thread prints "hello from thread $i"
   *    Print all messages IN REVERSE ORDER
   *
   * 2 - What's the max/min value of x?
   * 3 - "sleep fallacy": what's the value of message?
   */

  def inceptionThreads(maxThreads: Int, n: Int = 1): Thread = {
    new Thread(() => {
      if n != maxThreads then {
        val newThread = inceptionThreads(maxThreads, n + 1)
        newThread.start()
        newThread.join()
      }
      println(s"Hello From Thread ${n}")
    })
  }

  inceptionThreads(50).start()

  // 2
  /*
    max value = 100 - each thread increases x by 1
    min value = 1
      all threads read x = 0 at the same time
      all threads (in parallel) compute 0 + 1 = 1
      all threads try to write x = 1
   */
  def minMaxX(): Unit = {
    var x = 0
    val threads = (1 to 100).map(_ => new Thread(() => x += 1))
    threads.foreach(_.start())
  }


  // 3
  /*
      Note - "yielding" refers to the act of voluntarily giving up the current thread's control of the CPU.
      When a thread yields, it allows other threads to execute in its place.
      almost always, message = "Scala is awesome"
      is it guaranteed? NO
      Obnoxious situation (possible):

      main thread:
        message = "Scala sucks"
        awesomeThread.start()
        sleep(1001) - yields execution
      awesome thread:
        sleep(1000) - yields execution
        OS gives the CPU to some important thread, takes > 2s
      OS gives the CPU back to the main thread
      main thread:
        println(message) // "Scala sucks"
      awesome thread:
        message = "Scala is awesome"
     */
  def demoSleepFallacy(): Unit = {
    var message = ""
    val awesomeThread = new Thread(() => {
      Thread.sleep(1000)
      message = "Scala is awesome"
    })
    message = "Scala Sucks"
    awesomeThread.start()
    Thread.sleep(1001)
    // Solution
    awesomeThread.join()
    println(message)
  }
  demoSleepFallacy()
}
