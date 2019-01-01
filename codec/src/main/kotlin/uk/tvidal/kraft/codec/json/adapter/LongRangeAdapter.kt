package uk.tvidal.kraft.codec.json.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import uk.tvidal.kraft.codec.json.nullable

class LongRangeAdapter : TypeAdapter<LongRange>() {

    companion object {
        private const val sep = ".."
    }

    override fun write(writer: JsonWriter, value: LongRange?) {
        if (value != null) {
            val from = value.first
            val to = value.last
            writer.value("$from$sep$to")
        } else writer.nullValue()
    }

    override fun read(reader: JsonReader): LongRange? = reader.nullable {
        val value = nextString()
        val values = value.split(sep)
        val from = values[0].toLong()
        val to = values[1].toLong()
        from..to
    }
}
