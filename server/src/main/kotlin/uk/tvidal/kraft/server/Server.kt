package uk.tvidal.kraft.server

import java.lang.Runtime.getRuntime

internal const val KRAFT_THREAD_NAME = "KRaftMainThread"

private const val SHUTDOWN_HOOK_THREAD_NAME = "KRaftShutdownHook"

internal const val LOOP_TOLERANCE_MILLIS = 16L

fun registerStopServerShutdownHook(server: KRaftServer) = getRuntime().addShutdownHook(
    Thread(
        Runnable { server.stop() },
        SHUTDOWN_HOOK_THREAD_NAME
    )
)
