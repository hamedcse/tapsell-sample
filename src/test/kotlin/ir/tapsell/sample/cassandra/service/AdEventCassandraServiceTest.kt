package ir.tapsell.sample.cassandra.service

import ir.tapsell.sample.domain.kafka.ImpressionEvent
import ir.tapsell.sample.kafka.producer.KafkaProducer
import ir.tapsell.sample.kafka.producer.service.EventProducerService
import org.assertj.core.api.Assertions
import org.junit.*
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdEventCassandraServiceTest {
    companion object {
        @BeforeClass @JvmStatic @Throws(Exception::class)
        fun setUpBeforeClass() {
            System.setProperty("app.mode.is-test", true.toString())
        }
    }
    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private val adEventCassandraService: AdEventCassandraService? = null

    @Autowired
    private val eventProducerService: EventProducerService? = null

    @Test
    fun whenProcessSampleImpressionEvent_thenLoadFromDB() {
        val impressionEvent = eventProducerService!!.createRandomImpressionEvent()
        val mock: KafkaProducer = Mockito.mock(KafkaProducer::class.java)
        Mockito.doNothing().`when`(mock).
            sendImpressionEvent(ArgumentMatchers.anyString(), any(ImpressionEvent::class.java))
        adEventCassandraService!!.processImpressionEvent(impressionEvent)
        var eventFromDB = adEventCassandraService.findByRequestId(impressionEvent.requestId)
        Assertions.assertThat(eventFromDB.isPresent).isEqualTo(true)
        Assertions.assertThat(impressionEvent.appId).isEqualTo(eventFromDB.get().appId)
    }

    private fun <T> any(type: Class<T>): T {
        Mockito.any(type)
        return uninitialized()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> uninitialized(): T = null as T
}