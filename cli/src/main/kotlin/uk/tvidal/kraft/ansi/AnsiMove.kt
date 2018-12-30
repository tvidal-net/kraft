package uk.tvidal.kraft.ansi

import joptsimple.internal.Strings.EMPTY

enum class AnsiMove(private val id: Char) {

    UP('A'),
    DOWN('B'),
    RIGHT('C'),
    LEFT('D'),
    COL('G'),
    ROW('H'),
    CLEAR('K'),
    SAVE('s'),
    RESTORE('u');

    fun move(n: Int, force: Boolean = false) = if (force || hasAnsiSupport) "$ESC[$n$id" else EMPTY
}
