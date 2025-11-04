package space.kscience.controls.composite.persistence

import kotlin.test.BeforeTest

class InMemorySnapshotStoreTest : SnapshotStoreTestBase() {
    override lateinit var store: SnapshotStore

    @BeforeTest
    fun setup() {
        store = InMemorySnapshotStore()
    }
}