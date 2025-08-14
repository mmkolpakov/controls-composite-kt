package space.kscience.controls.composite.persistence

import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string

internal actual fun createSnapshotStore(context: Context, meta: Meta): SnapshotStore {
    return when (val type = meta["type"].string) {
        "file" -> {
            val path = meta["path"].string ?: error("File snapshot store requires a 'path' to be configured.")
            FileSnapshotStore(context, path)
        }
        "memory", null -> InMemorySnapshotStore()
        else -> error("Unknown snapshot store type: '$type'. Available types on this platform are 'file' and 'memory'.")
    }
}