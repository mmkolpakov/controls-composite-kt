package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.PhysicalUnit
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.dataforge.meta.*

/**
 * A [MetaConverter] for [PhysicalUnit] that serializes it to/from a simple string in the Meta tree.
 */
private object PhysicalUnitMetaConverter : MetaConverter<PhysicalUnit> {
    override fun readOrNull(source: Meta): PhysicalUnit? =
        source.string?.let { PhysicalUnit(it) }

    override fun convert(obj: PhysicalUnit): Meta = Meta(obj.ucumCode.asValue())
}

/**
 * A reusable, declarative specification for physical measurement properties.
 * This allows UI components to correctly display units, perform automatic conversions,
 * and render gauges or graphs with appropriate limits.
 *
 * It enforces the use of **UCUM** standards for units via [PhysicalUnit].
 *
 * @property unit The physical unit of measure (UCUM standard).
 * @property min The absolute minimum physical value (hard limit).
 * @property max The absolute maximum physical value (hard limit).
 * @property nominalLow The lower bound of the "normal" or "safe" operating range.
 * @property nominalHigh The upper bound of the "normal" or "safe" operating range.
 * @property precision The number of decimal places to display or significant digits.
 */
@Serializable(with = MeasurementSpec.Serializer::class)
public class MeasurementSpec : Scheme() {
    /**
     * The physical unit of measure.
     */
    public var unit: PhysicalUnit? by convertable(PhysicalUnitMetaConverter)

    /**
     * The absolute minimum value possible or allowed for this measurement.
     */
    public var min: Double? by double()

    /**
     * The absolute maximum value possible or allowed for this measurement.
     */
    public var max: Double? by double()

    /**
     * The lower bound of the nominal (expected) range. Values below this might indicate a warning.
     */
    public var nominalLow: Double? by double()

    /**
     * The upper bound of the nominal (expected) range. Values above this might indicate a warning.
     */
    public var nominalHigh: Double? by double()

    /**
     * A hint for the number of decimal places to display in UIs.
     */
    public var precision: Int? by int()

    public companion object : SchemeSpec<MeasurementSpec>(::MeasurementSpec)
    public object Serializer : SchemeAsMetaSerializer<MeasurementSpec>(Companion)
}
