package space.kscience.controls.composite.model

/**
 * Marks declarations that are internal to the controls-composite framework.
 * These APIs may change without notice in minor releases and are not intended for public use.
 * They are exposed publicly for technical reasons, such as inline functions or cross-module access
 * from runtime implementations.
 */
@RequiresOptIn("This is an internal API for the controls-composite framework and is not stable.", RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
public annotation class InternalControlsApi