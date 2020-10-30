package ir.tapsell.sample.kafka.topic

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class KafkaTopicUtil {
    @Value("\${message.topic.impression.name}")
    private val impressionTopicName: String? = null

    @Value("\${message.topic.click.name}")
    private val clickTopicName: String? = null

    @Value("\${message.topic.click.retry.name}")
    private val clickRetryTopicName: String? = null

    @Bean
    fun impressionTopic(): NewTopic? {
        return NewTopic(impressionTopicName, 1, 1.toShort())
    }

    @Bean
    fun clickTopic(): NewTopic? {
        return NewTopic(clickTopicName, 1, 1.toShort())
    }

    @Bean
    fun clickRetryTopic(): NewTopic? {
        return NewTopic(clickRetryTopicName, 1, 1.toShort())
    }


    companion object {
        @Value("\${message.topic.impression.name}")
        private val impressionTopicName: String? = null

        @Value("\${message.topic.click.name}")
        private val clickTopicName: String? = null

        @Value("\${message.topic.click.retry.name}")
        private val clickRetryTopicName: String? = null

        fun getImpressionTopic(): String{
            return if (impressionTopicName.isNullOrBlank()) {
                "impression-event"
            } else {
                impressionTopicName
            }
        }

        fun getClickTopic(): String{
            return if (clickTopicName.isNullOrBlank()) {
                "click-event"
            } else {
                clickTopicName
            }
        }

        fun getClickRetryTopic(): String{
            return if (clickRetryTopicName.isNullOrBlank()) {
                "click-retry-event"
            } else {
                clickRetryTopicName
            }
        }
    }

}