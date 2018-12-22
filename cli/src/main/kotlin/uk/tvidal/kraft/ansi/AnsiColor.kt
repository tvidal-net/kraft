package uk.tvidal.kraft.ansi

enum class AnsiColor(private val id: Int) {

    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7);

    private val fg by lazy { tput("setaf", id.toString()) }
    private val bg by lazy { tput("setab", id.toString()) }

    fun format(text: String) = if (hasAnsiSupport) ansiBold + fg + text + ansiReset else text
}
