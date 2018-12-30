package uk.tvidal.kraft

import com.google.common.reflect.ClassPath
import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSet
import uk.tvidal.kraft.ansi.AnsiColor.RED
import uk.tvidal.kraft.ansi.AnsiColor.YELLOW
import java.lang.System.exit
import java.lang.System.getProperty
import java.lang.System.setProperty
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

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

const val TOOLS_PACKAGE = "uk.tvidal.kraft.tool"

private val toolNameFix = Regex("Tool?$")
private val camelCaseFix = Regex("([a-z][A-Z])")

var logbackConfigurationFile: String
    get() = getProperty(LOGBACK_CONFIG)
    set(value) {
        setProperty(LOGBACK_CONFIG, value)
    }

val TOOLS = CLASS_PATH.getTopLevelClasses(TOOLS_PACKAGE)
    .map { jClass -> jClass.load().kotlin }
    .filterIsInstance<KClass<out KRaftTool>>()
    .associateBy(::toolName)

private fun toolName(kClass: KClass<out KRaftTool>): String = kClass
    .simpleName!!
    .replace(toolNameFix, "")
    .replace(camelCaseFix, "\$1-\$2")
    .toLowerCase()

fun optionParser(allowsUnrecognizedOptions: Boolean = false) = OptionParser().apply {
    if (allowsUnrecognizedOptions) allowsUnrecognizedOptions()
    accepts(HELP, HELP_DESCRIPTION).forHelp()
}

private fun createTool(toolName: String, parser: OptionParser): KRaftTool = TOOLS[toolName]
    ?.primaryConstructor
    ?.call(parser)!!

private fun OptionParser.printHelp(): Int = printHelpOn(System.err)
    .let { ERROR_SIMPLE }

private fun executeTool(toolName: String, args: List<String> = emptyList()): Int {
    val parser = optionParser()

    val tool = createTool(toolName, parser)
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

fun execute(op: OptionSet): Int {
    val args = op.nonOptionArguments().filterIsInstance<String>()
    val toolName = args.firstOrNull() ?: HELP

    return if (TOOLS.containsKey(toolName)) {
        val toolArgs = if (op.has(HELP)) listOf("--$HELP") else args.drop(1)
        executeTool(toolName, toolArgs)
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
