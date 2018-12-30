package uk.tvidal.kraft.ansi

enum class AnsiColor(color: Int) {

    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7),
    ORANGE(208);

    val fg = tput("setaf", color)
    val bg = tput("setab", color)

    fun format(text: Any?, force: Boolean = false) = if (force || hasAnsiSupport) "$ansiBold$fg$text$ansiReset" else text.toString()
}
