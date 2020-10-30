package ir.tapsell.sample.cassandra.repository

import ir.tapsell.sample.domain.cassandra.AdEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AdEventCassandraRepository : CassandraRepository<AdEvent, String> {
    fun findByRequestId(@Param("requestId") requestId: String): Optional<AdEvent>

    @Query("update ad_events set click_time=?0 where request_id=?1")
    fun updateClickTimeByRequestId(clickTime: Long, requestId: String)

    @Query("truncate ads.ad_events")
    fun clearData()
}