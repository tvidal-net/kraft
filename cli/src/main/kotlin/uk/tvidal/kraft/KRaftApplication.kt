package uk.tvidal.kraft

import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSet
import uk.tvidal.kraft.ansi.AnsiColor.RED
import uk.tvidal.kraft.ansi.AnsiColor.YELLOW
import uk.tvidal.kraft.tool.CatTool
import uk.tvidal.kraft.tool.ConsumerTool
import uk.tvidal.kraft.tool.HelpTool
import uk.tvidal.kraft.tool.ClusterTool
import java.lang.System.exit
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object KRaftApplication {

    const val SUCCESS = 0
    const val ERROR_SIMPLE = 1
    const val ERROR_SEVERE = 127

    const val HELP = "help"
    const val HELP_DESCRIPTION = "print usage information"

    private val ERROR = RED("[ERROR]")

    private val toolNameFix = Regex("Tool?$")
    private val camelCaseFix = Regex("([a-z][A-Z])")

    internal val tools: Map<String, KClass<out KRaftTool>> = listOf(
        HelpTool::class,
        CatTool::class,
        ConsumerTool::class,
        ClusterTool::class
    ).associateBy(::toolName)

    private fun toolName(kClass: KClass<out KRaftTool>): String = kClass
        .simpleName!!
        .replace(toolNameFix, "")
        .replace(camelCaseFix, "\$1-\$2")
        .toLowerCase()

    private fun optionParser(allowsUnrecognizedOptions: Boolean = false) = OptionParser().apply {
        if (allowsUnrecognizedOptions) {
            allowsUnrecognizedOptions()
        }
        accepts(HELP, HELP_DESCRIPTION)
            .forHelp()
    }.also { LogConfig(it) }

    private fun createTool(kClass: KClass<out KRaftTool>, parser: OptionParser): KRaftTool = kClass
        .primaryConstructor!!
        .call(parser)

    private fun help() = executeTool(HelpTool::class)

    private fun OptionParser.printHelp(): Int = printHelpOn(System.err)
        .let { ERROR_SIMPLE }

    private fun executeTool(toolName: String, args: Collection<String>) =
        executeTool(tools[toolName]!!, args)

    private fun executeTool(kClass: KClass<out KRaftTool>, args: Collection<String> = emptyList()): Int {
        val parser = optionParser()
        val tool = createTool(kClass, parser)
        return try {
            val op = parser.parse(*args.toTypedArray())
            if (op.has(HELP)) parser.printHelp()
            else tool.execute(op)
        } catch (e: OptionException) {
            System.err.println("$ERROR ${e.message}\n")
            parser.printHelp()
        } catch (e: Throwable) {
            e.printStackTrace()
            ERROR_SEVERE
        }
    }

    private fun execute(op: OptionSet): Int {
        val args = op.extraArguments()
        val toolName = args.firstOrNull()
            ?: return help()

        return if (tools.containsKey(toolName)) {
            val toolArgs = if (op.has(HELP)) listOf("--$HELP") else args.drop(1)
            executeTool(toolName, toolArgs)
        } else {
            val yellowName = YELLOW(toolName)
            System.err.println("$ERROR The tool '$yellowName' does not exist.\n")
            help()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val parser = optionParser(true)
        val op = parser.parse(*args)
        LogConfig(op)

        val ret = execute(op)
        exit(ret)
    }
}
