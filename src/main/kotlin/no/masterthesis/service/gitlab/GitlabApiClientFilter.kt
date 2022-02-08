package no.masterthesis.service.gitlab

import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import net.logstash.logback.argument.StructuredArguments.kv
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory

@Filter("/api/v4/**")
internal class GitlabApiClientFilter(
  @param:Value("\${gitlabapi.accessToken}") private val accessToken: String,
) : HttpClientFilter {
  private val log = LoggerFactory.getLogger(this::class.java)
  
  override fun doFilter(request: MutableHttpRequest<*>, chain: ClientFilterChain): Publisher<out HttpResponse<*>> {
    log.trace("Injecting Access token to request", kv("accessToken", accessToken))

    return chain.proceed(request.header("PRIVATE-TOKEN", accessToken))
  }
}
