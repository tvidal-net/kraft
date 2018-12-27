package uk.tvidal.kraft.storage.config

import uk.tvidal.kraft.storage.KRaftFile

interface FileFactory {

    fun open(): List<KRaftFile>
    fun create(firstIndex: Long, fileIndex: Int): KRaftFile
}
