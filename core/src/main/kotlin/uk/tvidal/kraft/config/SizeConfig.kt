package uk.tvidal.kraft.config

data class SizeConfig(
    val maxEntrySize: Int = 4 * 1024 * 1024,
    val maxMessageBatchSize: Int = maxEntrySize * 10,
    val maxUnackedBytesWindow: Int = maxMessageBatchSize * 2
)
