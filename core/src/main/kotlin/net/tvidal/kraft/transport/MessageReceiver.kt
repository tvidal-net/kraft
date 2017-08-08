package net.tvidal.kraft.transport

import net.tvidal.kraft.message.Message

interface MessageReceiver {

    fun poll(): Message

}
