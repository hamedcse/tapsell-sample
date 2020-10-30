package ir.tapsell.sample.kafka.consumer

import ir.tapsell.sample.cassandra.service.AdEventCassandraService
import ir.tapsell.sample.domain.kafka.ClickEvent
import ir.tapsell.sample.domain.kafka.ImpressionEvent
import ir.tapsell.sample.kafka.producer.KafkaProducer
import ir.tapsell.sample.kafka.topic.KafkaTopicUtil
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaConsumer(
        private val adEventCassandraService: AdEventCassandraService,
        private val kafkaProducer: KafkaProducer
){
    var numberOfProcessedImpressionEvent: Long = 0 // useful for testing

    @KafkaListener(topics = ["impression-event"], clientIdPrefix = "impressionEvent"
            , containerFactory = "impressionEventKafkaListenerContainerFactory")
    fun processImpressionMessagePartitionZero(consumerRecord: ConsumerRecord<String, ImpressionEvent>) {
        processImpressionEvent(consumerRecord.value())
    }

    private fun processImpressionEvent(event: ImpressionEvent) {
        adEventCassandraService.processImpressionEvent(event)
        numberOfProcessedImpressionEvent++
    }

    @KafkaListener(topics = ["click-event"], clientIdPrefix = "clickEvent"
            , containerFactory = "clickEventKafkaListenerContainerFactory")
    fun processClickMessage(consumerRecord: ConsumerRecord<String, ClickEvent>) {
        processClickEvent(consumerRecord)
    }

    @KafkaListener(topics = ["click-retry-event"], clientIdPrefix = "clickRetryEvent"
            , containerFactory = "clickRetryEventKafkaListenerContainerFactory")
    fun processClickRetryMessage(consumerRecord: ConsumerRecord<String, ClickEvent>) {
        processClickEvent(consumerRecord)
    }

    /**
     * If processing of click event is not successful (i.e related impression event not processed yet
     *  and therefor not found in the cassandra), then publish click event to "click-retry-event" topic
     *  for retry mechanism.<br/>
     * Tip: Click event will be re-published over and over to "click-retry-event" topic until the related
        impression event becomes available at the cassandra database.<br/>
     * Retry mechanism will help to recover from a situation where click event is starting to process
        where its related impression event is not being processed yet.<br/>
     * This mechanism is simple and will solve the problem,
        however, in real project it should handle much better!<br/>
    **/
    private fun processClickEvent(consumerRecord: ConsumerRecord<String, ClickEvent>) {
        val isSuccessFull = adEventCassandraService.processClickEvent(consumerRecord)
        if (!isSuccessFull) {
            kafkaProducer.sendClickEvent(KafkaTopicUtil.getClickRetryTopic(), consumerRecord.value())
        }
    }
}