package uk.tvidal.kraft

import uk.tvidal.kraft.logging.KRaftLogger
import java.lang.System.currentTimeMillis
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger

val MAX_CACHED_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4

fun daemonThread(name: String, runnable: Runnable) = Thread(runnable, name)
    .apply { isDaemon = true }

fun threadFactory(name: String) = object : ThreadFactory {

    private val count = AtomicInteger()

    private val next: Int
        get() = count.getAndIncrement()

    override fun newThread(it: Runnable) = daemonThread("$name-$next", it)
}

fun singleThreadPool(name: String): ScheduledExecutorService =
    Executors.newSingleThreadScheduledExecutor { daemonThread(name, it) }

fun fixedThreadPool(name: String, size: Int): ExecutorService = ThreadPoolExecutor(
    size, size,
    0L, SECONDS,
    SynchronousQueue(),
    threadFactory(name)
)

fun cachedThreadPool(
    name: String,
    maxPoolSize: Int = MAX_CACHED_THREAD_POOL_SIZE
): ExecutorService = ThreadPoolExecutor(
    0, maxPoolSize,
    60L, SECONDS,
    SynchronousQueue(),
    threadFactory(name)
)

fun ScheduledExecutorService.every(
    period: Int,
    unit: TimeUnit = MILLISECONDS,
    block: () -> Unit
): ScheduledFuture<*> {
    val log = KRaftLogger(block)
    val longPeriod = period.toLong()
    return scheduleAtFixedRate({
        log.tryCatch(false, block)
    }, longPeriod, longPeriod, unit)
}

fun <T> ExecutorService.tryCatch(block: () -> T): Future<T> = submit<T> {
    KRaftLogger(block)
        .tryCatch(false, block)
}

fun ExecutorService.loop(
    flag: () -> Boolean = { true },
    block: () -> Unit
): Future<*> = tryCatch {
    while (flag()) {
        block()
    }
}

fun ExecutorService.retry(
    flag: () -> Boolean = { true },
    delay: RetryDelay = RetryDelay(),
    name: String? = null,
    block: () -> Unit
): Future<*> = submit {
    val actualName = name ?: block.javaClassName
    val log = KRaftLogger(block)
    while (delay.isRunning && flag()) {
        val started = currentTimeMillis()
        try {
            block()
            delay.reset()
        } catch (e: Exception) {
            // resets the delay if execution took a healthy amount of time
            val duration = currentTimeMillis() - started
            if (duration > delay.maxDelay) {
                delay.reset()
            }
            delay.catch(e)

            log.error {
                with(delay) {
                    "$actualName: (${e::class.simpleName}) ${e.message} [Retrying after ${current}ms ($attempts left)]"
                }
            }
        } catch (e: Throwable) {
            log.error(e)
            throw e
        }
    }
    // rethrow the last error to complete the underlying future
    delay.rethrow()
}
