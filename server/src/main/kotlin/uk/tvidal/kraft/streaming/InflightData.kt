package uk.tvidal.kraft.streaming

data class InflightData(
    val firstIndex: Long,
    val size: Int,
    val bytes: Int
) {
    val lastIndex: Long
        get() = firstIndex + size - 1
}
