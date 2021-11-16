package info.cepheus.axon.infrastructure.boundary.query

import java.beans.ConstructorProperties
import java.util.*

/**
 * Represents the status of a projection (more precise the tracking event processor group of it).
 *
 * @author JohT
 */
class QueryProjectionStatus
@ConstructorProperties("features")
constructor(features: Collection<Feature>) : Iterable<QueryProjectionStatus.Feature> {
    private val features: MutableCollection<Feature> = ArrayList()
    operator fun contains(feature: Feature): Boolean {
        return features.contains(feature)
    }

    fun getFeatures(): Collection<Feature> {
        return Collections.unmodifiableCollection(features)
    }

    override fun iterator(): MutableIterator<Feature> {
        return features.iterator()
    }

    override fun toString(): String {
        return "ProjectionFeatures [features=$features]"
    }

    enum class Feature {
        CAUGHT_UP,

        /**
         * Note: Currently (August 2019) it seems, that this feature stays as long as the replay token is reached. This means, that it
         * remains after replaying is done until there is a new event to handle.
         */
        REPLAYING,

        /**
         * Shows that the tracking event processor is in error state.
         */
        ERROR_STATE,

        /**
         * Shows that the tracking event processor is currently consuming events.
         */
        RUNNING
    }

    companion object {
        fun of(vararg features: Feature): QueryProjectionStatus {
            return ofAll(listOf(*features))
        }

        fun ofAll(features: Collection<Feature>): QueryProjectionStatus {
            return QueryProjectionStatus(features)
        }
    }

    init {
        this.features.addAll(features)
    }
}