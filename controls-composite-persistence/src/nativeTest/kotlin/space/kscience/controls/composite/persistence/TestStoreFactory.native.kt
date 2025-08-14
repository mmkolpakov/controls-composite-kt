package space.kscience.controls.composite.persistence

import okio.FileSystem
import okio.Path
import space.kscience.dataforge.context.Global

actual suspend fun runTestWithStore(block: suspend (store: SnapshotStore) -> Unit) {
    val fs = FileSystem.SYSTEM
    val testDir: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "controls-persistence-test-${kotlin.random.Random.nextLong()}"
    fs.createDirectories(testDir)
    try {
        val store = FileSnapshotStore(Global, testDir.toString())
        block(store)
    } finally {
        fs.deleteRecursively(testDir)
    }
}