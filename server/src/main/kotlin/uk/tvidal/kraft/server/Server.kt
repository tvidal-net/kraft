package uk.tvidal.kraft.server

import java.lang.Runtime.getRuntime

private const val SINGLE_THREAD_NAME = "KRaftMainThread"

private const val SHUTDOWN_HOOK_THREAD_NAME = "KRaftShutdownHook"

internal fun singleThread(runnable: Runnable): Thread = Thread(runnable, SINGLE_THREAD_NAME).apply {
    isDaemon = false
}

fun registerStopServerShutdownHook(server: KRaftServer) = getRuntime().addShutdownHook(
    Thread(
        Runnable {
            server.stop()
        },
        SHUTDOWN_HOOK_THREAD_NAME
    )
)
