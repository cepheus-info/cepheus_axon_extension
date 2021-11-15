package info.cepheus.axon.infrastructure.database

import java.util.*
import java.util.regex.Pattern


class DatabaseCatalogTable protected constructor(schema: String?, tablename: String?) {
    val schema: String
    val name: String

    /**
     * Returns a new [DatabaseCatalogTable] using the given default schema, <br></br>
     * if this [DatabaseCatalogTable] has no schema and the given default schema is valid.<br></br>
     * Otherwise returns this (same) object.
     *
     * @param defaultSchema [String]
     * @return [DatabaseCatalogTable]
     */
    fun useDefaultSchema(defaultSchema: String): DatabaseCatalogTable {
        if (isUndefinedSchema(defaultSchema)) {
            return this
        }
        return if (isUndefinedSchema(schema)) {
            schemaAndTable(defaultSchema, name)
        } else this
    }

    fun toUpperCaseTablename(): DatabaseCatalogTable {
        return DatabaseCatalogTable(schema, name.toUpperCase())
    }

    fun toLowerCaseTablename(): DatabaseCatalogTable {
        return DatabaseCatalogTable(schema, name.toLowerCase())
    }

    fun matchesMap(catalogRowFields: Map<String, Any>): Boolean {
        return schemaMatchesMap(catalogRowFields) && tableMatchesMap(catalogRowFields)
    }

    private fun schemaMatchesMap(catalogRowFields: Map<String, Any>): Boolean {
        return getStringField("TABLE_SCHEM", catalogRowFields).equals(schema, ignoreCase = true)
    }

    private fun tableMatchesMap(catalogRowFields: Map<String, Any>): Boolean {
        return getStringField("TABLE_NAME", catalogRowFields).equals(name, ignoreCase = true)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val castOther = other as DatabaseCatalogTable
        return schema == castOther.schema && name == castOther.name
    }

    override fun hashCode(): Int {
        return Objects.hash(schema, name)
    }

    override fun toString(): String {
        return "DatabaseCatalogTable [schema=$schema, name=$name]"
    }

    companion object {
        private const val SCHEMA_SEPARATOR = "."
        private const val NO_SCHEMA = ""
        private val REGEX_SCHEMA_SEPARATOR = Pattern.compile("\\" + SCHEMA_SEPARATOR)
        private const val MESSAGE_COLUMN_CONTAINS_UNEXPECTED_CONTENT = "Database catalog column %s contains unexpected %s"
        fun fullQualified(fullQualifiedTableName: String): DatabaseCatalogTable {
            return schemaAndTable(extractSchema(fullQualifiedTableName), extractTablename(fullQualifiedTableName))
        }

        fun noSchema(tablename: String?): DatabaseCatalogTable {
            return schemaAndTable(NO_SCHEMA, tablename)
        }

        fun schemaAndTable(schemaName: String?, tableName: String?): DatabaseCatalogTable {
            return DatabaseCatalogTable(schemaName, tableName)
        }

        private fun getStringField(fieldname: String, catalogRowFields: Map<String, Any>): String {
            val fieldContent = catalogRowFields[fieldname.toUpperCase()]
            require(fieldContent is String) { String.format(MESSAGE_COLUMN_CONTAINS_UNEXPECTED_CONTENT, fieldname, fieldContent) }
            return fieldContent
        }

        private fun extractSchema(fullQualifiedTablename: String): String {
            return splitFullQualifiedTablename(fullQualifiedTablename)[0]
        }

        private fun extractTablename(fullQualifiedTablename: String): String {
            return splitFullQualifiedTablename(fullQualifiedTablename)[1]
        }

        private fun splitFullQualifiedTablename(fullQualifiedTablename: String): Array<String> {
            val splitted = REGEX_SCHEMA_SEPARATOR.split(fullQualifiedTablename)
            return if (splitted.size < 2) arrayOf(NO_SCHEMA, fullQualifiedTablename) else splitted
        }

        private fun isUndefinedSchema(schemaToCheck: String): Boolean {
            return NO_SCHEMA == trimmedOrEmpty(schemaToCheck)
        }

        private fun trimmedOrEmpty(value: String?): String {
            return Objects.toString(value, "").trim { it <= ' ' }
        }

        private fun notEmpty(value: String, errormessage: String): String {
            require(!value.isEmpty()) { errormessage }
            return value
        }
    }

    init {
        this.schema = trimmedOrEmpty(schema)
        name = notEmpty(trimmedOrEmpty(tablename), "the table name may not be empty")
    }
}