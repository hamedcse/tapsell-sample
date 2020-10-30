## Sample Tapsell Project:<br/>
Keywords: Kotlin, Maven, Spring, Boot, Test, Kafka, Cassandra
#### By: Hamed Ghezelbash [ [linkedin](https://www.linkedin.com/in/hamedcse/) , [email](mailto:gh.hamed.cs@gmail.com) ] 
#### Available as intellij project: [here](https://drive.google.com/file/d/1yfj41Mh_nBo9xG1BeF9XQ44lTohY9rL1/view?usp=sharing)
Project Info: 
- java <b>11</b>
- kotlin <b>1.3.72</b>
- Spring Boot <b>2.3.4.RELEASE</b>
- kafka <b>2.13-2.6.0</b>
- kafka-gui <b>kafka tool 2.0.8 [here](https://www.kafkatool.com)</b>
- cassandra <b>3.11.8</b>
- cassandra-gui <b>RazorSQL 9.2.2 [here](https://razorsql.com)</b>
- OS <b>windows 10</b>
- IDE <b>intellij</b>
<hr/>

### Sample Data Generator (time spent: 3 hours)
This module created for generating sample random data to test entire project!<br>
Method EventProducerService->startBulkSending() will generate ${kafka.numberForGenerate} records and will publish them into kafka queues!<br>
The startBulkSending method will be called after ApplicationReadyEvent of spring:<br>
```java
SpringConfiguration->initAfterStartup->EventProducerService->startBulkSending()
```
For test purposes there is rest api which will generate just one impression event and one click event and publish them to kafka:<br> 
```html
http://localhost:8080/api/sample/send
```
Note: Before startBulkSending method call in ApplicationReadyEvent, all data will be cleared from cassandra! 
```java
SpringConfiguration->initAfterStartup->AdEventCassandraService->clearData()
```
Info: ImpressionEvent and ClickEvent are in ir.tapsell.sample.domain.kafka package.<br>
EventProducerService is in ir.tapsell.sample.kafka.producer.service package<hr/>

### KafkaProducer and KafkaProducerConfig (time spent: 1 hours)
KafkaProducer is very straightforward and needs no explanation.<br>
KafkaProducerConfig will create all required beans for ImpressionEvent and ClickEvent queue.<br>
There is also KafkaTopicUtil to creating topics with some utility methods.<hr/>
### KafkaConsumer and KafkaConsumerConfig (time spent: 3 hours)
KafkaConsumerConfig will create all required beans for KafkaConsumer.<br>
KafkaConsumer contains three KafkaListener for three topics:
- impression-event
- click-event
- click-retry-event
<br>
The reason for creating `click-retry-event` topic explained at: KafkaConsumer->processClickEvent (JavaDoc)<br><br>
KafkaConsumer->processImpressionEvent: this method will pass a received impression event to adEventCassandraService .<br>
KafkaConsumer->processClickMessage+processClickRetryMessage: this two method will pass a received click event to processClickEvent method.<br>
KafkaConsumer->processClickEvent: This method will pass a received click event to adEventCassandraService, but will check whether click event processed successfully or not.<br/>

### retry mechanism
If processing was not successful (due to the slowness of persisting impression events thread compared to updating click events thread)
, then click event will be sent to `click-retry-event` for retry mechanism and preventing data loss.<hr/>

### CassandraConfig, AdEventCassandraRepository and AdEventCassandraService (time spent: 5 hours)
CassandraConfig is straightforward config and needs no explanation.<br>
AdEventCassandraRepository using CassandraRepository (which follows the Spring Data repository abstraction) and contains three method:
```java
fun findByRequestId(@Param("requestId") requestId: String): Optional<AdEvent>

@Query("update ad_events set click_time=?0 where request_id=?1")
fun updateClickTimeByRequestId(clickTime: Long, requestId: String)

@Query("truncate ads.ad_events")
fun clearData()
```
These methods are simple and just second one needs a little explain:<br>
However it is possible to use `save` method of adEventCassandraRepository to update an adEvent,
for experimenting effect of explicit query execution and comparing these two update mechanism,
`updateClickTimeByRequestId` created with related query!<br>
AdEventCassandraService contains multiple method, where just two of them require explanation:<br>
- processImpressionEvent
This method is just to create a new adEvent object from provided impressionEvent and then persist it at cassandra!
- processClickEvent
This method is more interesting:
1. First of all it will try to find related adEvent in cassandra by requestId.
2. If related adEvent is present, then based on ${cassandra.service.update.method} (which can be default or custom),
clickTime of the adEvent will be updated at cassandra (throw `save` or `updateClickTimeByRequestId` method of `cassandraRepository`).
So result of processClickEvent will be true and means success in processing clickEvent.
3. If related adEvent is absent, it means clickEvent is processed before processing related impressionEvent (which is possible).
4. To preventing data loss, as mentioned earlier, retry mechanism introduced. 
So result of processClickEvent will be false and means failure in processing clickEvent.<hr/>
#### experimenting effect of explicit query execution VS cassandraRepository save method for updating: (time spent: 2 hours)
````
For 10,000 impression event and 10,000 click event:
-------------------------------------------------------------------------------------------
#cassandraRepository save method for updating (default ${cassandra.service.update.method})
1. Finished with 16272 ms
2. Finished with 16309 ms
3. Finished with 16263 ms
4. Finished with 18605 ms
5. Finished with 16277 ms
6. Finished with 15676 ms
7. Finished with 16072 ms
8. Finished with 16894 ms
-------------------------------------------------------------------------------------------
#explicit query execution for updating (custom ${cassandra.service.update.method})
1. Finished with 14048 ms
2. Finished with 14378 ms
3. Finished with 13625 ms
4. Finished with 12565 ms
5. Finished with 13298 ms
6. Finished with 15481 ms
7. Finished with 13373 ms
8. Finished with 14045 ms
````
This kind of experimentation on local desktop machine, because of:
- without tuning kafka and cassandra
- limited resource
- windows as OS
- all components within one monolithic app
- producer and consumer within same app!!!
will be not considerable (it is still experiment though `:)` ).<br>
### About val and var in AdEvent
It is possible to make clickTime val instead of var in AdEvent class, but by converting clickTime to val,
it is required to create a new object just for setting clickTime for an AdEvent that returned from cassandra!<br/>
So just using var instead of val to prevent that overhead
(i.e one extra object creation) and keep code clean.<br/>
#### Configs
All configurations are available in application.properties file.<br>
Two property needs a little explanation:
- ${app.mode.is-test:false}: this property used in tricky approach to prevent execution of initAfterStartup method content in:
````kotlin
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
````
${app.mode.is-test} will set to `true` on all `Test classes`.<br>
- ${kafka.numberForGenerate:10000}: this property will be number of events to be generated at ApplicationReadyEvent.<br>
For cassandra server, there is cassandra-script.cql file to create `keyspace` and `table` (manually).<hr/>

### AllKafkaTestsRunner, KafkaProducerTest, KafkaConsumerTest and AdEventCassandraServiceTest (time spent: 17 hours)
It is better to explain all these test classes in abstract manner rather than specific manner (due to the complexity).
So first of all, it is important to say that for [AllKafkaTestsRunner, KafkaProducerTest, KafkaConsumerTest] embedded kafka used for testing.<br>
For testing KafkaProducerTest and KafkaConsumerTest, AllKafkaTestsRunner is provided. AllKafkaTestsRunner will create appropriate embedded kafka for testing both of them.<br>
````html
Directly testing of KafkaProducerTest and KafkaConsumerTest (bypassing AllKafkaTestsRunner) 
may lead to failure in tests.
````
- **KafkaProducerTest**: this test class will test **KafkaProducer** class by generating two ImpressionEvent and publish
them into _embedded kafka_ and then checking that received events from _embedded kafka_ are equal to published events or not.
Note: A simple consumer is provided and **KafkaConsumer** is not involved in this test.<br>

- **KafkaConsumerTest**: this test class will test **KafkaConsumer** class by generating x (random number between 1 and 10) number of ImpressionEvents and publish
them into _embedded kafka_ and then checking that number of received events from _embedded kafka_ are equal to x or not.
<br>.To preventing data manipulation in _cassandra_, cassandraRepository.save will be mocked.<br>
Note: A simple producer is provided and **KafkaProducer** is not involved in this test.<br>
Note: Cassandra should be available before running tests, if not, tests will be failed.<br><br>
- **AdEventCassandraServiceTest**: this test class will test **AdEventCassandraService** class by generating one
ImpressionEvent and pass it to adEventCassandraService->processImpressionEvent method.
<br>.To preventing kafka involvement, KafkaProducer.sendImpressionEvent will be mocked.<br>
After persisting generated ImpressionEvent in cassandra throw adEventCassandraService, then this method will try to
find AdEvent by requestId of generated ImpressionEvent and checking that appId of loaded AdEvent is equal to appId
of generated ImpressionEvent or not.
````html
Note: All test class are for impression event type, however, tests for click event type 
will be so likely and skipped because of time limit. 
````
````html
Test Coverage:
Class 83% (15/18)
Method 68% (53/77)
Line 62% (113/182)
````
### Preparing report (time spent: 3 hours)
Mentionable Notes:
- Kotlin headaches throw mapping from java to kotlin (especially null checking)
- Kafka embedded test setup: real nightmare
- Experimenting cassandra Leveled compaction performance impact seems interesting by considering heavy update behaviour.
- Providing Docker Compose for easy setup
````html
“You'll never reach perfection because there's always room for improvement. 
Yet get along the way to perfection, you'll learn to get better.”
````
# Total time: 34 hours
