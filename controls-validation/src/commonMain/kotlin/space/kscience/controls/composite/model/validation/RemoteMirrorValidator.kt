package space.kscience.controls.composite.model.validation

import space.kscience.controls.composite.model.common.DataType
import space.kscience.controls.composite.model.features.RemoteMirrorFeature
import space.kscience.controls.composite.model.services.BlueprintRegistry
import space.kscience.controls.composite.model.specs.device.DeviceBlueprintDeclaration
import space.kscience.controls.composite.model.specs.device.RemoteChildComponentConfig
import space.kscience.dataforge.names.Name

/**
 * Helper to map strict DataType to string representation for comparison.
 * This ensures backward compatibility with string-based type names in MirrorEntry.
 */
//TODO refactor?
internal val DataType.compatibleTypeNames: Set<String>
    get() = when (this) {
        DataType.INT -> setOf("kotlin.Int", "Int", "Integer")
        DataType.LONG -> setOf("kotlin.Long", "Long")
        DataType.FLOAT -> setOf("kotlin.Float", "Float")
        DataType.DOUBLE -> setOf("kotlin.Double", "Double")
        DataType.BOOLEAN -> setOf("kotlin.Boolean", "Boolean")
        DataType.STRING -> setOf("kotlin.String", "String")
        DataType.BINARY -> setOf("kotlin.ByteArray", "ByteArray")
        DataType.INT_ARRAY -> setOf("kotlin.IntArray", "IntArray")
        DataType.DOUBLE_ARRAY -> setOf("kotlin.DoubleArray", "DoubleArray")
        DataType.BYTE_ARRAY -> setOf("kotlin.ByteArray", "ByteArray")
        DataType.META -> setOf("space.kscience.dataforge.meta.Meta", "Meta")
        DataType.ENUM -> setOf("kotlin.Enum", "Enum")
        DataType.RECORD -> TODO()
    }

/**
 * A validator for the [RemoteMirrorFeature]. It checks if the remote children and their properties
 * referenced by the mirrors exist on their respective blueprint declarations and have compatible types.
 */
internal class RemoteMirrorValidator : FeatureValidator<RemoteMirrorFeature> {
    override fun validate(
        path: Name,
        declaration: DeviceBlueprintDeclaration,
        feature: RemoteMirrorFeature,
        registry: BlueprintRegistry,
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        feature.entries.forEach { entry ->
            val remoteChildConfig = declaration.children[entry.remoteChildName] as? RemoteChildComponentConfig
            if (remoteChildConfig == null) {
                errors.add(
                    ValidationError.InvalidMirror(
                        path,
                        declaration.id.value,
                        entry.localPropertyName,
                        "Remote child '${entry.remoteChildName}' not found in the blueprint."
                    )
                )
            } else {
                val remoteBlueprintDeclaration = registry.findById(remoteChildConfig.blueprintId)
                if (remoteBlueprintDeclaration == null) {
                    // This error will also be caught by the main recursive validator
                    errors.add(
                        ValidationError.BlueprintNotFound(path, entry.remoteChildName, remoteChildConfig.blueprintId)
                    )
                } else {
                    val remotePropertySpec = remoteBlueprintDeclaration.properties.find { it.name == entry.remotePropertyName }
                    if (remotePropertySpec == null) {
                        errors.add(
                            ValidationError.InvalidMirror(
                                path,
                                declaration.id.value,
                                entry.localPropertyName,
                                "Remote property '${entry.remotePropertyName}' not found on blueprint '${remoteBlueprintDeclaration.id}'."
                            )
                        )
                    } else {
                        // Check type compatibility using the helper
                        val isCompatible = entry.valueTypeName in remotePropertySpec.type.compatibleTypeNames
                                || entry.valueTypeName == remotePropertySpec.type.name // Fallback to enum name

                        if (!isCompatible) {
                            errors.add(
                                ValidationError.InvalidMirror(
                                    path,
                                    declaration.id.value,
                                    entry.localPropertyName,
                                    "Type mismatch for remote property '${entry.remotePropertyName}'. " +
                                            "Mirror expects '${entry.valueTypeName}', but remote property is '${remotePropertySpec.type}'."
                                )
                            )
                        }
                    }
                }
            }
        }
        return errors
    }
}
