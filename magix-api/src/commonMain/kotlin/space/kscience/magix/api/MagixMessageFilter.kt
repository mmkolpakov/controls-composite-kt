package space.kscience.magix.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.Serializable
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.matches

/**
 * A declarative filter for [MagixMessage]s, used to specify subscription criteria.
 * A `null` value for any collection means that the filter does not apply to that field (wildcard).
 *
 * @property format A collection of allowed format identifiers.
 * @property source A collection of allowed source endpoint identifiers.
 * @property target A collection of allowed target endpoint identifiers. A `null` in this collection
 *                  matches broadcast messages (where `targetEndpoint` is null).
 * @property topicPattern An optional topic pattern for content-based filtering.
 */
@Serializable
public data class MagixMessageFilter(
    val format: Collection<String>? = null,
    val source: Collection<String>? = null,
    val target: Collection<String?>? = null,
    val topicPattern: Name? = null,
) {

    /**
     * Checks if a given [MagixMessage] is accepted by this filter.
     *
     * @param message The message to check.
     * @return `true` if the message passes all filter criteria, `false` otherwise.
     */
    @OptIn(DFExperimental::class)
    public fun accepts(message: MagixMessage): Boolean =
        (format?.contains(message.format) ?: true)
                && (source?.contains(message.sourceEndpoint) ?: true)
                && (target?.contains(message.targetEndpoint) ?: true)
                && (topicPattern?.let { pattern -> message.topic?.matches(pattern) ?: false } ?: true)

    public companion object {
        /**
         * A singleton filter that accepts all messages.
         */
        public val ALL: MagixMessageFilter = MagixMessageFilter()
    }
}

/**
 * A convenience extension to apply a [MagixMessageFilter] to a [Flow] of [MagixMessage]s.
 * If the filter is [MagixMessageFilter.ALL], the original flow is returned without modification
 * to avoid unnecessary overhead.
 */
public fun Flow<MagixMessage>.filter(filter: MagixMessageFilter): Flow<MagixMessage> = if (filter == MagixMessageFilter.ALL) {
    this
} else {
    filter(filter::accepts)
}