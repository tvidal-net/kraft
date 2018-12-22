package uk.tvidal.kraft.tools

import joptsimple.OptionParser
import joptsimple.OptionSet
import uk.tvidal.kraft.DEFAULT_MARGIN
import uk.tvidal.kraft.DEFAULT_WIDTH
import uk.tvidal.kraft.Description
import uk.tvidal.kraft.ERROR_SIMPLE
import uk.tvidal.kraft.HELP_DESCRIPTION
import uk.tvidal.kraft.KRaftTool
import uk.tvidal.kraft.SPACE
import uk.tvidal.kraft.TOOLS
import kotlin.reflect.KClass

@Description(HELP_DESCRIPTION)
class HelpTool(private val parser: OptionParser) : KRaftTool {

    init {
        parser.formatHelpWith {
            val width = (TOOLS.keys.map { it.length + 4 }.max() ?: DEFAULT_WIDTH) + DEFAULT_MARGIN

            StringBuffer().run {
                appendln("Usage: kraft <tool-name> [args] (or --help)")
                appendln()
                appendln("Available Tools:")
                for ((toolName, toolClass) in TOOLS) {
                    append(" - $toolName:".padEnd(width, SPACE))
                    appendln(toolClass.description)
                }
                String(this)
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
