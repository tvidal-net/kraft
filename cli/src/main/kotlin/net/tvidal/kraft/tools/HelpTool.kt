package net.tvidal.kraft.tools

import joptsimple.OptionParser
import joptsimple.OptionSet
import net.tvidal.kraft.Description
import net.tvidal.kraft.KRaftTool
import net.tvidal.kraft.SUCCESS
import net.tvidal.kraft.TOOLS
import kotlin.reflect.KClass

@Description("Usage:")
class HelpTool(private val parser: OptionParser) : KRaftTool {

    init {
        parser.formatHelpWith {
            StringBuffer().run {
                appendln("Usage: <tool-name> [args]")
                appendln("- Tool help: <tool-name> --help")
                appendln()
                appendln("Available Tools:")
                for ((toolName, toolClass) in TOOLS) {
                    appendln(" - $toolName:  ${description(toolClass)}")
                }
                toString()
            }
        }
    }

    private fun description(toolClass: KClass<*>): String {
        val description = toolClass.annotations
          .filterIsInstance<Description>()
          .firstOrNull()

        return description?.text ?: toolClass.java.simpleName!!
    }

    override fun execute(op: OptionSet): Int {
        parser.printHelpOn(System.err)
        return SUCCESS
    }
}
