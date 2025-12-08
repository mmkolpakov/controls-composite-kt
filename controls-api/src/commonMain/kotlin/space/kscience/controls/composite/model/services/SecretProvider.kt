package space.kscience.controls.composite.model.services

import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta

/**
 * A contract for a service that resolves secret references into their actual values.
 * This contract is a key component for secure declarative configuration (GitOps), as it allows
 * device blueprints and state descriptors to refer to secrets without containing their actual values.
 *
 * The runtime is responsible for providing a concrete implementation of this service,
 * for example, by reading from Kubernetes Secrets, HashiCorp Vault, environment variables, or a secure properties file.
 */
public interface SecretProvider : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Resolves a secret reference into its actual value.
     *
     * @param ref A structured [Meta] reference describing the secret to resolve. The structure of the meta
     *            is specific to the provider implementation. For example: `Meta { "k8s.secret" put "my-db-secret/password" }`
     *            or `Meta { "env" put "DB_PASSWORD" }`.
     * @return The secret value as a String, or null if the secret cannot be found.
     */
    public suspend fun resolve(ref: Meta): String?

    public companion object : PluginFactory<SecretProvider> {
        override val tag: PluginTag = PluginTag("device.secrets", group = PluginTag.DATAFORGE_GROUP)

        /**
         * The default factory throws an error because SecretProvider is a service contract
         * that requires a concrete, environment-specific implementation from a runtime or integration module.
         */
        override fun build(context: Context, meta: Meta): SecretProvider {
            error("SecretProvider is a service interface and requires a runtime-specific implementation.")
        }
    }
}
