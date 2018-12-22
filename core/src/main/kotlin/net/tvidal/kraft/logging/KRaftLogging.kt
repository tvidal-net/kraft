package net.tvidal.kraft.logging

import org.slf4j.LoggerFactory

abstract class KRaftLogging(private val loggerName: String? = null) {

    val log by lazy {
        KRaftLogger(
            logger = LoggerFactory.getLogger(loggerName ?: loggerName(this))
        )
    }
}
