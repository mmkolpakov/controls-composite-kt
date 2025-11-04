package space.kscience.controls.composite.model

import kotlinx.serialization.Serializable
import space.kscience.dataforge.names.Name

/**
 * Defines the type of lock to be acquired on a shared resource.
 * The semantics are analogous to read-write locks in concurrent programming.
 */
@Serializable
public enum class LockType {
    /**
     * A shared (read) lock. Multiple operations can acquire a `SHARED_READ` lock on the same resource
     * simultaneously, as long as no operation holds an `EXCLUSIVE_WRITE` lock. This is intended for
     * operations that do not modify the state of the resource.
     */
    SHARED_READ,

    /**
     * An exclusive (write) lock. Only one operation can acquire an `EXCLUSIVE_WRITE` lock on a resource at any time.
     * No other shared or exclusive locks can be held on the same resource. This is intended for operations
     * that modify the state of the resource.
     */
    EXCLUSIVE_WRITE
}

/**
 * A serializable, declarative specification for a lock required by a device property or action.
 * This object is part of a device's static blueprint and informs the runtime about the resource
 * synchronization needs of an operation before it is executed.
 *
 * The runtime is responsible for implementing a lock manager that interprets these specifications
 * to prevent race conditions and ensure safe access to shared physical or logical resources like
 * serial ports, communication buses, or files.
 *
 * @property resourceName The unique, hierarchical name of the resource to be locked.
 *                        Using [Name] instead of a simple `String` is a deliberate architectural choice that enables
 *                        powerful, flexible locking strategies. For example, a `runtime` could implement hierarchical
 *                        locking, where an exclusive lock on `bus.i2c` would prevent any locks on child resources
 *                        like `bus.i2c.sensor[0]` or `bus.i2c.actuator[5]`. It also allows for pattern-based
 *                        resource definitions, like `bus.com[*]`.
 * @property lockType The type of lock required (`SHARED_READ` or `EXCLUSIVE_WRITE`).
 */
@Serializable
public data class ResourceLockSpec(
    public val resourceName: Name,
    public val lockType: LockType,
)
