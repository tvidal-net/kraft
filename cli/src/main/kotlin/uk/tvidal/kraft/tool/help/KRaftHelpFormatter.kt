package uk.tvidal.kraft.tool.help

import joptsimple.HelpFormatter
import joptsimple.OptionDescriptor

internal object KRaftHelpFormatter : HelpFormatter {

    private const val OPTION = "Option"
    private const val DESCRIPTION = "Description"

    private val OPTION_PREFIX = dashes(2)

    private const val MIN_WIDTH = OPTION.length + DEFAULT_MARGIN

    override fun format(allOptions: Map<String, OptionDescriptor>) = StringBuffer().run {
        val options = allOptions.values
            .filterNot { it.representsNonOptions() }

        val width = maxOf(
            colWidth(options.map { it.text }),
            MIN_WIDTH
        )

        fun appendColumn(text: String) {
            append(text.padEnd(width, SPACE))
        }

        appendColumn(OPTION)
        appendln(DESCRIPTION)

        appendColumn(dashes(OPTION))
        appendln(dashes(DESCRIPTION))

        for (spec in options) {
            appendColumn(spec.text)
            appendln(spec.description)
        }
        String(this)
    }

    private val OptionDescriptor.argumentType
        get() = Class.forName(argumentTypeIndicator()).kotlin.simpleName

    private val OptionDescriptor.text
        get() = OPTION_PREFIX + options().last() +
            if (requiresArgument()) " <$argumentType>" else EMPTY +
                if (acceptsArguments()) " [$argumentType]" else EMPTY

    private val OptionDescriptor.description
        get() = description() +
            if (!defaultValues().isEmpty()) " (default: ${defaultValues()})" else EMPTY
}
