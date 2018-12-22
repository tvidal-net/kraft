package net.tvidal.kraft

import net.tvidal.kraft.logging.KRaftLogger
import java.util.logging.Level

fun main(args: Array<String>) {
    logbackConfigurationFile = LOGBACK_CONSOLE

    val log = KRaftLogger {}
    log.warn { Level.SEVERE }
}
