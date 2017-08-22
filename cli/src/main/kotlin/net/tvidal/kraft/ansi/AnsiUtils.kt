package net.tvidal.kraft.ansi

internal const val ESC = 0x1B.toChar()

val hasAnsiSupport by lazy { isTerminal && tput("colors").toInt() > 0 }

internal fun tput(vararg args: String) = stdOut("tput", *args)

internal val ANSI_RESET by lazy { tput("sgr0") }
internal val ANSI_BOLD by lazy { tput("setb", "1") }

private fun exec(vararg args: String) = ProcessBuilder()
  .command(*args)
  .redirectErrorStream(true)
  .start()!!

private fun stdOut(vararg args: String) =
  exec(*args)
    .inputStream
    .reader()
    .buffered()
    .use { it.readLine() }

private fun retVal(vararg args: String) = exec(*args).waitFor()

private val isTerminal by lazy { retVal("test", "-t", "1") == 0 }
