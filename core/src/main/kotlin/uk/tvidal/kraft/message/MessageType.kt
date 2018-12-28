package uk.tvidal.kraft.message

import kotlin.reflect.KClass

interface MessageType {

    val name: String

    val messageType: KClass<out Message>?
        get() = null

    companion object {
        val NONE = object : MessageType {
            override val name = "NONE"
        }
    }
}
