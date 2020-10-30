package ir.tapsell.sample.kafka.producer

import ir.tapsell.sample.domain.kafka.ClickEvent
import ir.tapsell.sample.domain.kafka.ImpressionEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaProducer(
        private val kafkaTemplateForImpression: KafkaTemplate<String, ImpressionEvent>,
        private val kafkaTemplateForClick: KafkaTemplate<String, ClickEvent>
) {
    fun sendImpressionEvent(topic: String, value: ImpressionEvent) {
        kafkaTemplateForImpression.send(topic, value.requestId, value)
    }

    fun sendClickEvent(topic: String, value: ClickEvent) {
        kafkaTemplateForClick.send(topic, value.requestId, value)
    }
}