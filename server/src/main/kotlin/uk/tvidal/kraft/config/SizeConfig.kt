package uk.tvidal.kraft.config

data class SizeConfig(
    val entrySize: Int = 4096,
    val batchSize: Int = entrySize * 10,
    val unackedBytes: Int = batchSize * 2
)
