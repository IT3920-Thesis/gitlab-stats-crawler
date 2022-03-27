package no.masterthesis.util

fun String.encodeUriComponent() = this.replace("/", "%2F")
