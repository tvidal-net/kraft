package uk.tvidal.kraft.monitor

import com.sun.management.GarbageCollectionNotificationInfo
import com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION
import uk.tvidal.kraft.duration
import uk.tvidal.kraft.logging.KRaftLogger
import java.lang.management.ManagementFactory
import javax.management.Notification
import javax.management.NotificationEmitter
import javax.management.NotificationListener
import javax.management.openmbean.CompositeData

object GarbageCollectorListener : NotificationListener {

    private val log = KRaftLogger {}

    private const val GC_NOTIFICATION = GARBAGE_COLLECTION_NOTIFICATION

    init {
        garbageCollectorBeans()
            .filterIsInstance<NotificationEmitter>()
            .forEach(this::register)
    }

    fun install() = Unit

    override fun handleNotification(notification: Notification?, handback: Any?) {
        if (notification?.type == GC_NOTIFICATION) {
            val data = notification.userData as CompositeData
            val info = GarbageCollectionNotificationInfo.from(data)
            handleNotification(info)
        } else log.trace { notification }
    }

    private fun handleNotification(info: GarbageCollectionNotificationInfo) {
        val gcInfo = info.gcInfo
        val duration = duration(gcInfo.duration)
        log.info { "GarbageCollector duration=$duration name=[${info.gcName}] action=[${info.gcAction}] cause=[${info.gcCause}]" }
    }

    private fun garbageCollectorBeans() = ManagementFactory.getGarbageCollectorMXBeans()

    private fun register(emitter: NotificationEmitter) {
        emitter.addNotificationListener(this, null, null)
    }
}
