package no.masterthesis.factory

import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import io.micronaut.rabbitmq.connect.ChannelInitializer
import jakarta.inject.Singleton
import java.io.IOException
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory

const val RABBITMQ_CRAWL_PROJECT_ID = "crawlGitlabProject"
const val RABBITMQ_CRAWL_CONTRIBUTION_ID = "crawlChangeContribution"
const val RABBITMQ_FANOUT_PROJECT_CRAWLED = "gitlabProject"

@Singleton
internal class RabbitMQChannelPoolInitializer : ChannelInitializer() {
  private val log = LoggerFactory.getLogger(this::class.java)

  /**
   * Hooks onto RabbitMQ and creates the exchange and queues
   * we need to utilize the Fanout pattern.
   * */
  @Throws(IOException::class)
  override fun initialize(channel: Channel, name: String) {
    val queues = listOf(
      RABBITMQ_CRAWL_PROJECT_ID,
      RABBITMQ_CRAWL_CONTRIBUTION_ID,
    ).map {
      val queue = channel.queueDeclare(
        it,
        false,
        false,
        false,
        mapOf("x-max-priority" to 100),
      )
      log.info("Queue created", kv("queueName", queue.queue), kv("messageCount", queue.messageCount), kv("consumerCount", queue.consumerCount))
      it to queue
    }.associate { it }

    val gitProjectsFanout = channel.exchangeDeclare(RABBITMQ_FANOUT_PROJECT_CRAWLED, BuiltinExchangeType.FANOUT)
    log.info(
      "Fan-out exchange created for Gitlab Projects",
      kv("protocolMethodName", gitProjectsFanout.protocolMethodName()),
      kv("protocolClassId", gitProjectsFanout.protocolClassId()),
      kv("protocolMethodId", gitProjectsFanout.protocolMethodId()),
    )

    // Let every queue subscribe to the fanout exchange
    queues.forEach { (_, queue) ->
      channel.queueBind(queue.queue, RABBITMQ_FANOUT_PROJECT_CRAWLED, "", mapOf("x-match" to "all"))
    }
  }
}
