package net.tvidal.kraft.transport

import com.google.common.collect.Maps.newConcurrentMap
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.lmax.disruptor.EventFactory
import com.lmax.disruptor.EventHandler
import com.lmax.disruptor.RingBuffer
import com.lmax.disruptor.dsl.Disruptor
import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.Message
import java.util.function.Consumer

class DisruptorTransport : KRaftTransport {

    companion object {

        private const val SIZE = 64

        private val EVENT_FACTORY = EventFactory { MessageEvent() }

        private val THREAD_FACTORY = ThreadFactoryBuilder()
          .setDaemon(true)
          .setNameFormat("RaftNode%d")
          .build()
    }

    private val senders = newConcurrentMap<RaftNode, Sender>()

    override fun register(node: RaftNode, processor: Consumer<Message>) {
        senders.computeIfAbsent(node) { Sender(it, processor) }
    }

    override fun sender(node: RaftNode): MessageSender =
      senders[node] ?: throw IllegalArgumentException("Node $node has not registered a processor yet!")

    private class Sender(
      override val node: RaftNode,
      private val processor: Consumer<Message>

    ) : MessageSender {

        private val disruptor = Disruptor(EVENT_FACTORY, SIZE, THREAD_FACTORY)

        private val buffer: RingBuffer<MessageEvent>

        init {
            disruptor.handleEventsWith(EventHandler<MessageEvent> { e, _, _ -> processor.accept(e.message) })
            buffer = disruptor.start()!!
        }

        override fun send(message: Message) = buffer.publishEvent({ h, _, m -> h.message = m }, message)

        override fun respond(message: Message) = send(message)
    }
}
