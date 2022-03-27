package no.masterthesis.util

private typealias ApiPaginationCall<T> = suspend (page: Int) -> List<T>

/**
 * A very simple utility function to simplify API calls.
 * It continues to paginate until [callback] returns an empty list
 *
 * @return All items returned as a single list
 * */
suspend fun <T> paginateApiCall(callback: ApiPaginationCall<T>): List<T> {
  val allItems = ArrayList<T>()
  var currentPage = 1

  while (true) {
    val result = callback(currentPage)

    if (result.isEmpty()) {
      break
    }

    allItems.addAll(result)
    currentPage += 1
  }

  return allItems
}
