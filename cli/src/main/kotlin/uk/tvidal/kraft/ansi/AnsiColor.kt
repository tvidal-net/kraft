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

    private val fg = tput("setaf", color)
    // private val bg = tput("setab", color)

    operator fun invoke(text: Any?, force: Boolean = false) =
        if (force || hasAnsiSupport) "$ansiBold$fg$text$ansiReset"
        else text.toString()
}
