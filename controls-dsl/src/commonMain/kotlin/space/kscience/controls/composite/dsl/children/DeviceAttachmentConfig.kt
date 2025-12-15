package space.kscience.controls.composite.dsl.children

import space.kscience.controls.composite.old.lifecycle.DeviceLifecycleConfig
import space.kscience.controls.composite.old.lifecycle.StartMode
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.parseAsName

/**
 * A DSL marker for building device attachment configurations.
 */
@DslMarker
public annotation class AttachmentDsl

/**
 * A mutable configuration object used by the attachment DSL.
 * This is not intended for direct instantiation by users.
 */
@AttachmentDsl
public class AttachmentConfiguration {
    public var lifecycle: DeviceLifecycleConfig? = null
    public var meta: Meta? = null
    public var startMode: StartMode = StartMode.NONE

    private val _childrenOverrides = mutableMapOf<Name, AttachmentConfiguration>()
    public val childrenOverrides: Map<Name, AttachmentConfiguration> get() = _childrenOverrides

    /**
     * Configures the lifecycle for this device. Overrides any static configuration.
     */
    public fun lifecycle(block: DeviceLifecycleConfig.() -> Unit) {
        this.lifecycle = DeviceLifecycleConfig(block)
    }

    /**
     * Configures the metadata for this device instance. Overrides any static configuration.
     */
    public fun meta(block: MutableMeta.() -> Unit) {
        this.meta = Meta(block)
    }

    /**
     * Sets the [StartMode] to apply after the device is attached.
     */
    public fun start(mode: StartMode) {
        this.startMode = mode
    }

    /**
     * Configures a specific child device, overriding its static and parent-defined configurations.
     *
     * @param childName The local name of the child device to configure.
     * @param block The configuration block for the child.
     */
    public fun child(childName: String, block: AttachmentConfiguration.() -> Unit) {
        _childrenOverrides.getOrPut(childName.parseAsName()) { AttachmentConfiguration() }.apply(block)
    }
}

/**
 * A DSL entry point for creating an [AttachmentConfiguration].
 */
public fun deviceAttachment(block: AttachmentConfiguration.() -> Unit): AttachmentConfiguration =
    AttachmentConfiguration().apply(block)