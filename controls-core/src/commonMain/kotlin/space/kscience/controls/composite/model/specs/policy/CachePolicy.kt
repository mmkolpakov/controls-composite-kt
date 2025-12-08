package space.kscience.controls.composite.model.specs.policy

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.meta.listOfConvertable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.duration
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Defines the scope in which a cached action result is considered valid.
 * This allows for fine-grained control over cache sharing in multi-user or multi-hub environments.
 */
@Serializable
public enum class CacheScope {
    /**
     * The cached result is shared across all users and components within a single hub instance.
     * This is the default and most common scope for caching results of idempotent, read-only operations.
     */
    PER_HUB,

    /**
     * The cached result is specific to the [space.kscience.controls.composite.model.common.Principal] who initiated the action.
     * This is useful for actions whose results depend on user permissions or other user-specific context.
     * Two different users calling the same action with the same arguments will receive separately cached results.
     */
    PER_PRINCIPAL,

    /**
     * The cached result is shared globally across all hubs connected to a common backend cache
     * (e.g., a distributed cache like Redis). This scope requires a specialized runtime implementation
     * of the cache service and is intended for large-scale distributed systems.
     */
    GLOBAL
}

/**
 * A declarative policy that describes how the result of an idempotent action should be cached.
 *
 * @property ttl The Time-To-Live for a cached entry.
 * @property scope The [CacheScope] defining the visibility and sharing of the cached entry.
 * @property invalidationEvents A list of topic-like [Name] patterns for event-driven cache invalidation.
 */
@Serializable(with = CachePolicy.Serializer::class)
public class CachePolicy : Scheme() {
    public var ttl: Duration by duration(15.minutes)
    public var scope: CacheScope by enum(CacheScope.PER_HUB)
    public var invalidationEvents: List<Name> by listOfConvertable(Name.serializer())

    public companion object : SchemeSpec<CachePolicy>(::CachePolicy)
    public object Serializer : SchemeAsMetaSerializer<CachePolicy>(Companion)
}
