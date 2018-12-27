package uk.tvidal.kraft.storage.config

import uk.tvidal.kraft.storage.KRaftFile

interface FileFactory {

    fun open(): Map<LongRange, KRaftFile>
    fun create(firstIndex: Long, fileIndex: Int): KRaftFile
}
