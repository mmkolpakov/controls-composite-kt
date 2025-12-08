package space.kscience.controls.composite.model.meta

import kotlinx.serialization.Polymorphic

/**
 * A base polymorphic interface for tags that provide semantic, domain-specific metadata
 * for device properties and actions. Tags are used by external systems like UI generators,
 * documentation tools, or authorization services to interpret and handle device members
 * in a context-aware manner.
 *
 * This model is open for extension. Any module can define its own custom tags by implementing
 * this interface, allowing for a clean separation of concerns and a highly extensible system.
 *
 * A single property or action can have multiple tags from different domains. The core runtime
 * is generally unaware of the specific semantics of tags; it only transports them.
 */
@Polymorphic
public interface MemberTag
