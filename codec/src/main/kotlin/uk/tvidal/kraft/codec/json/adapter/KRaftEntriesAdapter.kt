package uk.tvidal.kraft.codec.json.adapter

import com.github.salomonbrys.kotson.typedToJson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import uk.tvidal.kraft.codec.json.gson
import uk.tvidal.kraft.codec.json.nextArray
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.entries

class KRaftEntriesAdapter : TypeAdapter<KRaftEntries>() {

    override fun write(writer: JsonWriter, entries: KRaftEntries) {
        writer.beginArray()
        for (entry in entries) {
            gson.typedToJson(entry, writer)
        }
        writer.endArray()
    }

    override fun read(reader: JsonReader): KRaftEntries = entries(
        reader.nextArray()
    )
}
