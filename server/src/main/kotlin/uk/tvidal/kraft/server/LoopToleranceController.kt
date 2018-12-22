package uk.tvidal.kraft.server

import java.lang.System.currentTimeMillis

internal class LoopToleranceController(
    val loopToleranceMillis: Long = 10,
    private val clock: () -> Long = System::currentTimeMillis
) {

    private var start = currentTimeMillis()

    fun yield(): Long {
        val now = clock()
        return if (now - start > loopToleranceMillis) {
            // yield if we've been holding the cpu core for too long
            Thread.yield()
            // include yield duration in current loop so we'll yield again if the cpu is too busy
            start = now
            clock()
        } else now
    }
}
