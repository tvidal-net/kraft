package uk.tvidal.kraft.message

import uk.tvidal.kraft.RaftNode

abstract class AbstractMessage(

    @Transient
    override val type: MessageType,
    override val from: RaftNode

) : Message {

    protected open val headerText: String
        get() = "$type ($from)"

    protected open fun text(): Any? = ""

    override fun toString() = "{$headerText ${text()}}"
}
