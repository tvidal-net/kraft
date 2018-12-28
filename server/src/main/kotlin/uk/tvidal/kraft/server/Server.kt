package uk.tvidal.kraft.server

import java.lang.Runtime.getRuntime
import kotlin.concurrent.thread

internal const val KRAFT_THREAD_NAME = "KRaftMainThread"

private const val SHUTDOWN_HOOK_THREAD_NAME = "KRaftShutdownHook"

fun registerStopServerShutdownHook(server: KRaftServer) = getRuntime().addShutdownHook(
    thread(name = SHUTDOWN_HOOK_THREAD_NAME, start = false) {
        server.close()
    }
)
