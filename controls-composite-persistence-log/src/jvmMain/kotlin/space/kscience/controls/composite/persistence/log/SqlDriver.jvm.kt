package space.kscience.controls.composite.persistence.log

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import space.kscience.dataforge.meta.Meta

/**
 * Creates a platform-specific [SqlDriver] for the given database path on JVM.
 * This function handles the creation of the database file and the initialization of the schema
 * by calling `AppDatabase.Schema.create(driver)`.
 *
 * @param dbPath The file path for the SQLite database.
 * @param meta Additional platform-specific configuration metadata (currently unused on JVM).
 * @return A configured and initialized [SqlDriver].
 */
internal actual fun createSqlDriver(dbPath: String, meta: Meta): SqlDriver {
    return JdbcSqliteDriver("jdbc:sqlite:$dbPath", schema = AppDatabase.Schema)
}