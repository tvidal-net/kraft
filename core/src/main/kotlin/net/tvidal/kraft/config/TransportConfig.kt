package net.tvidal.kraft.config

interface TransportConfig<out T : TransportNodeConfig> {

    val nodes: List<T>

}
