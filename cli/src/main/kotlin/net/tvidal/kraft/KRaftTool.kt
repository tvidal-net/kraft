package net.tvidal.kraft

import joptsimple.OptionSet

@FunctionalInterface
interface KRaftTool {
    fun execute(op: OptionSet): Int
}
