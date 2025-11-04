package space.kscience.controls.composite.persistence.log

import app.cash.sqldelight.db.SqlDriver
import space.kscience.dataforge.meta.Meta

/**
 * Creates a platform-specific [SqlDriver] for the given database path.
 * This function handles the creation of the database file and the initialization of the schema
 * by calling `AppDatabase.Schema.create(driver)`.
 *
 * @param dbPath The file path for the SQLite database.
 * @param meta Additional platform-specific configuration metadata.
 * @return A configured and initialized [SqlDriver].
 */
internal expect fun createSqlDriver(dbPath: String, meta: Meta): SqlDriver