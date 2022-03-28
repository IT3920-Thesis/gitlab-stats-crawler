package no.masterthesis.configuration

import io.micronaut.test.support.TestPropertyProvider
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class TestContainersWrapper : TestPropertyProvider {
  override fun getProperties(): MutableMap<String, String> {
    val container = LocalPostgreSQLContainer.container
    val rabbitMq = LocalRabbitMQWrapper.container

    println("Connecting to rabbit-mq host: ${rabbitMq.containerIpAddress}")

    return mapOf(
      "datasources.default.url" to "${container.jdbcUrl}?stringtype=unspecified",
      "datasources.default.driverClassName" to container.driverClassName,
      "datasources.default.username" to container.username,
      "datasources.default.password" to container.password,

      "rabbitmq.host" to "${rabbitMq.host}:${rabbitMq.httpPort}",
      "rabbitmq.username" to rabbitMq.adminUsername,
      "rabbitmq.password" to rabbitMq.adminPassword,
    )
      .toMutableMap()
  }
}
