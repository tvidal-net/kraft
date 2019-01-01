package uk.tvidal.kraft.client.producer

enum class ClientAckType(val ackExpected: Boolean) {
    FIRE_AND_FORGET(false),
    APPEND_IN_TERM(false),
    LEADER_WRITE(true),
    CLUSTER_COMMIT(true);
}
