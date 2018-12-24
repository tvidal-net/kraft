package uk.tvidal.kraft.consumer

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.client.consumer.FIRST_INDEX
import uk.tvidal.kraft.client.consumer.LAST_INDEX
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.client.ClientErrorMessage
import uk.tvidal.kraft.message.client.ClientErrorType
import uk.tvidal.kraft.message.client.ClientErrorType.CONSUME_AFTER_COMMIT
import uk.tvidal.kraft.message.client.ClientErrorType.CONSUME_BEFORE_LOG
import uk.tvidal.kraft.message.client.ConsumerAckMessage
import uk.tvidal.kraft.message.client.ConsumerDataMessage
import uk.tvidal.kraft.message.client.ConsumerRegisterMessage
import uk.tvidal.kraft.storage.KRaftStorage
import uk.tvidal.kraft.transport.KRaftTransport

class RaftConsumerState(
    private val transport: KRaftTransport,
    private val storage: KRaftStorage,
    private var commitIndex: Long
) {
    private companion object : KRaftLogging()

    val self: RaftNode
        get() = transport.self

    private val consumers = mutableMapOf<RaftNode, RaftConsumer>()

    fun commit(index: Long) {
        commitIndex = index
        consumers
            .values
            .forEach(this::updateStreaming)
    }

    fun register(message: ConsumerRegisterMessage) {
        if (validateIndex(message.from, message.fromIndex)) {
            val index = computeIndex(message.fromIndex)
            val consumer = RaftConsumer(message.from, index, commitIndex)
            consumers[message.from] = consumer
            log.info { "$self consumerRegister msg=$message $consumer " }
            updateData(consumer)
        }
    }

    private fun validateIndex(node: RaftNode, index: Long): Boolean {
        val error = when {
            index <= 0 -> null
            index > commitIndex -> CONSUME_AFTER_COMMIT
            index < storage.firstLogIndex -> CONSUME_BEFORE_LOG
            else -> null
        }
        if (error != null) {
            error(node, error)
            return false
        }
        return true
    }

    private fun computeIndex(fromIndex: Long): Long = when (fromIndex) {
        FIRST_INDEX -> storage.firstLogIndex
        LAST_INDEX -> commitIndex + 1
        else -> fromIndex
    }

    fun ack(message: ConsumerAckMessage) {
        val consumer = consumers[message.from]
        if (consumer != null) {
            consumer.update(message.index + 1, commitIndex)
            updateData(consumer)
        } else log.warn { "$self message from unknown consumer $message" }
    }

    private fun updateStreaming(consumer: RaftConsumer) {
        if (consumer.streaming) {
            update(consumer)
        }
    }

    private fun updateData(consumer: RaftConsumer) {
        if (!consumer.streaming) {
            update(consumer)
        }
    }

    private fun update(consumer: RaftConsumer) {
        val payload = storage.read(consumer.index, 256)
        log.debug { "$self consumeData $consumer $payload" }
        transport.sender(consumer.node).respond(
            ConsumerDataMessage(
                from = self,
                firstIndex = consumer.index,
                data = payload
            )
        )
    }

    private fun error(node: RaftNode, error: ClientErrorType) {
        log.warn { "$self consumerError ($node) error=$error" }
        transport.sender(node).respond(
            ClientErrorMessage(node, error)
        )
    }
}
