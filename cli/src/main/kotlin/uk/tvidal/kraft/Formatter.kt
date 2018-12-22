package uk.tvidal.kraft

const val DASH = '-'
const val SPACE = ' '
const val EMPTY = ""

const val DEFAULT_MARGIN = 2
const val DEFAULT_WIDTH = 10

fun colWidth(items: Iterable<String>) = (items.map(String::length).max() ?: DEFAULT_WIDTH) + DEFAULT_MARGIN

fun dashes(text: String) = dashes(text.length)
fun dashes(count: Int) = EMPTY.padEnd(count, DASH)
