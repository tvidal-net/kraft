package net.tvidal.kraft.tools

import joptsimple.OptionParser
import joptsimple.OptionSet
import net.tvidal.kraft.Description
import net.tvidal.kraft.KRaftTool
import net.tvidal.kraft.SUCCESS

@Description("Test tool")
class TestTool(parser: OptionParser) : KRaftTool {

    private val flag = parser.accepts("flag", "Flag usage")

    private val int = parser.accepts("int", "Integer argument")
      .withRequiredArg()
      .ofType(Int::class.java)

    private val lop = parser.accepts("op", "optional long")
      .withRequiredArg()
      .ofType(Long::class.java)
      .defaultsTo(0L)

    override fun execute(op: OptionSet): Int {
        println("flag is set ${op.has(flag)}")
        println("int is ${op.valueOf(int)}")
        println("op is ${op.valueOf(lop)}")
        return SUCCESS
    }

}
