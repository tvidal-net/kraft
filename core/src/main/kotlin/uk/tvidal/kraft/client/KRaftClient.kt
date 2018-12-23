package uk.tvidal.kraft.client

import uk.tvidal.kraft.DEFAULT_CLIENT_CLUSTER_NAME
import uk.tvidal.kraft.RaftNode
import java.net.Inet4Address
import java.net.NetworkInterface
import java.nio.ByteBuffer.wrap

fun localNetworkSiteAddress(): Inet4Address? {
    for (i in NetworkInterface.getNetworkInterfaces()) {
        if (!i.isLoopback) {
            val addresses = i.inetAddresses
            for (a in addresses) {
                if (a is Inet4Address && a.isSiteLocalAddress) {
                    return a
                }
            }
        }
    }
    return null
}

fun Inet4Address.toInt(): Int = with(wrap(address)) {
    getInt(0)
}

fun localClientIndex(): Int = localNetworkSiteAddress()?.toInt() ?: 0

fun localClientNode(name: String = DEFAULT_CLIENT_CLUSTER_NAME, index: Int = localClientIndex()) = RaftNode(index, name, true)
