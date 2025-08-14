package space.kscience.controls.composite.model

import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmInline
import kotlin.random.Random

/**
 * Represents the identity of the caller performing an action or query.
 * This can be extended to include authentication tokens, roles, and other security attributes.
 *
 * @property name A human-readable name for the principal (e.g., username).
 * @property roles A set of roles associated with the principal, used for authorization.
 * @property attributes A [Meta] object containing additional, arbitrary attributes about the principal
 *                      (e.g., session ID, source IP address, authentication token details).
 */
public interface Principal {
    public val name: String
    public val roles: Set<String>
    public val attributes: Meta
}

/**
 * A simple, data-holding implementation of [Principal].
 */
@Serializable
public data class SimplePrincipal(
    override val name: String,
    override val roles: Set<String> = emptySet(),
    override val attributes: Meta = Meta.EMPTY,
) : Principal

/**
 * A system-level principal used for internal operations or when no specific principal is provided.
 * By default, it has all permissions.
 */
public object SystemPrincipal : Principal {
    override val name: String = "system"
    override val roles: Set<String> = setOf("system")
    override val attributes: Meta = Meta.EMPTY
}

/**
 * Represents a permission required to execute an action.
 * @param id A unique identifier for the permission, e.g., "device.control.motor".
 */
@Serializable
public data class Permission(public val id: String)

/**
 * A type-safe, serializable CoroutineContext element to carry a unique correlation ID for tracing a request
 * through different components and asynchronous boundaries.
 *
 * @property id The string value of the correlation identifier.
 */
@JvmInline
@Serializable
public value class CorrelationId(public val id: String) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> get() = Key
    public companion object Key : CoroutineContext.Key<CorrelationId>
    override fun toString(): String = id
}

/**
 * A context for a single execution flow (a command plan or a query), carrying cross-cutting concerns
 * like security principal and tracing information.
 *
 * @param principal The identity of the caller. Defaults to a system principal.
 * @param correlationId A type-safe ID to trace a request through different components. Defaults to a random value.
 * @param originAddress The network address from which the original request was initiated. Can be null for internal requests.
 * @param attributes Additional metadata for the execution context, for extensibility.
 */
@Serializable
public data class ExecutionContext(
    val principal: Principal = SystemPrincipal,
    val correlationId: CorrelationId = CorrelationId("exec-${Random.nextLong().toString(16)}"),
    val originAddress: Address? = null,
    val attributes: Meta = Meta.EMPTY,
)