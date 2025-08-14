package space.kscience.controls.composite.persistence

import kotlinx.coroutines.test.runTest
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import kotlin.test.*

/**
 * A universal test suite for SnapshotStore implementations.
 * It uses the `runTestWithStore` factory to get a platform-specific store instance.
 */
class SnapshotStoreTest {

    private val testName = Name.parse("device.test")
    private val testSnapshot = Meta {
        "value" put "testValue"
        "number" put 42
    }

    @Test
    fun saveAndLoad() = runTest {
        runTestWithStore { store ->
            assertNull(store.load(testName), "Store should be empty initially for name $testName")

            store.save(testName, testSnapshot)
            val loaded = store.load(testName)

            assertNotNull(loaded, "Snapshot should be loaded after saving")
            assertEquals(testSnapshot, loaded)
            assertEquals("testValue", loaded["value"].string)
            assertEquals(42, loaded["number"].int)
        }
    }

    @Test
    fun overwrite() = runTest {
        runTestWithStore { store ->
            store.save(testName, testSnapshot)

            val newSnapshot = Meta {
                "value" put "newValue"
                "number" put 99
            }

            store.save(testName, newSnapshot)
            val loaded = store.load(testName)

            assertNotNull(loaded)
            assertEquals(newSnapshot, loaded)
            assertEquals("newValue", loaded["value"].string)
        }
    }

    @Test
    fun delete() = runTest {
        runTestWithStore { store ->
            store.save(testName, testSnapshot)
            assertNotNull(store.load(testName), "Snapshot should exist before deletion")

            store.delete(testName)
            assertNull(store.load(testName), "Snapshot should be null after deletion")
        }
    }
}