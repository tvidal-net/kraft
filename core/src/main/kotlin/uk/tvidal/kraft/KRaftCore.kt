package uk.tvidal.kraft

import java.lang.String.format
import java.util.UUID

const val DEFAULT_CLUSTER_NAME = "KRaft"
const val MAX_CLUSTER_NAME_LENGTH = 16

const val FOREVER = 253402300799999L // 9999-12-31 23:59:59.999
const val NEVER = -1L
const val NOW = 0L

const val HEARTBEAT_TIMEOUT = 500

const val LONG_BYTES = java.lang.Long.BYTES
const val INT_BYTES = java.lang.Integer.BYTES
const val SHORT_BYTES = java.lang.Short.BYTES
const val BYTE_BYTES = java.lang.Byte.BYTES

const val FIRST_INDEX = 1L

val MAGIC_NUMBER: UUID = UUID
    .fromString("deadBeef-aced-f00d-babe-180182c0ffee")

val Any.qualifiedClassName: String
    get() = javaClass.name.substringBefore('$')

val Any.javaClassName: String
    get() = qualifiedClassName.substringAfterLast('.')

inline fun <T> AutoCloseable.use(block: () -> T): T = try {
    block()
} finally {
    close()
}

inline fun <T> iterable(
    crossinline hasNext: () -> Boolean = { true },
    crossinline iterator: () -> T
): Iterable<T> = object : Iterable<T>, Iterator<T> {
    override fun iterator() = this
    override fun hasNext() = hasNext()
    override fun next() = iterator()
}

fun duration(millis: Long): String {
    val duration: Pair<String, Double> = when {
        // Hours
        millis > 7_200_000 -> "h" to millis / 3_600_000.0
        // Minutes
        millis > 120_000L -> "m" to millis / 60_000.0
        // Seconds
        else -> "s" to millis / 1000.0
    }
    return format("%.4g%s", duration.second, duration.first)
}
