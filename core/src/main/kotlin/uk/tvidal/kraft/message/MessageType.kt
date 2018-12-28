package uk.tvidal.kraft.message

import kotlin.reflect.KClass

interface MessageType {

    val name: String

    val messageType: KClass<out Message>?
        get() = null
}
