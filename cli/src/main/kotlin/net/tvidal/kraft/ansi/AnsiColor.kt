package net.tvidal.kraft.ansi

enum class AnsiColor(private val id: Int) {
    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7);

    private val foreground by lazy { tput("setaf", "$id") }
    private val background by lazy { tput("setab", "$id") }

    fun format(text: String) = if (hasAnsiSupport) foreground + text + ANSI_RESET else text
}
