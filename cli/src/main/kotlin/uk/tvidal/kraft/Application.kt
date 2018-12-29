package uk.tvidal.kraft

import com.google.common.reflect.ClassPath
import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSet
import uk.tvidal.kraft.ansi.AnsiColor.RED
import uk.tvidal.kraft.ansi.AnsiColor.YELLOW
import uk.tvidal.kraft.tool.help.EMPTY
import uk.tvidal.kraft.tool.help.KRaftHelpFormatter
import java.lang.System.exit
import java.lang.System.getProperty
import java.lang.System.setProperty
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.text.RegexOption.IGNORE_CASE

const val SUCCESS = 0
const val ERROR_SIMPLE = 1
const val ERROR_SEVERE = 127

const val HELP = "help"
const val HELP_DESCRIPTION = "Prints usage information"

private const val LOGBACK_CONFIG = "logback.configurationFile"
const val LOGBACK_CONSOLE = "logback-console.xml"

val ERROR = RED.format("[ERROR]")

@Suppress("UnstableApiUsage")
private val CLASS_PATH = ClassPath.from(ClassLoader.getSystemClassLoader())!!

private val REGEX_TOOL = Regex("Tool$", IGNORE_CASE)
private val REGEX_CAMEL = Regex("([a-z])([A-Z])")
private const val REPLACE_CAMEL = "\$1-\$2"

private const val TOOLS_PACKAGE = "uk.tvidal.kraft.tool"

var logbackConfigurationFile: String
    get() = getProperty(LOGBACK_CONFIG)
    set(value) {
        setProperty(LOGBACK_CONFIG, value)
    }

val TOOLS = CLASS_PATH.getTopLevelClasses(TOOLS_PACKAGE)
    .filter { REGEX_TOOL.containsMatchIn(it.name) }
    .map { it.load().kotlin }
    .filter { KRaftTool::class.isSuperclassOf(it) }
    .associateBy {
        it.simpleName!!
            .let { REGEX_TOOL.replace(it, EMPTY) }
            .let { REGEX_CAMEL.replace(it, REPLACE_CAMEL) }
            .toLowerCase()
    }

private fun optionParser(allowsUnrecognizedOptions: Boolean = false) = OptionParser().apply {
    if (allowsUnrecognizedOptions) allowsUnrecognizedOptions()
    accepts(HELP, HELP_DESCRIPTION).forHelp()
    formatHelpWith(KRaftHelpFormatter)
}

private fun createTool(toolName: String, parser: OptionParser): KRaftTool {
    val toolClass = TOOLS[toolName]!!
    val ctor = toolClass.primaryConstructor!!
    val tool = ctor.call(parser)
    return tool as KRaftTool
}

private fun executeTool(toolName: String, vararg args: String): Int {
    val parser = optionParser()
    val printHelp = { parser.printHelpOn(System.err); ERROR_SIMPLE }

    val tool = createTool(toolName, parser)
    return try {
        val op = parser.parse(*args)

        if (op.has(HELP)) printHelp()
        else tool.execute(op)
    } catch (e: OptionException) {
        System.err.println("$ERROR ${e.message}\n")
        printHelp()
    } catch (e: Throwable) {
        e.printStackTrace()
        ERROR_SEVERE
    }
}

fun execute(op: OptionSet): Int {
    val args = op.nonOptionArguments().filterIsInstance<String>()
    val toolName = args.getOrNull(0) ?: HELP

    return if (TOOLS.containsKey(toolName)) {
        val toolArgs = if (op.has(HELP)) listOf("--$HELP") else args.drop(1)
        executeTool(toolName, *toolArgs.toTypedArray())
    } else {
        System.err.println("$ERROR The tool '${YELLOW.format(toolName)}' does not exist.\n")
        executeTool(HELP)
    }
}

fun logbackConsoleConfiguration() {
    logbackConfigurationFile = LOGBACK_CONSOLE
}

fun main(args: Array<String>) {
    logbackConsoleConfiguration()
    val parser = optionParser(true)
    val op = parser.parse(*args)
    val ret = execute(op)
    exit(ret)
}
