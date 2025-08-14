package space.kscience.controls.composite.persistence

import kotlinx.coroutines.test.runTest
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import kotlin.test.*

class InMemorySnapshotStoreTest {

    private val testName = Name.parse("device.test")
    private val testSnapshot = Meta {
        "value" put "testValue"
        "number" put 42
    }

    @Test
    fun testSaveAndLoad() = runTest {
        val store = InMemorySnapshotStore()

        // 1. Проверяем, что изначально хранилище пусто
        assertNull(store.load(testName), "Store should be empty initially")

        // 2. Сохраняем и проверяем, что данные загружаются корректно
        store.save(testName, testSnapshot)
        val loaded = store.load(testName)
        assertNotNull(loaded, "Snapshot should be loaded after saving")
        assertEquals(testSnapshot, loaded)
        assertEquals("testValue", loaded["value"].string)
        assertEquals(42, loaded["number"].int)
    }

    @Test
    fun testOverwrite() = runTest {
        val store = InMemorySnapshotStore()
        store.save(testName, testSnapshot)

        val newSnapshot = Meta {
            "value" put "newValue"
            "number" put 99
        }

        // 3. Перезаписываем и проверяем
        store.save(testName, newSnapshot)
        val loaded = store.load(testName)
        assertNotNull(loaded)
        assertEquals(newSnapshot, loaded)
        assertEquals("newValue", loaded["value"].string)
        assertNotEquals(testSnapshot, loaded)
    }

    @Test
    fun testDelete() = runTest {
        val store = InMemorySnapshotStore()
        store.save(testName, testSnapshot)
        assertNotNull(store.load(testName), "Snapshot should exist before deletion")

        // 4. Удаляем и проверяем, что данных больше нет
        store.delete(testName)
        assertNull(store.load(testName), "Snapshot should be null after deletion")

        // 5. Повторное удаление не должно вызывать ошибок
        store.delete(testName)
    }
}