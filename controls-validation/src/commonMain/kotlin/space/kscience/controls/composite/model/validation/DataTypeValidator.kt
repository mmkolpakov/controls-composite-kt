package space.kscience.controls.composite.model.validation

import space.kscience.controls.composite.model.common.DataType
import space.kscience.controls.composite.model.data.RawValue

/**
 * A utility object responsible for validating that a [RawValue] payload matches the expected [DataType].
 * This ensures type safety within the Data Plane without the overhead of full object reflection.
 */
public object DataTypeValidator {

    /**
     * Checks if the provided [value] is compatible with the declared [type].
     *
     * @param type The strict data type definition from the property descriptor.
     * @param value The raw data value from the telemetry or state update.
     * @return `true` if the value matches the type, `false` otherwise.
     */
    public fun validate(type: DataType, value: RawValue): Boolean {
        return when (type) {
            DataType.INT -> value is RawValue.I
            DataType.LONG -> value is RawValue.L || value is RawValue.I || value is RawValue.UL || value is RawValue.UI
            DataType.FLOAT -> value is RawValue.F || value is RawValue.D // Allow precision widening if needed
            DataType.DOUBLE -> value is RawValue.D || value is RawValue.F
            DataType.BOOLEAN -> value is RawValue.B
            DataType.STRING -> value is RawValue.S
            // Binary is for blobs/files
            DataType.BINARY -> value is RawValue.Bin
            DataType.INT_ARRAY -> value is RawValue.IArr
            DataType.DOUBLE_ARRAY -> value is RawValue.DArr
            // Byte Array is for numerical arrays
            DataType.BYTE_ARRAY -> value is RawValue.BArr
            DataType.RECORD -> value is RawValue.Record
            DataType.META -> value is RawValue.M
            // Enums are transmitted as strings in RawValue
            DataType.ENUM -> value is RawValue.S
        }
    }

    /**
     * Validates the value and throws an exception if invalid.
     *
     * @param propertyName The name of the property being validated (for error context).
     * @param type The expected type.
     * @param value The actual value.
     * @throws IllegalArgumentException if the types do not match.
     */
    public fun check(propertyName: String, type: DataType, value: RawValue) {
        if (!validate(type, value)) {
            throw IllegalArgumentException(
                "Type mismatch for property '$propertyName': Expected $type but got ${value::class.simpleName}"
            )
        }
    }
}
