package net.tvidal.kraft.ansi

val HAS_ANSI_SUPPORT by lazy { System.console() != null && terminalColors() > 2 }

internal val ANSI_RESET by lazy { tput("sgr0") }
internal val ANSI_BOLD by lazy { tput("bold") }

internal const val ESC = 0x1B.toChar()

fun terminalRows() = tput("lines").toInt()

fun terminalColumns() = tput("cols").toInt()

fun terminalColors() = tput("colors").toInt()

internal fun tput(vararg args: String) = exec("tput", *args)

private fun exec(vararg args: String) = ProcessBuilder()
  .redirectErrorStream(true)
  .command(*args)
  .start()!!
  .inputStream
  .reader()
  .buffered()
  .use { it.readLine() }
