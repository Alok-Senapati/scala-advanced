package com.learning.concurrency

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Random, Success, Try}
import scala.concurrent.duration.Duration
import scala.concurrent.duration.*

object FuturesAndPromises extends App {
  /*
    The Future and Promise are two high-level asynchronous constructs in concurrent programming.

    1. Futures -
      - The Future is a read-only placeholder for the result of ongoing computation.
      - It acts as a proxy for an actual value that does not yet exist.
      - Think of IO-Bound or CPU-Bound operations, which take a notable time to complete.
      - To create asynchronous computation, we can put our computation inside the apply function of the Future:
      - To run the future, we need an ExecutionContext, which allows us to separate our business logic (the code) from the execution environment.
      - Because the ExecutionContext is an implicit parameter, we can import or create an ExecutionContext and mark it as implicit in our scope.
      - Future has two stages:
        - Not Completed: The computation is not completed yet.
        - Completed: The computation is completed, leaving the result in one of two states. If the computation results in a value,
          it’s considered a success, and if it results in an exception, it’s considered a failure. Until the result of the ongoing
          computation is ready, the state of the Future is not completed (datatype - Option, value - None), and after that, the Future
          is in either the success or failure state.
      - We can get the value of the future using
          - Using the onComplete callback
          - Blocking the Future by using Await.result() or Await.ready() (Non Recommended)
      - The Future has the onComplete() method. It gets the f: Try[T] => U as a callback, which let us decide what to do in each state
        of the completion stage

    2. Promises -
      - While futures are defined as a type of read-only placeholder object created for a result which doesn’t yet exist,
        a promise can be thought of as a writeable, single-assignment container, which completes a future.
      - That is, a promise can be used to successfully complete a future with a value (by “completing” the promise) using the success method.
      - Conversely, a promise can also be used to complete a future with an exception, by failing the promise, using the failure method.
   */

  // Scala Futures provides a Functional way to compute something on parallel or on another thread
  def getIntAfterSleep(): Int = {
    Thread.sleep(2000)
    Random.nextInt(250)
  }

  // Below computes getIntAfterSleep() in another thread
  // This calls the apply method of Future object which returns a Future
  // The apply method also takes a implicit ExecutorContext which needs to be passed. Implicits will be discussed in the Implicits section
  // We can import scala.concurrent.ExecutionContext.Implicits.global which will provide a global context
  // A context handles the thread allocation to a Future
  val aFuture: Future[Int] = Future {
    getIntAfterSleep()
  }  // (global) Will be passed by the compiler
  println(aFuture.value)  // Returns value of type Option[Try[Int]] where Int denotes the output type of the computation
  // Try denotes that there might be some Success or Failure while performing the Computation
  // Option denotes that the computation may or may not be completed yet

  // onComplete takes a Callback Lambda to be executed once the computation is complete
  // onComplete returns Unit
  // The lambda function takes input of type Try[T] as the computation may have returned in Success or Failure
  aFuture.onComplete(t => t match {
    case Success(value) => println(s"Value of the computation is $value")
    case Failure(exception) => println(s"Computation failed with exception $exception")
  })

  // The above can also be written as partial function
  /*
  aFuture.onComplete {
    case Success(value) => println(s"Value of the computation is $value")
    case Failure(exception) => println(s"Computation failed with exception $exception")
  }*/


  // Example of using Futures - SocialMedia
  case class Profile(id: String, name: String) {
    def poke(friend: Profile): Unit = println(s"${this.name} pokes ${friend.name}")
  }

  object SocialNetwork {
    // Database
    val userNames = Map(
      "fb.id.1.jack" -> "Jack",
      "fb.id.2.bill" -> "Bill",
      "fb.id.3.mary" -> "Mary",
      "fb.id.4.harry" -> "Harry",
      "fb.id.5.dummy" -> "Dummy"
    )

    val friends = Map(
      "fb.id.1.jack" -> "fb.id.3.mary",
      "fb.id.2.bill" -> "fb.id.4.harry",
      "fb.id.4.harry" -> "fb.id.1.jack"
    )

    // APIs
    def fetchProfile(userId: String): Future[Profile] = Future {
      Thread.sleep(Random.nextInt(1000))
      Profile(userId, userNames(userId))
    }

    def fetchBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(Random.nextInt(1000))
      Profile(friends(profile.id), userNames(friends(profile.id)))
    }
  }

  // Jack pokes to Mary
  val jack = SocialNetwork.fetchProfile("fb.id.1.jack")
  jack.onComplete {
    case Success(jackProfile) => {
      val mary = SocialNetwork.fetchBestFriend(jackProfile)
      mary.onComplete {
        case Success(maryProfile) => jackProfile.poke(maryProfile)
        case Failure(exception) => exception.printStackTrace()
      }
    }
    case Failure(exception) => exception.printStackTrace()
  }

  // Function Compositions of Futures
  // map, flatMap and filter
  val jackName: Future[String] = SocialNetwork.fetchProfile("fb.id.1.jack").map(profile => profile.name)
  val maryProfile: Future[Profile] = jack.flatMap(profile => SocialNetwork.fetchBestFriend(profile))
  val billBestFriendWithFilter: Future[Profile] = SocialNetwork.fetchProfile("fb.id.2.bill").filter(profile => profile.name.startsWith("h"))

  // for-comprehension
  for {
    jack <- SocialNetwork.fetchProfile("fb.id.1.jack")
    mary <- SocialNetwork.fetchBestFriend(jack)
  } jack poke mary

  println("For Filter")
  (for {
    profile <- SocialNetwork.fetchProfile("fb.id.2.bill")
    bestFriend <- SocialNetwork.fetchBestFriend(profile) if bestFriend.name.startsWith("h")
  } yield bestFriend.name).onComplete(x => x match {
    case Success(value) => println(value)
    case Failure(e) => e.printStackTrace()
  })

  // Fallbacks
  val getProfileNoMaterWhat = SocialNetwork.fetchProfile("unknown").recover {  // Recover takes a partial function which returns a Profile
    case e: Throwable => Profile("fb.id.5.dummy", "Dummy")
  }

  val fetchProfileNoMaterWhat = SocialNetwork.fetchProfile("unknown").recoverWith {   // Recover takes a partial function which fetches the profile
    case e: Throwable => SocialNetwork.fetchProfile("fb.id.5.dummy")
  }

  val fallBack = SocialNetwork.fetchProfile("unknown").fallbackTo(SocialNetwork.fetchProfile("fb.id.5.dummy"))  // Fallbacks in case of an exception

  getProfileNoMaterWhat.foreach(println)
  fetchProfileNoMaterWhat.foreach(println)
  fallBack.foreach(println)



  /*
  * Blocking Futures -
    - Why? Sometimes we need to block Future for some critical applications such as Bank Transfer/Transactional Operations
    - Await.result and Await.ready can be used for blocking a Future
  * */

  case class User(name: String)
  case class Transction(userName: String, merchantName: String, amount: Double, status: String)

  object BankingApp {
    def fetchUser(name: String): Future[User] = Future {
      // Some DB Operations
      Thread.sleep(1000)
      User(name)
    }

    def createTransaction(user: User, merchantName: String, amount: Double): Future[Transction] = Future {
      // Some Validation Logic
      Thread.sleep(1000)
      Transction(userName = user.name, merchantName = merchantName, amount = amount, status = "SUCCESS")
    }

    def purchaseItem(username: String, merchantName: String, item: String, cost: Double): String = {
      // Fetch User from DB
      // create the transaction
      // wait till the transaction is finished
      val transactionFuture = for {
        user <- fetchUser(username)
        txn <- createTransaction(user, merchantName, cost)
      } yield txn.status
//      Await.result(transactionFuture, 2.seconds)  // Blocks the thread Waits till the Future is complete and returns the value
      val transactionReady = Await.ready(transactionFuture, 2.seconds)  // Blocks the thread Waits till the Future is complete and returns a completed Futore of same type
      transactionReady.value.map(t => t match {
        case Success(value) => value
        case Failure(e) => "FAILED"
      }).get
    }
  }

  println(BankingApp.purchaseItem("Alok", "someMerchant", "IPhone 15", 10000))


  // Promises
  val aPromise = Promise[Int]

  val fut1 = Future {   // Will result in a Failure as the promise is already completed by fut2
    Thread.sleep(5000)
    aPromise success 1
  }

  val fut2 = Future {
    Thread.sleep(1000)
    aPromise success 2
  }

  fut1.onComplete(t => t match {
    case Success(value) => println(s"fut1 -> $value")
    case Failure(e) => println(s"fut1 -> $e")
  })
  fut2.onComplete(t => t match {
    case Success(value) => println(s"fut2 -> $value")
    case Failure(e) => println(s"fut2 -> $e")
  })

  aPromise.future.onComplete(x => println(s"Future of aPromise holds value $x post computation"))
  Thread.sleep(10000)


  // Producer Consumer using Promises
  val prodConsPromise = Promise[Int]()  // Promise of type Int
  val futureOfPromise = prodConsPromise.future  // Future wrapped inside a Promise
  val producerFuture = Future {
    try {
      val value = Random.nextInt(200)
      Thread.sleep(1000)
      prodConsPromise success value  // Completes the future with success and value
    } catch {
      case e: Exception => prodConsPromise failure e  // Completes the future with exception
    }
  }

  val consumerFuture = Future {
    Thread.sleep(200)
    futureOfPromise onComplete (t => t match {
      case Success(value) => println(s"Consumed value $value from producer")
    })
  }

  Thread.sleep(2000)


  /*
    Exercises -
      1) fulfill a future IMMEDIATELY with a value
      2) inSequence(fa, fb) -> Return the future b after it made sure that future a is successful
      3) first(fa, fb) => new future with the first value of the two futures
      4) last(fa, fb) => new future with the last value
      5) retryUntil[T](action: () => Future[T], condition: T => Boolean): Future[T]
  */

  // Solutions
  def futureImmediately[T](value: T): Future[T] = Future(value)
  def inSequence[A, B](fa: Future[A], fb: Future[B]): Future[B] = {
    fa.flatMap(_ => fb)
  }
  def first[A](fa: Future[A], fb: Future[A]): Future[A] = {
    val promise = Promise[A]
    // tryComplete -> tries to complete the promise with either a value or the exception.
    // Whichever future completes first the promise will have value for that particular future and the later will be ignored
    // as success and future to a promise can only be published once
    fa.onComplete(promise.tryComplete)
    fb.onComplete(promise.tryComplete)
    promise.future
  }
  def last[A](fa: Future[A], fb: Future[A]): Future[A] = {
    val bothPromise = Promise[A]
    val lastPromise = Promise[A]
    def checkAndComplete(result: Try[A]) = {
      if !bothPromise.tryComplete(result) then lastPromise.tryComplete(result)
      else bothPromise.tryComplete(result)
    }
    fa.onComplete(checkAndComplete)
    fb.onComplete(checkAndComplete)
    lastPromise.future
  }
  def retryUntil[T](action: () => Future[T], condition: T => Boolean): Future[T] = {
    action().filter(condition).recoverWith {
      case _ => retryUntil(action, condition)
    }
  }

  val fa = Future {
    Thread.sleep(2000)
    println("Completing fa")
    49
  }

  val fb = Future {
    Thread.sleep(500)
    println("Completing fb")
    43
  }


  futureImmediately(10).foreach(println)
  inSequence(fa, fb).foreach(println)
  first(fa, fb).foreach(x => println(s"FIRST -> $x"))
  last(fa, fb).foreach(x => println(s"LAST -> $x"))

  val random = new Random()
  val action = () => Future {
    Thread.sleep(100)
    val nextValue = random.nextInt(100)
    println("generated " + nextValue)
    nextValue
  }

  retryUntil(action, (x: Int) => x < 10).foreach(result => println("settled at " + result))

  Thread.sleep(20000)

}
