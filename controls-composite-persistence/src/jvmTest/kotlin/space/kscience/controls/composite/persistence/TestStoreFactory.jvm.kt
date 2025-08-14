package space.kscience.controls.composite.persistence

import space.kscience.dataforge.context.Global
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

@OptIn(ExperimentalPathApi::class)
actual suspend fun runTestWithStore(block: suspend (store: SnapshotStore) -> Unit) {
    val testDir = Files.createTempDirectory("controls-persistence-test-")
    try {
        val store = FileSnapshotStore(Global, testDir.toString())
        block(store)
    } finally {
        testDir.deleteRecursively()
    }
}