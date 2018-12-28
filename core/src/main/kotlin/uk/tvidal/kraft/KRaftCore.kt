package uk.tvidal.kraft

import java.util.UUID

const val DEFAULT_CLUSTER_NAME = "KRaft"
const val MAX_CLUSTER_NAME_LENGTH = 16

const val LONG_BYTES = java.lang.Long.BYTES
const val INT_BYTES = java.lang.Integer.BYTES
const val SHORT_BYTES = java.lang.Short.BYTES
const val BYTE_BYTES = java.lang.Byte.BYTES

const val FIRST_INDEX = 1L

val MAGIC_NUMBER: UUID = UUID
    .fromString("acedBabe-dead-f00d-beef-180182c0ffee")

fun Function<*>.qualifiedClassName() = javaClass.name.substringBefore('$')

fun Function<*>.simpleClassName() = qualifiedClassName().substringAfterLast('.')

inline fun <T> iterable(
    crossinline hasNext: () -> Boolean = { true },
    crossinline iterator: () -> T
): Iterable<T> = object : Iterable<T>, Iterator<T> {
    override fun iterator() = this
    override fun hasNext() = hasNext()
    override fun next() = iterator()
}
