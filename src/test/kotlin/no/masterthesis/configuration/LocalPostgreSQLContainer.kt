package no.masterthesis.configuration

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

object LocalPostgreSQLContainer {
  class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>(DockerImageName.parse("postgres:14"))

  const val DATABASE_NAME = "costmanager"
  const val DATABASE_USER = "testuser"
  const val DATABASE_PASSWORD = "testpassword"

  val container = KPostgreSQLContainer().apply {
    withDatabaseName(DATABASE_NAME)
    withUsername(DATABASE_USER)
    withPassword(DATABASE_PASSWORD)
  }

  init {
    container.start()
  }
}

