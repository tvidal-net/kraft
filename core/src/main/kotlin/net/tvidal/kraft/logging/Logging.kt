package net.tvidal.kraft.logging

private const val COMPANION_SUFFIX = ".Companion"

internal fun loggerName(logger: Any) = logger::class.qualifiedName!!.let { name ->
    when (logger) {
        is Enum<*> -> name + '.' + logger.name
        logger::class.isCompanion -> name.substringBefore(COMPANION_SUFFIX)
        else -> name.substringBefore('$')
    }
}
