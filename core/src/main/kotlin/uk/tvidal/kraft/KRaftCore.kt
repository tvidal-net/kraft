package uk.tvidal.kraft

import java.util.UUID

const val DEFAULT_CLUSTER_NAME = "KRaft"
const val MAX_CLUSTER_NAME_LENGTH = 16

const val FOREVER = 253402300799999L // 9999-12-31 23:59:59.999
const val NEVER = -1L
const val NOW = 0L

const val HEARTBEAT_TIMEOUT = 500

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
