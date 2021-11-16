package info.cepheus.axon.infrastructure.database

import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet


/**
 * Queries the database catalog using the given [Connection]. <br></br>
 * The result contains all columns that match the optionally given schema and tablename pattern.
 *
 *
 *
 * @author JohT
 * @see DatabaseMetaData.getColumns
 */
class DatabaseCatalogQuery protected constructor(private val connection: Connection) {
    private val results: MutableList<Map<String, Any>> = ArrayList()
    private var queryFinished = false
    private lateinit var defaultSchema: String
    private val tables: MutableSet<DatabaseCatalogTable> = HashSet<DatabaseCatalogTable>()

    /**
     * Sets the default database schema, that will be used for all subsequent calls of [.tablename].<br></br>
     * If the table is not given in a full qualified manner (schema + "." + table),<br></br>
     * the default schema will be used.
     *
     *
     * An empty schema is assumed, if it is set to `null`.<br></br>
     * The schema will be trimmed, removing leading and trailing spaces.
     *
     * @param schemaToUse [String]
     * @return [DatabaseCatalogQuery]
     * @see DatabaseMetaData.getColumns
     */
    fun defaultSchema(schemaToUse: String): DatabaseCatalogQuery {
        defaultSchema = schemaToUse
        return this
    }

    /**
     * Adds another table name to be queried from the database catalog.
     *
     *
     * The table name should not be empty or `null`.<br></br>
     * The table name will be trimmed, removing leading and trailing spaces.
     *
     * @param tablenameToQuery [String]
     * @return [DatabaseCatalogQuery]
     * @see DatabaseMetaData.getColumns
     */
    fun tablename(tablenameToQuery: String): DatabaseCatalogQuery {
        return tablename(DatabaseCatalogTable.fullQualified(tablenameToQuery))
    }

    /**
     * Adds another [DatabaseCatalogTable] to be queried from the database catalog.<br></br>
     * To make shure, that the table is found, an upper and lower case invariant is added too.
     *
     *
     * [DatabaseCatalogTable] should not be `null`.<br></br>
     *
     * @param tablenameToQuery [DatabaseCatalogTable]
     * @return [DatabaseCatalogQuery]
     * @see DatabaseMetaData.getColumns
     */
    fun tablename(tablenameToQuery: DatabaseCatalogTable): DatabaseCatalogQuery {
        val table: DatabaseCatalogTable = tablenameToQuery.useDefaultSchema(defaultSchema)
        tables.add(table)
        tables.add(table.toUpperCaseTablename())
        tables.add(table.toLowerCaseTablename())
        return this
    }

    /**
     * Gets the (lower case) name of the column type for the given [DatabaseCatalogColumn].
     *
     * @param columnName [String]
     * @param columnTypeName [String]
     * @return `true` on a match
     */
    fun getColumnType(columnToFind: DatabaseCatalogColumn): String {
        if (!tables.contains(columnToFind.table) && results.isNotEmpty()) {
            val message = java.lang.String.format(MESSAGE_TABLE_NOT_CONTAINED_IN_RESULT, columnToFind.table)
            throw IllegalArgumentException(message)
        }
        return getResults().stream()
                .filter(columnToFind::matchesMap)
                .map { column: Map<String, Any> -> column["TYPE_NAME"].toString().toLowerCase() }
                .findFirst().orElse("")
    }

    /**
     * Triggers database catalog query (if not already done).
     *
     *
     * Please use [.getResults] to query the database catalog and get the results directly.<br></br>
     * This method is mean't to be used to trigger the query explicitly, <br></br>
     * e.g. when the [Connection] is only opened for this operation. <br></br>
     *
     * @return [DatabaseCatalogQuery]
     */
    fun triggerQuery(): DatabaseCatalogQuery {
        if (queryFinished) {
            return this
        }
        for (tablename in tables) {
            try {
                queryCatalog(tablename).use { resultSet ->
                    while (resultSet.next()) {
                        results.add(resultSetRowAsMap(resultSet))
                    }
                }
            } catch (e: SQLException) {
                throw IllegalStateException("error during database catalog query using " + toString(), e)
            }
        }
        queryFinished = true
        return this
    }

    /**
     * Queries the database catalog (if not already done) and returns the results in a generic way.
     *
     * @return [List] of [Map]s with the column name as [String]-key and the column content as [Object]-value.
     */
    fun getResults(): List<Map<String, Any>> {
        if (!queryFinished) {
            triggerQuery()
        }
        return Collections.unmodifiableList(results)
    }

    @Throws(SQLException::class)
    protected fun queryCatalog(table: DatabaseCatalogTable): ResultSet {
        return connection.metaData.getColumns(connection.catalog, table.schema, table.name, null)
    }

    override fun toString(): String {
        return ("DatabaseCatalogQuery [connection=" + connection + ", results=" + results + ", defaultSchema=" + defaultSchema + ", tables="
                + tables + "]")
    }

    companion object {
        private const val MESSAGE_TABLE_NOT_CONTAINED_IN_RESULT = "The already done database catalog query does not contain table %s"
        fun forConnection(connection: Connection): DatabaseCatalogQuery {
            return DatabaseCatalogQuery(connection)
        }

        @Throws(SQLException::class)
        private fun resultSetRowAsMap(columns: ResultSet): Map<String, Any> {
            val resultSetMetaData = columns.metaData
            val row: MutableMap<String, Any> = HashMap()
            for (columnIndex in 1..resultSetMetaData.columnCount) {
                row[resultSetMetaData.getColumnName(columnIndex)] = columns.getObject(columnIndex)
            }
            return row
        }
    }
}