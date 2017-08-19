package net.tvidal.kraft.ansi

import java.io.BufferedReader
import java.io.InputStreamReader

private fun exec(vararg args: String) = ProcessBuilder()
  .command(*args)
  .redirectErrorStream(true)
  .start()!!

private fun retVal(vararg args: String) = exec(*args).waitFor()

private fun stdOut(vararg args: String): String {
    val process = exec(*args)
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    return reader.use { it.readLine() }
}

internal fun tput(vararg args: String) = stdOut("tput", *args)

internal val isTerminal by lazy { retVal("test", "-t", "1") == 0 }

val hasAnsiSupport by lazy {
    if (!isTerminal) false
    else tput("colors").toInt() > 0
}

internal val ANSI_RESET by lazy { tput("sgr0") }

internal const val ESC = 27.toChar()
