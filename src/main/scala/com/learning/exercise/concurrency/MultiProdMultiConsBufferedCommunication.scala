package com.learning.exercise.concurrency

import scala.collection.mutable
import scala.util.Random

object MultiProdMultiConsBufferedCommunication {
  /*
    The behavior does not change in terms of racing, but using notifyAll prevents a possible deadlock.
      Example: 10 producers, 2 consumers, buffer size = 3. start())
        1. One producer fills the buffer quickly. The other 9 go to sleep. Same with the open producer when it's done.
        2. One consumer consumes all, then goes to sleep. The others go to sleep once they see buffer empty.
        3. After 3 notifications, 3 producers wake up, fill the space. Notifications go to other producers.
        4. Every poor producer sees buffer full, goes to sleep.
        5. Deadlock.
      NotifyAll fixes this.
   */
  private class Producer(id: Int, buffer: mutable.Queue[Int], maxCapacity: Int, producerMaxSleep: Int = 500) extends Thread {
    override def run(): Unit = {
      println(s"[producer-$id] started..")
      while (true) {
        val value = Random.nextInt(1000)
        buffer.synchronized {
          while (buffer.size == maxCapacity) {  // As we can multiple producer, we can't use if,
            // because even if the wait is over some other producer may produce to the buffer and make it full immediately
            println(s"[producer-$id] buffer is full. waiting for the consumer to consume")
            buffer.wait()
          }

          println(s"[producer-$id] publishing value $value to buffer")
          buffer.enqueue(value)
          /*
            We need to use notifyAll. Otherwise, deadlock scenario:
              Scenario: 2 producers, one consumer, capacity = 1
                producer1 produces a value, then waits
                producer2 sees buffer full, waits
                consumer consumes value, notifies one producer (producer1)
                consumer sees buffer empty, wait
                producer1 produces a value, calls notify - signal goes to producer2
                producer1 sees buffer full, waits
                producer2 sees buffer full, waits
                DEADLOCK
           */
          buffer.notifyAll()
        }
        Thread.sleep(Random.nextInt(producerMaxSleep))
      }
    }
  }

  private class Consumer(id: Int, buffer: mutable.Queue[Int], consumerMaxSleep: Int = 500) extends Thread {
    override def run(): Unit = {
      println(s"[consumer-$id] started..")
      while (true) {
        buffer.synchronized {
          while (buffer.isEmpty) {
            println(s"[consumer-$id] buffer is empty. waiting for producer to produce data")
            buffer.wait()
          }

          println(s"[consumer-$id] consumed value from buffer: ${buffer.dequeue()}")
          buffer.notifyAll()
        }
        Thread.sleep(Random.nextInt(consumerMaxSleep))
      }
    }
  }

  private def multiProdCons(nProducers: Int, nConsumers: Int, bufferCapacity: Int, producerMaxSleep: Int = 500, consumerMaxSleep: Int = 500): Unit = {
    val buffer = new mutable.Queue[Int]

    (1 to nProducers).map(id => new Producer(id, buffer, bufferCapacity, producerMaxSleep)).foreach(thread => thread.start())
    (1 to nConsumers).map(id => new Consumer(id, buffer, consumerMaxSleep)).foreach(thread => thread.start())
  }

  def main(args: Array[String]): Unit = {
    multiProdCons(100000, 50000, 250000, 5000, 1500)
  }
}
