package ir.tapsell.sample.util

import ir.tapsell.sample.cassandra.service.AdEventCassandraService
import ir.tapsell.sample.kafka.producer.service.EventProducerService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener


@Configuration
class SpringConfiguration{
    @Value(value = "\${app.mode.is-test}")
    private val isTestMode: Boolean = true

    @EventListener(ApplicationReadyEvent::class)
    fun initAfterStartup(event: ApplicationReadyEvent) {
        if(!isTestMode){
            event.applicationContext.getBean(AdEventCassandraService::class.java).clearData()
            event.applicationContext.getBean(EventProducerService::class.java).startBulkSending()
        }
    }
}