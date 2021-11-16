package info.cepheus.axon.infrastructure.database

import java.util.*


class DatabaseCatalogColumn internal constructor(table: DatabaseCatalogTable?, columnName: String?) {
    val table: DatabaseCatalogTable = Objects.requireNonNull(table, "table may not be null")!!
    val name: String

    fun matchesMap(catalogRowFields: Map<String, Any>): Boolean {
        return nameMatchesMap(catalogRowFields) && table.matchesMap(catalogRowFields)
    }

    private fun nameMatchesMap(catalogRowFields: Map<String, Any>): Boolean {
        return getStringField("COLUMN_NAME", catalogRowFields).equals(name, ignoreCase = true)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val castOther = other as DatabaseCatalogColumn
        return table == castOther.table && name == castOther.name
    }

    override fun hashCode(): Int {
        return Objects.hash(table, name)
    }

    override fun toString(): String {
        return "TableColumn [table=$table, name=$name]"
    }

    companion object {
        private const val MESSAGE_COLUMN_CONTAINS_UNEXPECTED_CONTENT = "Database catalog column %s contains unexpected %s"

        /**
         * Creates a [DatabaseCatalogColumn] for the given [DatabaseCatalogTable] and [String] "column".
         *
         * @param table [DatabaseCatalogTable]
         * @param column [String]
         * @return [DatabaseCatalogColumn]
         */
        fun columnIn(table: DatabaseCatalogTable?, columnname: String?): DatabaseCatalogColumn {
            return DatabaseCatalogColumn(table, columnname)
        }

        private fun getStringField(fieldName: String, catalogRowFields: Map<String, Any>): String {
            val fieldContent = catalogRowFields[fieldName.toUpperCase()]
            require(fieldContent is String) { String.format(MESSAGE_COLUMN_CONTAINS_UNEXPECTED_CONTENT, fieldName, fieldContent) }
            return fieldContent
        }

        private fun trimmedOrEmpty(value: String?): String {
            return Objects.toString(value, "").trim { it <= ' ' }
        }

        private fun notEmpty(value: String, errormessage: String): String {
            require(value.isNotEmpty()) { errormessage }
            return value
        }
    }

    init {
        name = notEmpty(trimmedOrEmpty(columnName), "columnName may not be empty")
    }
}