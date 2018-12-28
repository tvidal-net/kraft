package uk.tvidal.kraft.message

abstract class AbstractMessage : Message {

    protected open val headerText: String
        get() = "$type ($from)"

    protected open fun text(): Any? = ""

    override fun toString() = "{$headerText ${text()}}"
}
