package net.tvidal.kraft.config

data class SizeConfig(
    val maxEntrySize: Int,
    val maxMessageBatchSize: Int,
    val maxUnackedBytesWindow: Int
)
