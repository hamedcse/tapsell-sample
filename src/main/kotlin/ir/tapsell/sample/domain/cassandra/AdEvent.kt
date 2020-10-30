package ir.tapsell.sample.domain.cassandra


import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.CassandraType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table

@Table("ad_events")
class AdEvent(
        @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "request_id")
        @CassandraType(type = CassandraType.Name.VARCHAR)
        val requestId: String, // a unique id for the request

        @CassandraType(type = CassandraType.Name.VARCHAR)
        @Column("ad_id")
        val adId: String,

        @CassandraType(type = CassandraType.Name.VARCHAR)
        @Column("ad_title")
        val adTitle: String,

        @CassandraType(type = CassandraType.Name.DOUBLE)
        @Column("advertiser_cost")
        val advertiserCost: Double,

        @CassandraType(type = CassandraType.Name.VARCHAR)
        @Column("app_id")
        val appId: String,

        @CassandraType(type = CassandraType.Name.VARCHAR)
        @Column("app_title")
        val appTitle: String,

        @CassandraType(type = CassandraType.Name.BIGINT)
        @Column("impression_time")
        val impressionTime: Long,

        @CassandraType(type = CassandraType.Name.BIGINT)
        @Column("click_time")
        var clickTime: Long // default value will be -1 and means not available clickTime
        /**
         * Note: It is possible to make clickTime val instead of var, but by converting clickTime to val,
                it is required to create a new object just for setting clickTime
                for an AdEvent that returned from cassandra!<br/>
         * So just using var instead of val to prevent that overhead
               (i.e one extra object creation) and keep code clean.<br/>
        **/
)