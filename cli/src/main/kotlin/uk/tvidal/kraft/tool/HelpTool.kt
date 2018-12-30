package uk.tvidal.kraft.tool

import joptsimple.OptionParser
import joptsimple.OptionSet
import uk.tvidal.kraft.Description
import uk.tvidal.kraft.ERROR_SIMPLE
import uk.tvidal.kraft.HELP_DESCRIPTION
import uk.tvidal.kraft.KRaftTool
import uk.tvidal.kraft.tools
import uk.tvidal.kraft.tool.help.DEFAULT_MARGIN
import uk.tvidal.kraft.tool.help.DEFAULT_WIDTH
import uk.tvidal.kraft.tool.help.SPACE
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@Description(HELP_DESCRIPTION)
class HelpTool(private val parser: OptionParser) : KRaftTool {

    init {
        parser.formatHelpWith {
            val maxWidth = tools.keys
                .map { key -> key.length + 4 }
                .max()

            val width = (maxWidth ?: DEFAULT_WIDTH) + DEFAULT_MARGIN

            buildString {
                appendln("Usage: kraft <tool-name> [args] (or --help)")
                appendln()
                appendln("Available Tools:")
                for ((toolName, toolClass) in tools) {
                    append(" - $toolName:".padEnd(width, SPACE))
                    appendln(toolClass.description)
                }
            }
        }
    }

    private val KClass<*>.description: String
        get() = findAnnotation<Description>()?.value ?: simpleName!!

    override fun execute(op: OptionSet): Int {
        parser.printHelpOn(System.err)
        return ERROR_SIMPLE
    }
}
