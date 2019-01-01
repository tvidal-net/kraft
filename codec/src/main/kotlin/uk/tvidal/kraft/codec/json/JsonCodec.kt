package uk.tvidal.kraft.codec.json

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonToken.NULL
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.codec.json.adapter.KRaftEntriesAdapter
import uk.tvidal.kraft.codec.json.adapter.KRaftEntryAdapter
import uk.tvidal.kraft.codec.json.adapter.LongRangeAdapter
import uk.tvidal.kraft.codec.json.adapter.RaftNodeAdapter
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.KRaftEntry

val gson = GsonBuilder()
    .registerTypeAdapter<RaftNode>(RaftNodeAdapter())
    .registerTypeAdapter<LongRange>(LongRangeAdapter())
    .registerTypeAdapter<KRaftEntry>(KRaftEntryAdapter())
    .registerTypeAdapter<KRaftEntries>(KRaftEntriesAdapter())
    .setPrettyPrinting()
    .create()!!

inline fun <T> JsonReader.nullable(block: JsonReader.(JsonToken) -> T): T? {
    val token = peek()
    return if (token == NULL) null
    else block(token)
}

inline fun <reified T : Any> JsonReader.nextArray(): List<T> = sequence {
    beginArray()
    while (hasNext()) {
        val value = gson.fromJson<T>(this@nextArray)
        yield(value)
    }
    endArray()
}.toList()
