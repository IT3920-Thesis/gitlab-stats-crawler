package utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

/**
 * Utility function to handle coroutines in tests.
 * It is simply a wrapper around [kotlinx.coroutines.runBlocking]
 * that always returns [Unit] (expected by Junit).
 * */
fun runBlockingTest(
  block: suspend CoroutineScope.() -> Any
): Unit = runBlocking {
  block()
  Unit
}
