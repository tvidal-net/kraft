package net.tvidal.kraft

import com.google.common.reflect.ClassPath
import joptsimple.OptionParser
import kotlin.text.RegexOption.IGNORE_CASE

private val regexTool = Regex("Tool$", IGNORE_CASE)
private val regexCamel = Regex("([a-z])([A-Z])")
private const val REPLACE_CAMEL = "\$1-\$2"

private const val TOOLS_PACKAGE = "net.tvidal.kraft.tools"

private const val HELP = "help"

private val classPath = ClassPath.from(ClassLoader.getSystemClassLoader())!!

private val tools = classPath.getTopLevelClasses(TOOLS_PACKAGE)
  .filter { regexTool.containsMatchIn(it.name) }
  .map { it.load() }
  .filter { KRaftTool::class.java.isAssignableFrom(it) }
  .associateBy {
      it.simpleName
        .let { regexTool.replace(it, "") }
        .let { regexCamel.replace(it, REPLACE_CAMEL) }
        .toLowerCase()
  }

private val toolParser = OptionParser().apply {
    allowsUnrecognizedOptions()
    accepts(HELP).forHelp()
}

fun main(args: Array<String>) {
    val op = toolParser.parse(*args)
    val toolName = op.nonOptionArguments().getOrElse(0) { HELP }
    if (tools.containsKey(toolName)) {
        if (op.has(HELP)) {
            // show help for tool
        } else {
            // run tool
        }
    } else {
        // print general help
    }
}
