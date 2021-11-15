package info.cepheus.axon.infrastructure.boundary.query

/**
 * Lists available processor configurations for event handlers.
 *
 * @author JohT
 */
enum class QueryProcessor(val s: String) {
    TRACKING("tracking"),
    SUBSCRIBING("subscribing");

    val isDefault: Boolean
        get() = equals(TRACKING)
}