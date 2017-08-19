package net.tvidal.kraft

import joptsimple.HelpFormatter
import joptsimple.OptionDescriptor
import joptsimple.internal.Strings
import joptsimple.internal.Strings.EMPTY

internal object KRaftHelpFormatter : HelpFormatter {

    private const val OPTION = "Option"
    private const val DESCRIPTION = "Description"

    private const val MARGIN = 2
    private const val DEFAULT_WIDTH = 10
    private const val MIN_WIDTH = OPTION.length + MARGIN

    override fun format(allOptions: Map<String, OptionDescriptor>) = StringBuffer().run {
        val options = allOptions.values
          .filterNot { it.representsNonOptions() }

        val width = maxOf(width(options.map { it.text }), MIN_WIDTH)
        fun align(text: String) {
            append(align(text, width))
        }

        align(OPTION)
        appendln(DESCRIPTION)

        align(dashes(OPTION))
        appendln(dashes(DESCRIPTION))

        for (spec in options) {
            align(spec.text)
            appendln(spec.description)
        }
        toString()
    }

    private fun width(items: Iterable<String>) = (items
      .map(String::length)
      .max() ?: DEFAULT_WIDTH) + MARGIN

    private val OptionDescriptor.argumentType
        get() = Class.forName(argumentTypeIndicator()).kotlin.simpleName

    private val OptionDescriptor.text
        get() = dashes(2) + options().last() +
          if (requiresArgument()) " <$argumentType>" else EMPTY +
            if (acceptsArguments()) " [$argumentType]" else EMPTY

    private val OptionDescriptor.description
        get() = description() +
          if (!defaultValues().isEmpty()) " (default: ${defaultValues()})"
          else EMPTY

    private fun align(text: String, width: Int) = text + spaces(width - text.length)

    private fun dashes(text: String) = dashes(text.length)
    private fun dashes(count: Int) = Strings.repeat('-', count)
    private fun spaces(count: Int) = Strings.repeat(' ', count)
}
