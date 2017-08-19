package net.tvidal.kraft

import joptsimple.HelpFormatter
import joptsimple.OptionDescriptor
import joptsimple.internal.Strings
import joptsimple.internal.Strings.EMPTY

object KRaftHelpFormatter : HelpFormatter {

    private const val OPTION = "Option"
    private const val DESCRIPTION = "Description"

    private const val MARGIN = 2
    private const val DEFAULT_WIDTH = 10
    private const val MIN_WIDTH = OPTION.length + MARGIN

    override fun format(allOptions: Map<String, OptionDescriptor>) = StringBuffer().run {
        val options = allOptions.values
          .filterNot { it.representsNonOptions() }

        val width = maxOf(width(options.map { optionText(it) }), MIN_WIDTH)
        fun align(text: String) {
            append(align(text, width))
        }

        align(OPTION)
        appendln(DESCRIPTION)

        align(dashes(OPTION))
        appendln(dashes(DESCRIPTION))

        for (spec in options) {
            align(optionText(spec))
            appendln(optionDescription(spec))
        }
        toString()
    }

    private fun width(items: Iterable<String>) = (items
      .map(String::length)
      .max() ?: DEFAULT_WIDTH) + MARGIN

    fun argumentType(spec: OptionDescriptor) = Class.forName(spec.argumentTypeIndicator()).kotlin.simpleName

    fun optionText(spec: OptionDescriptor) = dashes(2) + spec.options().last() +
      if (spec.requiresArgument()) " <${argumentType(spec)}>" else EMPTY +
        if (spec.acceptsArguments()) " [${argumentType(spec)}]" else EMPTY

    fun optionDescription(spec: OptionDescriptor) = spec.description() +
      if (!spec.defaultValues().isEmpty()) " (default: ${spec.defaultValues()})" else EMPTY

    fun align(text: String, width: Int) = text + spaces(width - text.length)

    fun dashes(text: String) = dashes(text.length)
    fun dashes(count: Int) = Strings.repeat('-', count)
    fun spaces(count: Int) = Strings.repeat(' ', count)
}
