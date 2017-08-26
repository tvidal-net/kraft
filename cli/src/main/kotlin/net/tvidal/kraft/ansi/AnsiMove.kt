package net.tvidal.kraft.ansi

import joptsimple.internal.Strings.EMPTY

enum class AnsiMove(private val id: Char) {

    UP('A'),
    DOWN('B'),
    RIGHT('C'),
    LEFT('D'),
    COL('G'),
    ROW('H');

    fun move(n: Int) = if (HAS_ANSI_SUPPORT) "$ESC[$n$id" else EMPTY
}