package ir.tapsell.sample.cassandra.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration
import org.springframework.data.cassandra.config.SchemaAction
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories


@Configuration
@EnableCassandraRepositories(basePackages = ["ir.tapsell.sample.domain.cassandra"])
class CassandraConfig : AbstractCassandraConfiguration() {
    @Value("\${cassandra.contactpoints}")
    private val contactPoints: String? = null

    @Value("\${cassandra.port}")
    private val port = 9042

    @Value("\${cassandra.keyspace}")
    private val keySpace: String? = null

    @Value("\${cassandra.datacenter}")
    private val datacenter: String? = null

    override fun getKeyspaceName(): String {
        return keySpace!!
    }

    override fun getContactPoints(): String {
        return contactPoints!!
    }

    override fun getPort(): Int {
        return port
    }

    override fun getSchemaAction(): SchemaAction {
        return SchemaAction.CREATE_IF_NOT_EXISTS
    }

    override fun getLocalDataCenter(): String? {
        return datacenter
    }

}