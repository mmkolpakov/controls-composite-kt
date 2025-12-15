package space.kscience.controls.composite.metrics

import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.context.request
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import kotlin.collections.getOrPut

/**
 * A DataForge plugin for managing and providing [MetricCollector] instances.
 */
public class MetricPlugin : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag

    private val registry = mutableMapOf<String, MetricCollector>()

    /**
     * Gets or creates a [MetricCollector] with the given name.
     */
    public fun getCollector(name: String): MetricCollector =
        registry.getOrPut(name) { AtomicMetricCollector(name) }

    override fun content(target: String): Map<Name, Any> = when (target) {
        MetricCollector.TYPE -> registry.mapKeys { Name.parse(it.key) }
        else -> super.content(target)
    }

    public companion object : PluginFactory<MetricPlugin> {
        override val tag: PluginTag = PluginTag("metrics", group = PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): MetricPlugin = MetricPlugin()
    }
}

/**
 * A constant representing the target for metric collectors in the provider old.
 */
public val MetricCollector.Companion.TYPE: String get() = "metric"

/**
 * An extension function to conveniently access a [MetricCollector] from a [Context].
 */
public fun Context.metricCollector(name: String): MetricCollector {
    val plugin = request(MetricPlugin)
    return plugin.getCollector(name)
}