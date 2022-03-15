package no.masterthesis

import io.micronaut.runtime.Micronaut.build

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
  build()
    .args(*args)
    .packages("no.masterthesis")
    .start()
}

