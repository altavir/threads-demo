package ru.mipt.npm.demo.threads

import kotlinx.coroutines.experimental.channels.asReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import tornadofx.*
import java.util.*
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.experimental.buildSequence

val loopTask: FXTask<*>.() -> Unit = {
    updateTitle("Loop task")
    updateProgress(0, 200)

    repeat(200) {
        Thread.sleep(50)
        updateProgress(it + 1L, 200)
        updateMessage("${(it + 1).toDouble() / 2} % Done")
    }

    updateMessage("Task finished")
}

val asyncCall: FXTask<*>.() -> Unit = {
    updateTitle("Main")
    updateMessage("Starting task")
    updateProgress(-1, 1)

    Thread.sleep(2000)

    updateMessage("Async call started")
    val child = runTask {
        updateTitle("Async")
        repeat(200) {
            Thread.sleep(50)
            updateProgress(it + 1L, 200)
            updateMessage("${(it + 1).toDouble() / 2} % Done")
        }
    }

    Thread.sleep(2000)
    updateMessage("Can drink beer while async task works!")

    try {
        child.get() // join thread
        updateMessage("Async work finished")
        Thread.sleep(2000)
    } catch (ex: CancellationException) {
        updateMessage("Async work cancelled")
        Thread.sleep(2000)
    }
}

interface Callback<T> {
    fun reportProgress(progress: Double)
    fun reportResult(result: T)
}

val callback: FXTask<*>.() -> Unit = {
    updateTitle("Main")
    updateMessage("Starting task")
    updateProgress(-1, 1)

    val callback = object : Callback<String> {
        override fun reportProgress(progress: Double) {
            updateMessage("Async reported progress: ${progress * 100} %")
            updateProgress(progress, 1.0)
        }

        override fun reportResult(result: String) {
            updateProgress(1.0, 1.0)
            updateMessage("Async work finished with result $result")
            Thread.sleep(2000)
        }
    }

    Thread.sleep(2000)
    updateMessage("Async call started")

    val child = runTask {
        updateTitle("Async")
        repeat(200) {
            Thread.sleep(50)
            updateProgress(it + 1L, 200)
            updateMessage("${(it + 1).toDouble() / 2} % Done")
            if ((it + 1) % 50 == 0) {
                callback.reportProgress((it + 1).toDouble() / 200)
            }
        }
        callback.reportResult("OK")
    }

    Thread.sleep(2000)
    updateMessage("Can drink beer while async task works!")

    child.get()//keep main task from finishing while async is running
}

val parallel: FXTask<*>.() -> Unit = {
    updateTitle("Parallel")
    updateProgress(0, 20)

    val counter = AtomicInteger(0)
    val parent = this

    (1..20).map {
        runTask {
            updateTitle("Worker $it")
            synchronized(counter) {
                Thread.sleep(1000)
            }
            parent.updateProgress(counter.incrementAndGet() + 1L, 20)
            parent.updateMessage("Work $it completed")
        }
    }.forEach { it.get() }
}

val coroutines: FXTask<*>.() -> Unit = {
    val random = Random()

    val channel = buildSequence { yield(random.nextDouble()) } // building a lazy sequence of rundom numbers
            .chunked(50) { it.sum() } // taking 50 subsequent elements and summing up
            .asReceiveChannel() // transforming into asynchronous receive channel

    //nothing is

    launch {
        channel.consumeEach {

        }
    }

}