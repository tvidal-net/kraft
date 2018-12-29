package uk.tvidal.kraft.tool

import joptsimple.OptionParser
import joptsimple.OptionSet
import uk.tvidal.kraft.Description
import uk.tvidal.kraft.KRaftTool
import uk.tvidal.kraft.SUCCESS
import uk.tvidal.kraft.intArgument
import uk.tvidal.kraft.longArgument
import uk.tvidal.kraft.option

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
