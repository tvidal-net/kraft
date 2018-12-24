package uk.tvidal.kraft

import uk.tvidal.kraft.logging.KRaftLogger
import java.lang.System.currentTimeMillis
import java.lang.Thread.currentThread
import java.lang.Thread.sleep
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors.newCachedThreadPool
import java.util.concurrent.Executors.newSingleThreadExecutor
import java.util.concurrent.Future
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger

fun daemonThread(runnable: Runnable, name: String) = Thread(runnable, name)
    .apply { isDaemon = true }

fun threadFactory(name: String) = object : ThreadFactory {

    private val count = AtomicInteger()

    private val next: Int
        get() = count.getAndIncrement()

    override fun newThread(it: Runnable) = daemonThread(it, "$name-$next")
}

fun fixedThreadPool(name: String, size: Int): ExecutorService = ThreadPoolExecutor(
    size,
    size,
    0L,
    MILLISECONDS,
    SynchronousQueue<Runnable>(),
    threadFactory(name)
)

fun singleThreadPool(name: String): ExecutorService = newSingleThreadExecutor { daemonThread(it, name) }

fun cachedThreadPool(name: String): ExecutorService = newCachedThreadPool(threadFactory(name))

fun <T> ExecutorService.tryCatch(block: () -> T): Future<T> = submit<T> {
    KRaftLogger(block)
        .tryCatch(false, block)
}

fun ExecutorService.loop(flag: () -> Boolean = { true }, block: () -> Unit): Future<*> = tryCatch {
    while (flag()) {
        block()
    }
}

fun ExecutorService.retry(
    flag: () -> Boolean = { true },
    maxAttempts: Int = 10,
    initialDelay: Int = 16,
    fallbackFactor: Double = 2.0,
    maxDelay: Int = 10_000,
    name: String? = null,
    block: () -> Unit
): Future<*> = submit {
    val actualName = name ?: currentThread().name
    val random = ThreadLocalRandom.current()
    val log = KRaftLogger(block)
    var delay = initialDelay
    var attempts = maxAttempts
    var lastError: Exception? = null
    while ((maxAttempts == 0 || attempts > 0) && flag()) {
        val started = currentTimeMillis()
        try {
            block()
            delay = initialDelay
            attempts = maxAttempts
            lastError = null
        } catch (e: Exception) {
            lastError = e
            if (currentTimeMillis() - started > maxDelay) {
                delay = initialDelay
                attempts = maxAttempts
            }
            attempts--
            log.error(e) { "$actualName: Retrying after ${delay}ms... ($attempts left)" }
            if (maxAttempts == 0 || attempts > 0) {
                sleep(delay.toLong())
                val jitter = random.nextInt(initialDelay * 2) - initialDelay
                val newDelay = (delay * fallbackFactor).toInt()
                delay = (if (newDelay <= maxDelay) newDelay else delay) + jitter
            }
        } catch (e: Throwable) {
            log.error(e)
            throw e
        }
    }
    // rethrow the last error to complete the underlying future
    lastError?.let { throw it }
}
