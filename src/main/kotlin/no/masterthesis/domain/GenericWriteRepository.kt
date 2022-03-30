package no.masterthesis.domain

interface GenericWriteRepository<T> {
  fun saveAll(items: List<T>)
}
