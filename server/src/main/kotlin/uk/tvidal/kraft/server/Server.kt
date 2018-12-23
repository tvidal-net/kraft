package uk.tvidal.kraft.server

import java.lang.Runtime.getRuntime
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger

internal const val KRAFT_THREAD_NAME = "KRaftMainThread"

private const val SHUTDOWN_HOOK_THREAD_NAME = "KRaftShutdownHook"

internal const val LOOP_TOLERANCE_MILLIS = 16L

fun threadFactory(name: String = KRAFT_THREAD_NAME) = object : ThreadFactory {

    private val count = AtomicInteger()

    private val next: Int
        get() = count.getAndIncrement()

    override fun newThread(it: Runnable) = Thread(it, "$name-$next")
}

internal fun fixedThreadPool(size: Int): ExecutorService = ThreadPoolExecutor(
    size,
    size,
    0L,
    MILLISECONDS,
    SynchronousQueue<Runnable>(),
    threadFactory()
)

internal fun singleThread(runnable: Runnable): Thread = Thread(runnable, KRAFT_THREAD_NAME).apply {
    isDaemon = false
}

fun registerStopServerShutdownHook(server: KRaftServer) = getRuntime().addShutdownHook(
    Thread(
        Runnable { server.stop() },
        SHUTDOWN_HOOK_THREAD_NAME
    )
)
