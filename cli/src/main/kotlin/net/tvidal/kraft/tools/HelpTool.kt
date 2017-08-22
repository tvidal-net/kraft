package net.tvidal.kraft.tools

import joptsimple.OptionParser
import joptsimple.OptionSet
import net.tvidal.kraft.Description
import net.tvidal.kraft.ERROR_SIMPLE
import net.tvidal.kraft.KRaftTool
import net.tvidal.kraft.TOOLS
import net.tvidal.kraft.ansi.hasAnsiSupport
import kotlin.reflect.KClass

@Description("Prints this help text")
class HelpTool(private val parser: OptionParser) : KRaftTool {

    init {
        parser.formatHelpWith {
            StringBuffer().run {
                appendln("Usage: <tool-name> [args]")
                appendln("- Tool help: <tool-name> --help")
                appendln()
                appendln("Available Tools:")
                appendln("hasAnsiSupport: $hasAnsiSupport")
                for ((toolName, toolClass) in TOOLS) {
                    appendln(" - $toolName:  ${toolClass.description}")
                }
                toString()
            }
        }
    }

    private val KClass<*>.description
        get() = annotations
          .filterIsInstance<Description>()
          .firstOrNull()?.text ?: simpleName!!

    override fun execute(op: OptionSet): Int {
        parser.printHelpOn(System.err)
        return ERROR_SIMPLE
    }
}
