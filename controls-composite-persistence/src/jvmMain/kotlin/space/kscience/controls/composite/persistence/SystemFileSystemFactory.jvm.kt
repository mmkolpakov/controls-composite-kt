package space.kscience.controls.composite.persistence

import okio.FileSystem
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName

public actual val SystemFileSystemFactory: FileSystemFactory = object : FileSystemFactory {
    override val name: Name = "SYSTEM".asName()
    override fun build(context: Context, meta: Meta): FileSystem = FileSystem.SYSTEM
}