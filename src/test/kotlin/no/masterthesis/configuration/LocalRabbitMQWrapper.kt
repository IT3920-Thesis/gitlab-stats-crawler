package no.masterthesis.configuration

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName

object LocalRabbitMQWrapper {
  class Container : RabbitMQContainer(DockerImageName.parse("rabbitmq:3"))

  const val ADMIN_USERNAME = "guest"
  const val ADMIN_PASSWORD = "testpassword"

  val container = Container().apply {
    withAdminPassword(ADMIN_PASSWORD)
  }

  init {
    container.start()
  }
}

