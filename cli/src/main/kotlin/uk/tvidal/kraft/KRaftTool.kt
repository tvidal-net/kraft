package uk.tvidal.kraft

import joptsimple.OptionSet

interface KRaftTool {

    fun execute(op: OptionSet): Int
}
