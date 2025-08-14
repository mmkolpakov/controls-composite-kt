package space.kscience.controls.composite.persistence

import kotlinx.browser.localStorage

actual suspend fun runTestWithStore(block: suspend (store: SnapshotStore) -> Unit) {
    val testPrefix = "test.controls.snapshot."
    val store = LocalStorageSnapshotStore(testPrefix)

    fun clearStorage() {
        val keysToRemove = mutableListOf<String>()
        for (i in 0 until localStorage.length) {
            val key = localStorage.key(i)
            if (key?.startsWith(testPrefix) == true) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { localStorage.removeItem(it) }
    }

    try {
        clearStorage() // Очистка перед тестом
        block(store)
    } finally {
        clearStorage() // Очистка после теста
    }
}