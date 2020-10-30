package ir.tapsell.sample.api.rest

import ir.tapsell.sample.domain.cassandra.AdEvent
import ir.tapsell.sample.util.BuildUtil
import ir.tapsell.sample.kafka.topic.KafkaTopicUtil
import ir.tapsell.sample.domain.kafka.ImpressionEvent
import ir.tapsell.sample.kafka.producer.KafkaProducer
import ir.tapsell.sample.kafka.producer.service.EventProducerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api")
class RestApi(
        private val kafkaProducer: KafkaProducer,
        private val eventProducerService: EventProducerService
) {
    @GetMapping("/sample/send/")
    @Throws(Exception::class)
    fun sendImpression(): ResponseEntity<AdEvent> {
        var adEvent = try {
            val impressionEvent = eventProducerService.createRandomImpressionEvent()
            kafkaProducer.sendImpressionEvent(KafkaTopicUtil.getImpressionTopic(), impressionEvent)
            val clickEvent = eventProducerService.createRandomClickEvent(impressionEvent.requestId)
            kafkaProducer.sendClickEvent(KafkaTopicUtil.getClickTopic(), clickEvent)
            BuildUtil.buildNewAdEvent(impressionEvent, clickEvent)
        } catch (e: Exception){
            null
        }
        val result = Optional.ofNullable(adEvent)
        return if (result.isPresent) {
            ResponseEntity.ok(result.get())
        } else {
            ResponseEntity.badRequest().body(null)
        }
    }
}