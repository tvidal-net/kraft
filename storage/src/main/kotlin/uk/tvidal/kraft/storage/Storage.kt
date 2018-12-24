package uk.tvidal.kraft.storage

import java.util.zip.CRC32

const val DEFAULT_FILE_SIZE = 4L * 1024 * 1024 // 4 MB
const val FILE_NAME_FORMAT = "%s_%d.%s"
const val FILE_EXTENSION = "kr"
const val FILE_EXTENSION_COMMIT = "c.kr"
const val FILE_EXTENSION_DISCARD = "d.kr"
const val MAGIC_NUMBER = 0xFF

fun checksum(data: ByteArray): Int {
    val crc = CRC32()
    crc.update(data)
    return crc.value.toInt()
}
