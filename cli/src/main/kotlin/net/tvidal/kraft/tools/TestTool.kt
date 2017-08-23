package net.tvidal.kraft.tools

import joptsimple.OptionParser
import joptsimple.OptionSet
import net.tvidal.kraft.Description
import net.tvidal.kraft.KRaftTool
import net.tvidal.kraft.SUCCESS
import net.tvidal.kraft.intArgument
import net.tvidal.kraft.longArgument
import net.tvidal.kraft.option

@Description("Test tool")
class TestTool(parser: OptionParser) : KRaftTool {

    private val flag = parser.option("Flag", "flag")

    private val int = parser.intArgument("Something Integer", "int")

    private val lop = parser.longArgument("Something long", "long")
      .defaultsTo(0L)

    override fun execute(op: OptionSet): Int {
        println("flag is set ${op.has(flag)}")
        println("int is ${op.valueOf(int)}")
        println("lop is ${op.valueOf(lop)}")
        return SUCCESS
    }
}
