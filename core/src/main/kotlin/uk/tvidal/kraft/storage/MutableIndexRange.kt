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

    override val start: Long
        get() = range.first

    override val endInclusive: Long
        get() = range.last
}
