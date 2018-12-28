package uk.tvidal.kraft.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.logging.KRaftLogger
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer.wrap

private val log = KRaftLogger {}

private fun localNetworkSiteAddress(): InetAddress {
    try {
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
    } catch (e: Exception) {
        log.error(e)
    }
    return InetAddress.getLoopbackAddress()
}

val localNetworkSiteAddress = localNetworkSiteAddress()

fun InetAddress.toInt(): Int = with(wrap(address)) {
    getInt(0)
}

fun clientNode(name: String = "Client", inetAddress: InetAddress = localNetworkSiteAddress) =
    RaftNode(inetAddress.toInt(), name, true)
