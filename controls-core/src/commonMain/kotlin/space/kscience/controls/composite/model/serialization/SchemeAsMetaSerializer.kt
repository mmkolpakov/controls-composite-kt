package space.kscience.controls.composite.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaSerializer
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec

/**
 * A generic KSerializer for any class that extends [Scheme].
 * It works by converting the Scheme to its [Meta] representation for serialization,
 * and then reading the [Meta] back to construct the Scheme instance using its [SchemeSpec].
 *
 * Use this base class for Schemes in Core and Spec modules.
 */
public open class SchemeAsMetaSerializer<S : Scheme>(private val spec: SchemeSpec<S>) : KSerializer<S> {
    override val descriptor: SerialDescriptor get() = MetaSerializer.descriptor

    override fun serialize(encoder: Encoder, value: S) {
        encoder.encodeSerializableValue(MetaSerializer, value.toMeta())
    }

    override fun deserialize(decoder: Decoder): S {
        val meta = decoder.decodeSerializableValue(MetaSerializer)
        return spec.read(meta)
    }
}
