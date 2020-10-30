package ir.tapsell.sample.kafka.producer.service

import ir.tapsell.sample.domain.kafka.ClickEvent
import ir.tapsell.sample.kafka.topic.KafkaTopicUtil
import ir.tapsell.sample.domain.kafka.ImpressionEvent
import ir.tapsell.sample.kafka.producer.KafkaProducer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*
import kotlin.experimental.and
import kotlin.math.absoluteValue

@Service
class EventProducerService(private val kafkaProducer: KafkaProducer) {
    @Value(value = "\${kafka.numberForGenerate}")
    private val numberOfRecordsToProduce = 10_000

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private var requestIdSequence: Long = 1

    fun startBulkSending(){
        var index = 0
        while (index++ < numberOfRecordsToProduce){
            val impressionEvent = createRandomImpressionEvent()
            kafkaProducer.sendImpressionEvent(KafkaTopicUtil.getImpressionTopic(), impressionEvent)
            val clickEvent = createRandomClickEvent(impressionEvent.requestId)
            kafkaProducer.sendClickEvent(KafkaTopicUtil.getClickTopic(), clickEvent)
        }
    }

    fun createRandomImpressionEvent(): ImpressionEvent {
        return ImpressionEvent(
                requestIdSequence++.toString(),
                (1 + Random().nextInt().absoluteValue).toString(),
                generateSampleTitle(16 + Random().nextInt(16)),
                (1 + Random().nextDouble().absoluteValue),
                (1 + Random().nextInt().absoluteValue).toString(),
                generateSampleTitle(32 + Random().nextInt(32)),
                (1 + Random().nextLong().absoluteValue)
        )
    }

    fun createRandomClickEvent(requestId: String): ClickEvent {
        return ClickEvent(requestId, (1 + Random().nextLong().absoluteValue))
    }

    private fun generateSampleTitle(length: Int): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return (bytes.indices)
                .map { i ->
                    charPool[(bytes[i] and 0xFF.toByte() and (charPool.size-1).toByte()).toInt()]
                }.joinToString("")
    }
}