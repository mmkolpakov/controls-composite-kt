package space.kscience.controls.composite.old.meta

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A base polymorphic interface for tags that provide semantic, domain-specific metadata
 * for device properties and actions. Tags are used by external systems like UI generators,
 * documentation tools, or authorization services to interpret and handle device members
 * in a context-aware manner.
 *
 * This old is open for extension. Any module can define its own custom tags by implementing
 * this interface, allowing for a clean separation of concerns and a highly extensible system.
 *
 * ### Example for a UI Module:
 *
 * ```kotlin
 * @Serializable
 * @SerialName("tag.ui")
 * data class UiHint(val group: String, val widget: String? = null) : MemberTag
 * ```
 *
 * ### Example for an Authorization Module:
 *
 * ```kotlin
 * @Serializable
 * @SerialName("tag.security")
 * data class SecurityLevel(val level: Int) : MemberTag
 * ```
 *
 * A single property or action can have multiple tags from different domains. The core runtime
 * is generally unaware of the specific semantics of tags; it only transports them.
 */
@Polymorphic
@Serializable
public sealed interface MemberTag

/**
 * A test tag for verifying the extensibility of the MemberTag system.
 *
 * @property group A semantic group for UI elements.
 * @property widget A hint for a preferred UI widget.
 */
@Serializable
@SerialName("tag.ui.test")
public data class UiTestHint(val group: String, val widget: String? = null) : MemberTag