package space.kscience.controls.composite.model.serialization

import space.kscience.dataforge.meta.*
import kotlin.properties.ReadWriteProperty
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A [MetaConverter] for [Instant], which serializes it to an ISO-8601 string.
 */
private object InstantConverter : MetaConverter<Instant> {
    override fun readOrNull(source: Meta): Instant? = source.string?.let { Instant.parse(it) }
    override fun convert(obj: Instant): Meta = Meta(obj.toString())
}

/**
 * A [MetaConverter] for [Duration], which serializes it to an ISO-8601 duration string.
 */
private object DurationConverter : MetaConverter<Duration> {
    override fun readOrNull(source: Meta): Duration? = source.string?.let { Duration.parse(it) }
    override fun convert(obj: Duration): Meta = Meta(obj.toString())
}

/**
 * Provides a singleton instance of a [MetaConverter] for [Instant].
 */
public val MetaConverter.Companion.instant: MetaConverter<Instant> get() = InstantConverter

/**
 * Provides a singleton instance of a [MetaConverter] for [Duration].
 */
public val MetaConverter.Companion.duration: MetaConverter<Duration> get() = DurationConverter

/**
 * An extension property to safely read an [Instant] from a nullable [Meta] object.
 * It expects the meta's value to be a string in ISO-8601 format.
 * Returns `null` if the meta is null, has no value, or the value is not a valid Instant string.
 */
public val Meta?.instant: Instant? get() = this?.value?.string?.let {
    try {
        Instant.parse(it)
    } catch (e: Exception) {
        null
    }
}

/**
 * A property delegate for a non-nullable [Duration] value with a mandatory default.
 * The duration is stored as an ISO-8601 string.
 *
 * @param default The default value to be used.
 */
public fun Scheme.duration(default: Duration): ReadWriteProperty<Any?, Duration> =
    convertable(DurationConverter, default)

/**
 * A property delegate for a nullable [Duration] value.
 * The duration is stored as an ISO-8601 string.
 */
public val Scheme.duration: ReadWriteProperty<Any?, Duration?> get() = convertable(DurationConverter)