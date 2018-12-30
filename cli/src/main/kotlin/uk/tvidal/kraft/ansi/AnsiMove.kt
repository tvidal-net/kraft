package uk.tvidal.kraft.ansi

enum class AnsiMove(private val ch: Char) {

    UP('A'),
    DOWN('B'),
    RIGHT('C'),
    LEFT('D'),
    COL('G'),
    ROW('H'),
    CLEAR('K'),
    SAVE('s'),
    RESTORE('u');

    companion object {
        private inline fun ansi(block: () -> String) {
            print(block())
        }

        fun up(n: Int) = ansi { "$ESC[$n$UP" }

        fun down(n: Int) = ansi { "$ESC[$n$DOWN" }

        fun clearLine() = ansi { "$ESC[2$CLEAR" }

        fun column(n: Int) = ansi { "$ESC[$n$COL" }

        fun pos(row: Int = 0, col: Int = 0) = ansi { "$ESC[$row;$col$ROW" }

        fun save() = ansi { "$ESC[$SAVE" }

        fun restore() = ansi { "$ESC[$RESTORE" }
    }

    override fun toString() = ch.toString()
}
