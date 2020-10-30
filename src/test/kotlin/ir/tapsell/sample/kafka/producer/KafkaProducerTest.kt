package ir.tapsell.sample.kafka.producer

import ir.tapsell.sample.kafka.AllKafkaTestsRunner
import ir.tapsell.sample.domain.kafka.ImpressionEvent
import ir.tapsell.sample.kafka.producer.service.EventProducerService
import ir.tapsell.sample.kafka.topic.KafkaTopicUtil
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.*
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Warning: Running testProducer() as a test will fail because of embedded kafka problem.
 *  For successful test, AllKafkaTestsRunner is provided!
 * */
@RunWith(SpringRunner::class)
@SpringBootTest
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaProducerTest{
    companion object {
        @BeforeClass @JvmStatic @Throws(Exception::class)
        fun setUpBeforeClass() {
            System.setProperty("app.mode.is-test", true.toString())
        }
    }
    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private val kafkaProducer: KafkaProducer? = null

    @Autowired
    private val eventProducerService: EventProducerService? = null

    private var container: KafkaMessageListenerContainer<String, ImpressionEvent>? = null

    private var consumerRecords: LinkedBlockingQueue<ConsumerRecord<String, ImpressionEvent>>? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        consumerRecords = LinkedBlockingQueue()

        val containerProperties = ContainerProperties(KafkaTopicUtil.getImpressionTopic())

        val consumerProperties = KafkaTestUtils.consumerProps(
                "impression-group", "false", AllKafkaTestsRunner.embeddedKafka.embeddedKafka)
        consumerProperties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        val jsonDeserializer = JsonDeserializer<ImpressionEvent>()
        jsonDeserializer.addTrustedPackages("*")

        val consumer: DefaultKafkaConsumerFactory<String, ImpressionEvent> = DefaultKafkaConsumerFactory(
                consumerProperties, StringDeserializer(), jsonDeserializer)

        container = KafkaMessageListenerContainer(consumer, containerProperties)
        container!!.setupMessageListener(MessageListener(fun(record: ConsumerRecord<String, ImpressionEvent>){
            consumerRecords!!.add(record)
        }))
        container!!.start()

        ContainerTestUtils.waitForAssignment(container, AllKafkaTestsRunner.embeddedKafka.embeddedKafka.partitionsPerTopic)
    }

    @After
    fun tearDown() {
        container!!.stop()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testProducer() {
        // send the message
        val topic = KafkaTopicUtil.getImpressionTopic()
        val impressionEvent = eventProducerService!!.createRandomImpressionEvent()
        kafkaProducer!!.sendImpressionEvent(topic, impressionEvent)
        val impressionEvent2 = eventProducerService.createRandomImpressionEvent()
        kafkaProducer.sendImpressionEvent(topic, impressionEvent2)

        val received: ConsumerRecord<String, ImpressionEvent>? = consumerRecords!!.poll(2, TimeUnit.SECONDS)
        val received2: ConsumerRecord<String, ImpressionEvent>? = consumerRecords!!.poll(2, TimeUnit.SECONDS)

        //logger.debug("impressionEvent.requestId: " + impressionEvent.requestId)
        //logger.debug("received.requestId: " + received!!.value().requestId)
        Assert.assertEquals(received!!.value().requestId, impressionEvent.requestId)
        //logger.debug("impressionEvent2.requestId: " + impressionEvent2.requestId)
        //logger.debug("received2.requestId: " + received2!!.value().requestId)
        Assert.assertEquals(received2!!.value().requestId, impressionEvent2.requestId)
    }
}