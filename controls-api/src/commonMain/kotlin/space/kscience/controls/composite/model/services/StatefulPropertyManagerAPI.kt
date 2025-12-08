package space.kscience.controls.composite.model.services

import space.kscience.controls.composite.model.contracts.runtime.StatefulPropertyUpdateChannelProvider
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta

/**
 * A formal [Plugin] interface for the stateful property management system.
 * By defining this in the `model` module, we allow the `runtime` to provide an
 * implementation while the `dsl` can safely depend on the abstraction.
 */
public interface StatefulPropertyManagerAPI : StatefulPropertyUpdateChannelProvider, Plugin {
    public companion object : PluginFactory<StatefulPropertyManagerAPI> {
        override val tag: PluginTag = PluginTag("statefulPropertyManager", group = PluginTag.DATAFORGE_GROUP)
        override fun build(context: Context, meta: Meta): StatefulPropertyManagerAPI {
            error("StatefulPropertyManagerAPI is a service interface and requires a runtime implementation.")
        }
    }
}
