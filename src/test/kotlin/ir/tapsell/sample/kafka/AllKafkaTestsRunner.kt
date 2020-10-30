package ir.tapsell.sample.kafka

import ir.tapsell.sample.kafka.consumer.KafkaConsumerTest
import ir.tapsell.sample.kafka.producer.KafkaProducerTest
import ir.tapsell.sample.kafka.topic.KafkaTopicUtil
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.rule.EmbeddedKafkaRule

/**
 * For successful test of kafka producer and consumer (with embedded kafka), This class is provided!
 * AllKafkaTestsRunner will run tests for KafkaProducerTest and KafkaConsumerTest.
 * Note: Cassandra should be available before running tests, if not, tests will be failed.
 * */
@RunWith(Suite::class)
@SuiteClasses(KafkaProducerTest::class, KafkaConsumerTest::class)
@SpringBootTest
class AllKafkaTestsRunner {
    companion object {
        @JvmStatic
        @get:ClassRule
        val embeddedKafka = EmbeddedKafkaRule(1, true, 1, KafkaTopicUtil.getImpressionTopic())

        @BeforeClass
        @JvmStatic
        @Throws(Exception::class)
        fun setUpBeforeClass() {
            try {
                val kafkaBootstrapServers: String = embeddedKafka.embeddedKafka.brokersAsString
                // override the property in application.properties
                System.setProperty("kafka.bootstrap-servers", kafkaBootstrapServers)
            } finally {
                System.setProperty("app.mode.is-test", true.toString())
            }
        }
    }
}