package ir.tapsell.sample.kafka.consumer

import ir.tapsell.sample.cassandra.repository.AdEventCassandraRepository
import ir.tapsell.sample.kafka.AllKafkaTestsRunner
import ir.tapsell.sample.domain.kafka.ImpressionEvent
import ir.tapsell.sample.kafka.producer.service.EventProducerService
import ir.tapsell.sample.kafka.topic.KafkaTopicUtil
import ir.tapsell.sample.util.BuildUtil
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.junit.*
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import kotlin.random.Random

/**
 * Warning: Running testConsumer() as a test will fail because of embedded kafka problem.
 *  For successful test, AllKafkaTestsRunner is provided!
 * */
@RunWith(SpringRunner::class)
@SpringBootTest
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaConsumerTest {
    companion object {
        @BeforeClass @JvmStatic @Throws(Exception::class)
        fun setUpBeforeClass() {
            System.setProperty("app.mode.is-test", true.toString())
        }
    }
    private val logger = LoggerFactory.getLogger(javaClass)

    /** Mocking AdEventCassandraRepository to prevent data manipulation in test mode!*/
    @MockBean
    private lateinit var cassandraRepository: AdEventCassandraRepository

    @Autowired
    private val kafkaConsumer: KafkaConsumer? = null

    @Autowired
    private val eventProducerService: EventProducerService? = null

    private var template: KafkaTemplate<String, ImpressionEvent>? = null

    /** The kafkaListenerEndpointRegistry will be ok and IDE may show false message like:
         'Could not autowire. No beans of KafkaListenerEndpointRegistry type found'.
         Just Ignore message!**/
    @Autowired
    private val kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        // set up the Kafka producer properties
        val senderProperties = KafkaTestUtils.producerProps(
                AllKafkaTestsRunner.embeddedKafka.embeddedKafka.brokersAsString)
        // create a Kafka producer factory
        val producerFactory: ProducerFactory<String, ImpressionEvent> = DefaultKafkaProducerFactory<String, ImpressionEvent>(
                senderProperties,  StringSerializer(), JsonSerializer<ImpressionEvent>())
        // create a Kafka template
        template = KafkaTemplate<String, ImpressionEvent>(producerFactory)
        // set the default topic to send to
        template!!.defaultTopic = KafkaTopicUtil.getImpressionTopic()
        // wait until the partitions are assigned
        for (messageListenerContainer in kafkaListenerEndpointRegistry!!.listenerContainers) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer,
                    AllKafkaTestsRunner.embeddedKafka.embeddedKafka.partitionsPerTopic)
        }
    }

    @After
    fun tearDown() {
    }

    @Test
    @Throws(InterruptedException::class)
    fun testConsumer() {
        val randomNumberOfGenerateEvent = 1 + Random.nextLong(9)
        // WHEN
        for (x in 1..randomNumberOfGenerateEvent){
            val impressionEvent = eventProducerService!!.createRandomImpressionEvent()
            template!!.sendDefault(impressionEvent)
            //Mocking save method of cassandraRepository:
            val adEvent = BuildUtil.buildNewAdEvent(impressionEvent)
            Mockito.`when`(cassandraRepository.save(adEvent)).thenReturn(adEvent);
        }
        // THEN
        // we must have randomNumberOfGenerateEvent entity processed
        // We cannot predict when the process will occur. So we wait until the value is present. Thank to Awaitility.
        Awaitility.await().atMost(Durations.TEN_SECONDS).untilAsserted {
            val numberOfConsumedMessages = kafkaConsumer!!.numberOfProcessedImpressionEvent
            logger.debug("numberOfProcessedImpressionEvent: $numberOfConsumedMessages")
            Assert.assertEquals(randomNumberOfGenerateEvent, numberOfConsumedMessages)
        }
    }
}