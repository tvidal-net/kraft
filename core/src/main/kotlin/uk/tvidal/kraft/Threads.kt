package uk.tvidal.kraft

import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger

fun threadFactory(name: String) = object : ThreadFactory {

    private val count = AtomicInteger()

    private val next: Int
        get() = count.getAndIncrement()

    override fun newThread(it: Runnable) = Thread(it, "$name-$next")
}

fun fixedThreadPool(name: String, size: Int): ExecutorService = ThreadPoolExecutor(
    size,
    size,
    0L,
    MILLISECONDS,
    SynchronousQueue<Runnable>(),
    threadFactory(name)
)
