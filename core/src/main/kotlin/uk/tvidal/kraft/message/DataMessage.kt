package uk.tvidal.kraft.message

interface DataMessage<T> {
    @Payload
    val data: T
}
