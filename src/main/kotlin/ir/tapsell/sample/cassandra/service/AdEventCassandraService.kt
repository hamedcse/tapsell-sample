package ir.tapsell.sample.cassandra.service

import ir.tapsell.sample.cassandra.repository.AdEventCassandraRepository
import ir.tapsell.sample.domain.cassandra.AdEvent
import ir.tapsell.sample.util.BuildUtil
import ir.tapsell.sample.domain.kafka.ClickEvent
import ir.tapsell.sample.domain.kafka.ImpressionEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class AdEventCassandraService(private val cassandraRepository: AdEventCassandraRepository) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${cassandra.service.update.method}")
    private val updateMethod: String = "default" //Possible values: default or custom

    fun processImpressionEvent(event: ImpressionEvent){
        val adEvent = BuildUtil.buildNewAdEvent(event)
        cassandraRepository.save(adEvent)
        //logger.info("ImpressionEvent persisted with request-id: ${consumerRecord.value().requestId}")
    }

    fun processClickEvent(consumerRecord: ConsumerRecord<String, ClickEvent>): Boolean{
        var isSuccessFull: Boolean
        val event = cassandraRepository.findByRequestId(consumerRecord.value().requestId)
        isSuccessFull = if(event.isPresent){
            if(updateMethod == "default") {
                val adEvent = event.get()
                adEvent.clickTime = consumerRecord.value().clickTime
                cassandraRepository.save(adEvent)
            } else { // updateMethod == "custom"
                cassandraRepository.updateClickTimeByRequestId(consumerRecord.value().clickTime, consumerRecord.value().requestId)
            }
            //logger.info("ClickEvent [$updateMethod] persisted with request-id: ${consumerRecord.value().requestId}")
            true
        } else {
            //logger.warn("Failed ClickEvent with request-id: ${consumerRecord.value().requestId}")
            false
        }
        return isSuccessFull
    }

    fun findByRequestId(requestId: String): Optional<AdEvent> {
        return cassandraRepository.findByRequestId(requestId)
    }

    fun findAll(): MutableList<AdEvent> {
        return cassandraRepository.findAll()
    }

    fun clearData(){
        cassandraRepository.clearData()
    }
}