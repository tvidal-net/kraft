package net.tvidal.kraft.config

interface SizeConfig {

    val maxEntrySize: Int

    val maxMessageBatchSize: Int

    val maxUnackedBytesWindow: Int

}
