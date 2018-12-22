package uk.tvidal.kraft.logging

abstract class KRaftLogging {

    val log by lazy {
        KRaftLogger(this::class)
    }
}
