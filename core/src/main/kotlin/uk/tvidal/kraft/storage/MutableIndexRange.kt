package uk.tvidal.kraft.storage

interface MutableIndexRange : ClosedRange<Long> {

    var range: LongRange

    var firstIndex: Long
        get() = range.first
        set(newFirst) {
            range = newFirst..lastIndex
        }

    var lastIndex: Long
        get() = range.last
        set(newLast) {
            range = firstIndex..newLast
        }

    var size: Int
        get() = (lastIndex - firstIndex + 1).toInt()
        set (newSize) {
            lastIndex = firstIndex + newSize - 1
        }

    override val start: Long
        get() = range.first

    override val endInclusive: Long
        get() = range.last
}
