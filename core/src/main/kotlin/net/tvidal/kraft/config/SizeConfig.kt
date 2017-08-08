package net.tvidal.kraft.config

interface SizeConfig {

    val maxEntrySize: Long

    val maxMessageBatchSize: Long

    val maxUnackedBytesWindow: Long

}
