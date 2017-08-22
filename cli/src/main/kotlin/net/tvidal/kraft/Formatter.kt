package net.tvidal.kraft

import joptsimple.internal.Strings
import net.tvidal.kraft.KRaftHelpFormatter.OPTION

const val MARGIN = 2
const val DEFAULT_WIDTH = 10
const val MIN_WIDTH = OPTION.length + MARGIN

fun colWidth(items: Iterable<String>) = (items
  .map(String::length)
  .max() ?: DEFAULT_WIDTH) + MARGIN

private fun align(text: String, width: Int) =
  text + spaces(width - text.length)

fun dashes(text: String) = dashes(text.length)
fun dashes(count: Int) = Strings.repeat('-', count)
fun spaces(count: Int) = Strings.repeat(' ', count)
