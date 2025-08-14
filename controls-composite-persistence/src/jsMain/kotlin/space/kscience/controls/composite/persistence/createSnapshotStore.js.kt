package space.kscience.controls.composite.persistence

import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string

internal actual fun createSnapshotStore(context: Context, meta: Meta): SnapshotStore {
    return when (val type = meta["type"].string) {
        "localStorage" -> LocalStorageSnapshotStore()
        "memory", null -> InMemorySnapshotStore()
        "file" -> throw UnsupportedOperationException("FileSnapshotStore is not supported on JS platform. Use 'localStorage' or 'memory'.")
        else -> error("Unknown snapshot store type: '$type'. Available types on this platform are 'localStorage' and 'memory'.")
    }
}