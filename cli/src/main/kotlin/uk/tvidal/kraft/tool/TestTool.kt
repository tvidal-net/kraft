package uk.tvidal.kraft.tool

import joptsimple.OptionParser
import joptsimple.OptionSet
import uk.tvidal.kraft.Description
import uk.tvidal.kraft.KRaftTool
import uk.tvidal.kraft.LocalTest
import uk.tvidal.kraft.LogConfig
import uk.tvidal.kraft.SUCCESS
import uk.tvidal.kraft.stringArgument

@Description("runs a local test cluster")
class TestTool(parser: OptionParser) : KRaftTool {

    val path = parser.stringArgument("file path", "path")

    override fun execute(op: OptionSet): Int {
        LogConfig.appender = "STDOUT"
        LocalTest.main()
        return SUCCESS
    }
}
