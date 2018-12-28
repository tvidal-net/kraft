package uk.tvidal.kraft.message

import uk.tvidal.kraft.storage.KRaftEntries

interface DataMessage {
    @Payload
    val data: KRaftEntries
}
