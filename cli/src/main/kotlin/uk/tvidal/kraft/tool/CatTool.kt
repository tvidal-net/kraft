package uk.tvidal.kraft.tool

import joptsimple.OptionParser
import joptsimple.OptionSet
import joptsimple.OptionSpec
import uk.tvidal.kraft.Description
import uk.tvidal.kraft.KRaftTool
import uk.tvidal.kraft.ansi.AnsiColor.BLACK
import uk.tvidal.kraft.ansi.AnsiColor.BLUE
import uk.tvidal.kraft.ansi.AnsiColor.CYAN
import uk.tvidal.kraft.ansi.AnsiColor.MAGENTA
import uk.tvidal.kraft.ansi.AnsiColor.YELLOW
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.uuid
import uk.tvidal.kraft.intArgument
import uk.tvidal.kraft.longArgument
import uk.tvidal.kraft.option
import uk.tvidal.kraft.otherArguments
import uk.tvidal.kraft.storage.config.FileName.Companion.DATA_FILE
import uk.tvidal.kraft.storage.config.FileName.Companion.INDEX_FILE
import uk.tvidal.kraft.storage.data.KRaftData
import uk.tvidal.kraft.storage.index.KRaftIndex
import java.io.File

@Description("View the contents of a kraft data or index file")
class CatTool(parser: OptionParser) : KRaftTool {

    private val opFromIndex = parser
        .longArgument("from index", "fromIndex")

    private val opToIndex = parser
        .longArgument("to index", "toIndex")

    private val opCount = parser
        .intArgument("count", "count")

    private val opAll = parser
        .option("prints all entries", "all")

    private val opNoHeader = parser
        .option("removes the header", "no-header")

    override fun execute(op: OptionSet): Int {

        for (fileName in op.otherArguments()) {
            catFile(op, fileName.toLowerCase())
        }
        return 0
    }

    private fun catFile(op: OptionSet, fileName: String) {
        val file = File(fileName)
        when {
            !file.exists() -> System.err.println("$fileName: does not exist")
            fileName.endsWith(INDEX_FILE) -> catIndexFile(op, file)
            fileName.endsWith(DATA_FILE) -> catDataFile(op, file)
        }
    }

    private fun catIndexFile(op: OptionSet, file: File) {
        KRaftIndex(file).use {

            if (op.printEntries) {
                val range = op.range(it)
                if (!op.has(opNoHeader)) {
                    csv(
                        MAGENTA.format("file"),
                        YELLOW.format("index"),
                        BLACK.format("id"),
                        CYAN.format("offset"),
                        BLUE.format("bytes"),
                        "checksum"
                    )
                }
                for (index in range) {
                    it[index].run {
                        csv(
                            MAGENTA.format(file),
                            YELLOW.format(index),
                            BLACK.format(uuid(id)),
                            CYAN.format(offset),
                            BLUE.format(bytes)
                        )
                    }
                }
            } else {
                val range = it.range
                val size = it.size
                val last = it[range.last]
                val bytes = last.offset + last.bytes
                println("$file: $range size=$size bytes=$bytes")
            }
        }
    }

    private fun catDataFile(op: OptionSet, file: File) {
        KRaftData.open(file).let {

            if (op.printEntries) {
                val data = it.rebuildIndex().associateBy(IndexEntry::getIndex)
                val range = op.range(it)
                if (!op.has(opNoHeader)) {
                    csv(
                        MAGENTA.format("file"),
                        YELLOW.format("index"),
                        BLACK.format("id"),
                        CYAN.format("offset"),
                        BLUE.format("bytes"),
                        "payload"
                    )
                }
                for (index in range) {
                    val indexEntry = data[index]!!
                    val entry = it[indexEntry]
                    csv(
                        MAGENTA.format(file),
                        YELLOW.format(index),
                        BLACK.format(entry.id),
                        CYAN.format(indexEntry.offset),
                        BLUE.format(indexEntry.bytes),
                        String(entry.payload)
                    )
                }
            } else {
                val range = it.range
                val size = it.size
                val state = it.state
                val bytes = file.length()
                println("$file: $range $state size=$size bytes=$bytes")
            }
        }
    }

    private fun csv(vararg s: Any) {
        println(s.joinToString("\t", transform = Any::toString))
    }

    private fun OptionSet.range(default: ClosedRange<Long>): LongRange {
        val fromIndex = valueOf(opFromIndex) ?: default.start
        val toIndex = if (has(opCount)) fromIndex + valueOf(opCount)
        else valueOf(opToIndex) ?: default.endInclusive
        return fromIndex..toIndex
    }

    private val OptionSet.printEntries
        get() = sequenceOf<OptionSpec<*>>(opAll, opCount, opFromIndex, opToIndex)
            .any { has(it) }
}
