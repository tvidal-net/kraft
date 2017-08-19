package net.tvidal.kraft

import com.google.common.reflect.ClassPath
import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSet
import joptsimple.internal.Strings
import java.lang.System.exit
import kotlin.reflect.full.cast
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.text.RegexOption.IGNORE_CASE

const val SUCCESS = 0
const val SIMPLE_ERROR = 1
const val SEVERE_ERROR = 127

const val HELP = "help"
const val HELP_DESCRIPTION = "Shows usage information"

const val ERROR = "[ERROR]"

private val regexTool = Regex("Tool$", IGNORE_CASE)
private val regexCamel = Regex("([a-z])([A-Z])")
private const val REPLACE_CAMEL = "\$1-\$2"

private const val TOOLS_PACKAGE = "net.tvidal.kraft.tools"

private val classPath = ClassPath.from(ClassLoader.getSystemClassLoader())!!

val TOOLS = classPath.getTopLevelClasses(TOOLS_PACKAGE)
  .filter { regexTool.containsMatchIn(it.name) }
  .map { it.load().kotlin }
  .filter { KRaftTool::class.isSuperclassOf(it) }
  .associateBy {
      it.simpleName!!
        .let { regexTool.replace(it, Strings.EMPTY) }
        .let { regexCamel.replace(it, REPLACE_CAMEL) }
        .toLowerCase()
  }

private fun optionParser(allowsUnrecognizedOptions: Boolean = false) = OptionParser().apply {
    if (allowsUnrecognizedOptions) allowsUnrecognizedOptions()
    accepts(HELP, HELP_DESCRIPTION).forHelp()
    formatHelpWith(KRaftHelpFormatter)
}

private operator fun OptionParser.invoke(vararg args: String) = try {
    parse(*args)
} catch (e: OptionException) {
    System.err.println("$ERROR ${e.message}\n")
    null
}

private fun createTool(toolName: String, parser: OptionParser): KRaftTool {
    val toolClass = TOOLS[toolName]!!
    val ctor = toolClass.primaryConstructor!!
    val tool = ctor.call(parser)
    return KRaftTool::class.cast(tool)
}

private fun executeTool(toolName: String, vararg args: String): Int {
    val parser = optionParser()
    val tool = createTool(toolName, parser)
    val op = parser(*args)
    if (op == null || op.has(HELP)) {
        parser.printHelpOn(System.err)
        return SIMPLE_ERROR
    }
    return try {
        tool.execute(op)
    } catch (e: Throwable) {
        e.printStackTrace()
        SEVERE_ERROR
    }
}

fun execute(op: OptionSet): Int {
    val args = op.nonOptionArguments().filterIsInstance<String>()
    val toolName = args.getOrNull(0) ?: HELP
    return if (TOOLS.containsKey(toolName)) {
        val toolArgs = if (op.has(HELP)) listOf("--$HELP") else args.drop(1)
        executeTool(toolName, *toolArgs.toTypedArray())
    } else {
        System.err.println("$ERROR The tool '$toolName' does not exist.\n")
        executeTool(HELP)
        SIMPLE_ERROR
    }
}

fun main(args: Array<String>) {
    val parser = optionParser(true)
    val op = parser.parse(*args)
    val ret = execute(op)
    exit(ret)
}
