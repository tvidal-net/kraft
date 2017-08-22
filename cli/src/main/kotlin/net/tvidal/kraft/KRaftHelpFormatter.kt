package net.tvidal.kraft

import joptsimple.HelpFormatter
import joptsimple.OptionDescriptor
import joptsimple.internal.Strings
import joptsimple.internal.Strings.EMPTY

internal object KRaftHelpFormatter : HelpFormatter {

    internal const val OPTION = "Option"
    internal const val DESCRIPTION = "Description"

    override fun format(allOptions: Map<String, OptionDescriptor>) = StringBuffer().run {
        val options = allOptions.values
          .filterNot { it.representsNonOptions() }

        val width = maxOf(colWidth(options.map { it.text }), MIN_WIDTH)
        fun align(text: String) {
            append(align(text, xwidth))
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
}
