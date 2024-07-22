package io.github.anki.anki.configuration

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableConfigurationProperties(MongoProperties::class)
@EnableMongoRepositories(
    mongoTemplateRef = "mongoTemplate",
    basePackages = ["io.github.anki.anki.repository.mongodb"],
)
class MongoConfiguration(
    private val props: MongoProperties,
): AbstractMongoClientConfiguration() {

    override fun getDatabaseName(): String = props.database
}
