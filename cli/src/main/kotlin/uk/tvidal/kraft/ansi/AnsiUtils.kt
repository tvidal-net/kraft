package uk.tvidal.kraft.ansi

internal val ansiReset = tput("sgr0")
internal val ansiBold = tput("bold")

internal const val ESC: Char = 0x1B.toChar()

val terminalRows: Int = tput("lines").toInt()

val terminalColumns: Int = tput("cols").toInt()

val terminalColors: Int = tput("colors").toInt()

val hasAnsiSupport = System.console() != null && terminalColors > 2

internal fun tput(vararg args: Any) = exec("tput", "-T", "xterm-256color", *args.map(Any::toString).toTypedArray())

private fun exec(vararg args: String): String = ProcessBuilder()
    .redirectErrorStream(true)
    .command(*args)
    .start()!!
    .inputStream
    .bufferedReader()
    .use { it.readLine() }
