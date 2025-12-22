package space.kscience.controls.composite.persistence.log

import app.cash.sqldelight.db.SqlDriver
import space.kscience.controls.services.AuditLogService
import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName

/**
 * A DataForge plugin to provide and configure a [SqlDelightAuditLogService].
 * This plugin creates an instance of the service based on its configuration
 * and registers it in the context for discovery by other components.
 *
 * Example configuration:
 * ```kotlin
 * plugin(SqlDelightAuditLogPlugin) {
 *     "dbPath" put "./audit.db"
 * }
 * ```
 *
 * @param meta The configuration for this plugin. It must contain the `dbPath`.
 */
public class SqlDelightAuditLogPlugin(meta: Meta) : AbstractPlugin(meta) {
    override val tag: PluginTag get() = Companion.tag

    private var _driver: SqlDriver? = null

    /**
     * The service instance, created lazily based on the plugin's configuration.
     */
    public val service: AuditLogService by lazy {
        val dbPath = meta["dbPath"].string ?: "audit.db"
        val driver = createSqlDriver(dbPath, meta).also { _driver = it }
        SqlDelightAuditLogService(context, driver, meta)
    }

    /**
     * Provides the [space.kscience.controls.services.AuditLogService] instance to the DataForge provider mechanism.
     */
    override fun content(target: String): Map<Name, Any> = when (target) {
        AuditLogService::class.simpleName!! -> mapOf(tag.name.asName() to service)
        else -> emptyMap()
    }

    /**
     * Detaches the plugin and closes the underlying SqlDriver to release database resources.
     */
    override fun detach() {
        _driver?.close()
        super.detach()
    }

    public companion object : PluginFactory<SqlDelightAuditLogPlugin> {
        override val tag: PluginTag = PluginTag("audit.log.sqldelight", group = PluginTag.DATAFORGE_GROUP)
        override fun build(context: Context, meta: Meta): SqlDelightAuditLogPlugin = SqlDelightAuditLogPlugin(meta)
    }
}