package space.kscience.controls.composite.persistence

/**
 * A platform-specific factory that provides the default system [okio.FileSystem].
 * The `actual` implementations will provide `FileSystem.SYSTEM`, `NodeJsFileSystem`, etc.
 */
public expect val SystemFileSystemFactory: FileSystemFactory