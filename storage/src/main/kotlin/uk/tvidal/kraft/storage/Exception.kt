package uk.tvidal.kraft.storage

import java.io.IOException

open class KRaftFileStorageException(
    message: String,
    cause: Throwable? = null
) : IOException(message, cause)

class WriteToImmutableFileException(message: String) : KRaftFileStorageException(message)
class ModifyCommittedFileException(message: String) : KRaftFileStorageException(message)
class TruncateOutOfRangeException(message: String) : KRaftFileStorageException(message)
class IndexOutOfRangeException(message: String) : KRaftFileStorageException(message)
class FileSequenceGapException(message: String) : KRaftFileStorageException(message)
class CorruptedFileException(message: String) : KRaftFileStorageException(message)
