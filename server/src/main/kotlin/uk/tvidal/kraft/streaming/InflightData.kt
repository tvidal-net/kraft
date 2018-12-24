package uk.tvidal.kraft.streaming

data class InflightData(
    val firstIndex: Long,
    val size: Int,
    val bytes: Int
) : Comparable<InflightData> {

    val lastIndex: Long
        get() = firstIndex + size - 1

    override fun compareTo(other: InflightData): Int = (firstIndex - other.firstIndex).toInt()
}
