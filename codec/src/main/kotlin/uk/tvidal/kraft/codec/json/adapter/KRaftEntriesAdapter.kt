package uk.tvidal.kraft.codec.json.adapter

import com.github.salomonbrys.kotson.getAdapter
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import uk.tvidal.kraft.codec.json.gson
import uk.tvidal.kraft.codec.json.nextArray
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.KRaftEntry
import uk.tvidal.kraft.storage.entries

class KRaftEntriesAdapter : TypeAdapter<KRaftEntries>() {

    val entryAdapter by lazy { gson.getAdapter<KRaftEntry>() }

    override fun write(writer: JsonWriter, entries: KRaftEntries) {
        writer.beginArray()
        for (entry in entries) {
            entryAdapter.write(writer, entry)
        }
        writer.endArray()
    }

    override fun read(reader: JsonReader): KRaftEntries = entries(
        reader.nextArray(entryAdapter)
    )
}
