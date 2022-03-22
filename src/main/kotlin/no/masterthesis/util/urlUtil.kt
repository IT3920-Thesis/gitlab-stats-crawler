package no.masterthesis.util

fun String.base64UrlEncode() = this.replace("/", "%2F")
