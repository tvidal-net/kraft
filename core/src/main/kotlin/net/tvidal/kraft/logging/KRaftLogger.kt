package net.tvidal.kraft.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class KRaftLogger(val logger: Logger) {

    constructor(name: String) : this(LoggerFactory.getLogger(name))

    constructor(enum: Enum<*>) : this("${loggerName(enum::class)}.${enum.name}")

    constructor(cls: KClass<*>) : this(loggerName(cls))

    constructor(block: () -> Unit) : this(block::class)

    companion object {
        private const val COMPANION_SUFFIX = ".Companion"

        private fun loggerName(cls: KClass<*>) =
            if (cls.isCompanion) cls.qualifiedName!!.substringBefore(COMPANION_SUFFIX)
            else cls.qualifiedName!!.substringBefore('$')
    }

    inline fun trace(message: () -> Any?) {
        if (logger.isTraceEnabled) logger.trace(message().toString())
    }

    fun trace(message: Any?) = trace { message }

    fun trace(message: String, vararg args: Any?) = logger.trace(message, *args)

    inline fun debug(message: () -> Any?) {
        if (logger.isDebugEnabled) logger.debug(message().toString())
    }

    fun debug(message: Any?) = debug { message }

    fun debug(message: String, vararg args: Any?) = logger.debug(message, *args)

    inline fun info(message: () -> Any?) {
        if (logger.isInfoEnabled) logger.info(message().toString())
    }

    fun info(message: Any?) = info { message }

    fun info(message: String, vararg args: Any?) = logger.info(message, *args)

    inline fun warn(e: Throwable? = null, message: () -> Any?) {
        if (logger.isWarnEnabled) logger.warn(message().toString(), e)
    }

    fun warn(message: Any?, e: Throwable? = null) = warn(e) { message }

    fun warn(message: String, vararg args: Any?) = logger.warn(message, *args)

    inline fun error(e: Throwable? = null, message: () -> Any?) {
        if (logger.isErrorEnabled) logger.error(message().toString(), e)
    }

    fun error(message: Any?, e: Throwable? = null) = error(e) { message }

    fun error(e: Throwable) = error(e) { "${e::class.simpleName}: ${e.message}" }

    fun error(message: String, vararg args: Any?) = logger.error(message, *args)
}
