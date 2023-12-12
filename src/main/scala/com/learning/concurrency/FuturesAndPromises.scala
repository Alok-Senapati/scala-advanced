package com.learning.concurrency

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Random, Success}
import scala.concurrent.duration.Duration

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
  println(Await.result(maryProfile, Duration.Inf))  // Blocks the thread till the Future execution is complete
  // Thread.sleep(10000)  // To make the main thread wait till the Future computation completes
}
