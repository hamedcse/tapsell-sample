package ir.tapsell.sample.kafka.consumer

import ir.tapsell.sample.domain.kafka.ClickEvent
import ir.tapsell.sample.domain.kafka.ImpressionEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import java.util.*


@Configuration
class KafkaConsumerConfig {
    @Value(value = "\${kafka.bootstrap-servers}")
    private val bootstrapAddress: String? = null
    @Value("\${spring.kafka.consumer.group-id.impression-event}")
    private val groupIdImpressionEvent: String? = null
    @Value("\${spring.kafka.consumer.group-id.click-event}")
    private val groupIdClickEvent: String? = null
    @Value("\${spring.kafka.consumer.group-id.click-retry-event}")
    private val groupIdClickRetryEvent: String? = null

    fun impressionEventConsumerFactory(): ConsumerFactory<String, ImpressionEvent> {
        val props: MutableMap<String, Any?> = HashMap()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapAddress
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupIdImpressionEvent
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), JsonDeserializer(ImpressionEvent::class.java))
    }

    @Bean
    fun impressionEventKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, ImpressionEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, ImpressionEvent>()
        factory.consumerFactory = impressionEventConsumerFactory()
        return factory
    }

    fun clickEventConsumerFactory(): ConsumerFactory<String, ClickEvent> {
        val props: MutableMap<String, Any?> = HashMap()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapAddress
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupIdClickEvent
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), JsonDeserializer(ClickEvent::class.java))
    }

    @Bean
    fun clickEventKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, ClickEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, ClickEvent>()
        factory.consumerFactory = clickEventConsumerFactory()
        return factory
    }

    fun clickRetryEventConsumerFactory(): ConsumerFactory<String, ClickEvent> {
        val props: MutableMap<String, Any?> = HashMap()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapAddress
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupIdClickRetryEvent
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), JsonDeserializer(ClickEvent::class.java))
    }

    @Bean
    fun clickRetryEventKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, ClickEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, ClickEvent>()
        factory.consumerFactory = clickRetryEventConsumerFactory()
        return factory
    }
}