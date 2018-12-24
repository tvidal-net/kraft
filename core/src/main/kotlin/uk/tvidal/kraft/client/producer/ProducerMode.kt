package uk.tvidal.kraft.client.producer

enum class ProducerMode {
    FIRE_AND_FORGET,
    APPEND_IN_TERM,
    LEADER_WRITE,
    CLUSTER_COMMIT;
}
