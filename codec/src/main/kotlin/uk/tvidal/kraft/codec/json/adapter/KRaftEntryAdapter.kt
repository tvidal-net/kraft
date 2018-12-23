package uk.tvidal.kraft.codec.json.adapter

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import uk.tvidal.kraft.storage.KRaftEntry
import uk.tvidal.kraft.storage.entryOf

class KRaftEntryAdapter : TypeAdapter<KRaftEntry>() {

    companion object {
        const val SEPARATOR = "::"
    }

    override fun write(writer: JsonWriter, entry: KRaftEntry) {
        with(entry) {
            writer.value("$term$SEPARATOR${String(payload)}")
        }
    }

    override fun read(reader: JsonReader): KRaftEntry {
        val value = reader.nextString()
        val data = value.split(SEPARATOR, limit = 2)
        if (data.size != 2) {
            throw JsonParseException("Cannot read '$value' as a valid KRaftEntry")
        }
        return entryOf(data[1], data[0].toLong())
    }
}
