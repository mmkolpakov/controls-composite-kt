package space.kscience.controls.composite.model.common

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * A strict, type-safe wrapper for a Unit of Measure code.
 *
 * This wrapper enforces the use of the **UCUM** (Unified Code for Units of Measure) standard
 * (e.g., "m/s2", "Cel", "kg", "[degF]").
 *
 * Using a value class instead of a raw String prevents accidental assignment of non-unit strings
 * (like descriptions or device names) to unit fields in specifications.
 *
 * @property ucumCode The case-sensitive UCUM code string.
 */
@Serializable
@JvmInline
public value class PhysicalUnit(public val ucumCode: String) {
    init {
        require(ucumCode.isNotBlank()) { "PhysicalUnit code cannot be blank." }
    }

    override fun toString(): String = ucumCode

    public companion object {
        /**
         * Represents a dimensionless quantity (e.g., count, ratio, percentage).
         * Corresponds to the UCUM code "1".
         */
        public val DIMENSIONLESS: PhysicalUnit = PhysicalUnit("1")

        /**
         * Percentage (dimensionless ratio * 100).
         */
        public val PERCENT: PhysicalUnit = PhysicalUnit("%")

        // Common Base Units
        public val METER: PhysicalUnit = PhysicalUnit("m")
        public val SECOND: PhysicalUnit = PhysicalUnit("s")
        public val KILOGRAM: PhysicalUnit = PhysicalUnit("kg")
        public val AMPERE: PhysicalUnit = PhysicalUnit("A")
        public val KELVIN: PhysicalUnit = PhysicalUnit("K")
        public val MOLE: PhysicalUnit = PhysicalUnit("mol")
        public val CANDELA: PhysicalUnit = PhysicalUnit("cd")

        // Common Derived Units
        public val CELSIUS: PhysicalUnit = PhysicalUnit("Cel")
        public val HERTZ: PhysicalUnit = PhysicalUnit("Hz")
        public val VOLT: PhysicalUnit = PhysicalUnit("V")
        public val WATT: PhysicalUnit = PhysicalUnit("W")
        public val JOULE: PhysicalUnit = PhysicalUnit("J")
        public val PASCAL: PhysicalUnit = PhysicalUnit("Pa")
        public val NEWTON: PhysicalUnit = PhysicalUnit("N")
    }
}

/**
 * Extension to easily convert a valid UCUM string to a [PhysicalUnit].
 */
public fun String.toPhysicalUnit(): PhysicalUnit = PhysicalUnit(this)
