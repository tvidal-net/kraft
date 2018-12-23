package uk.tvidal.kraft.message

import kotlin.reflect.KClass

interface MessageType {

    object NONE : MessageType {
        override val name = "NONE"
    }

    val name: String

    val messageType: KClass<out Message>?
        get() = null
}
