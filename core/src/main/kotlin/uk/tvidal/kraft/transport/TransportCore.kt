package uk.tvidal.kraft.transport

import uk.tvidal.kraft.message.Message

typealias MessageReader = Iterable<Message>
typealias MessageWriter = (Message) -> Unit
