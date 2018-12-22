package net.tvidal.kraft.logging

import org.slf4j.Logger

class KRaftLogger(val logger: Logger) {

    inline fun trace(message: () -> Any?) {
        if (logger.isTraceEnabled) {
            logger.trace(message().toString())
        }
    }

    fun trace(message: Any?) = trace { message }

    inline fun debug(message: () -> Any?) {
        if (logger.isDebugEnabled) {
            logger.debug(message().toString())
        }
    }

    fun debug(message: Any?) = debug { message }

    inline fun info(message: () -> Any?) {
        if (logger.isInfoEnabled) {
            logger.info(message().toString())
        }
    }

    fun info(message: Any?) = info { message }

    inline fun warn(e: Throwable? = null, message: () -> Any?) {
        if (logger.isWarnEnabled) {
            logger.warn(message().toString(), e)
        }
    }

    fun warn(message: Any?, e: Throwable? = null) = warn(e) { message }

    inline fun error(e: Throwable? = null, message: () -> Any?) {
        if (logger.isErrorEnabled) {
            logger.error(message().toString(), e)
        }
    }

    fun error(message: Any?, e: Throwable? = null) = error(e) { message }

    fun error(e: Throwable) = error(e) { "${e::class.simpleName}: ${e.message}" }
}
