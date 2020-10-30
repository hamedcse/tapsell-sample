package ir.tapsell.sample.kafka.producer

import ir.tapsell.sample.domain.kafka.ClickEvent
import ir.tapsell.sample.domain.kafka.ImpressionEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.*

@Configuration
class KafkaProducerConfig {
    @Value(value = "\${kafka.bootstrap-servers}")
    private val bootstrapAddress: String? = null

    @Bean
    fun impressionEventProducerFactory(): ProducerFactory<String, ImpressionEvent> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapAddress!!
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return DefaultKafkaProducerFactory<String, ImpressionEvent>(configProps)
    }

    @Bean
    fun impressionEventKafkaTemplate(): KafkaTemplate<String, ImpressionEvent> {
        return KafkaTemplate(impressionEventProducerFactory())
    }

    @Bean
    fun clickEventProducerFactory(): ProducerFactory<String, ClickEvent> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapAddress!!
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return DefaultKafkaProducerFactory<String, ClickEvent>(configProps)
    }

    @Bean
    fun clickEventKafkaTemplate(): KafkaTemplate<String, ClickEvent> {
        return KafkaTemplate(clickEventProducerFactory())
    }
}