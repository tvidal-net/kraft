package uk.tvidal.kraft

import java.lang.String.format
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.HOURS
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS

private const val FORMAT_DEFAULT = "%.4g"

private const val FORMAT_DAY = "${FORMAT_DEFAULT}d"
private const val FORMAT_HOUR = "${FORMAT_DEFAULT}h"
private const val FORMAT_MINUTE = "${FORMAT_DEFAULT}m"
private const val FORMAT_SECOND = "${FORMAT_DEFAULT}s"
private const val FORMAT_MILLIS = "%.3fs"

private val DAY_THRESHOLD = DAYS.toMillis(2)
private val HOUR_THRESHOLD = HOURS.toMillis(2)
private val MINUTE_THRESHOLD = MINUTES.toMillis(2)
private val SECOND_THRESHOLD = SECONDS.toMillis(2)

private val DAY_MILLIS = DAYS.toMillis(1).toDouble()
private val HOUR_MILLIS = HOURS.toMillis(1).toDouble()
private val MINUTE_MILLIS = MINUTES.toMillis(1).toDouble()
private val SECOND_MILLIS = SECONDS.toMillis(1).toDouble()

fun duration(millis: Long): String = when {
    // Days
    millis >= DAY_THRESHOLD -> format(FORMAT_DAY, millis / DAY_MILLIS)
    // Hours
    millis >= HOUR_THRESHOLD -> format(FORMAT_HOUR, millis / HOUR_MILLIS)
    // Minutes
    millis >= MINUTE_THRESHOLD -> format(FORMAT_MINUTE, millis / MINUTE_MILLIS)
    // Seconds
    millis >= SECOND_THRESHOLD -> format(FORMAT_SECOND, millis / SECOND_MILLIS)
    // Millis
    else -> format(FORMAT_MILLIS, millis / 1_000.0)
}
