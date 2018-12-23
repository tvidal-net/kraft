package uk.tvidal.kraft.codec

import com.github.salomonbrys.kotson.fromJson
import uk.tvidal.kraft.client.localClientNode
import uk.tvidal.kraft.codec.json.gson
import uk.tvidal.kraft.message.client.ClientAppendMessage
import uk.tvidal.kraft.storage.entryOf

fun main(args: Array<String>) {
    val from = localClientNode()
    val payload = entryOf("Hello World").toEntries()

    val msg = ClientAppendMessage(from, payload)
    val s = gson.toJson(msg)
    println(s)

    val json = gson.fromJson<ClientAppendMessage>(s)
    println(json)
}
