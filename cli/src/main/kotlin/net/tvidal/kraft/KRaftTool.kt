package net.tvidal.kraft

import joptsimple.OptionSet

interface KRaftTool {

    fun run(op: OptionSet)

}
