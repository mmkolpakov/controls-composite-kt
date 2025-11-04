package space.kscience.controls.composite.persistence.log

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import space.kscience.dataforge.meta.Meta

/**
 * Creates a platform-specific [SqlDriver] for the given database path on Native platforms.
 * This function handles the creation of the database file and the initialization of the schema
 * by using the `NativeSqliteDriver` constructor that accepts a schema.
 *
 * @param dbPath The file path for the SQLite database.
 * @param meta Additional platform-specific configuration metadata (currently unused on Native).
 * @return A configured and initialized [SqlDriver].
 */
internal actual fun createSqlDriver(dbPath: String, meta: Meta): SqlDriver {
    return NativeSqliteDriver(AppDatabase.Schema, dbPath)
}