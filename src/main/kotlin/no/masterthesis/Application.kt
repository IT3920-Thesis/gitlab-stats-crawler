package no.masterthesis

import io.micronaut.runtime.Micronaut.*

fun main(args: Array<String>) {
  build()
    .args(*args)
    .packages("no.masterthesis")
    .start()
}

