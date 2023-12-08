package com.learning.exercise.concurrency

import scala.collection.mutable
import scala.util.Random

object ProdConsQueuedCommunication extends App {
  private def queuedProdCons(producerMaxSleep: Int, consumerMaxSleep: Int): Unit = {
    val capacity = 3
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]

    val producerThread = new Thread(() => {
      println("[producer] started..")
      while(true) {
        val value = Random.nextInt(1000)
        buffer.synchronized {
          if (buffer.size == capacity) {
            println("[producer] buffer is full. waiting for the consumer to consume")
            buffer.wait()
          }

          println(s"[producer] publishing value $value to buffer")
          buffer.enqueue(value)
          buffer.notify()
        }
        Thread.sleep(Random.nextInt(producerMaxSleep))
      }
    })

    val consumerThread = new Thread(() => {
      println("[consumer] started..")
      while(true) {
        buffer.synchronized {
          if (buffer.isEmpty) {
            println("[consumer] buffer is empty. waiting for producer to produce data")
            buffer.wait()
          }

          println(s"[consumer] consumed value from buffer: ${buffer.dequeue()}")
          buffer.notify()
        }
        Thread.sleep(Random.nextInt(consumerMaxSleep))
      }
    })

    producerThread.start()
    consumerThread.start()
  }
  queuedProdCons(250, 500)
}
