package info.cepheus.axon.infrastructure.adapter

import info.cepheus.axon.infrastructure.boundary.query.QueryProjectionManagementService
import info.cepheus.axon.infrastructure.boundary.query.QueryProjectionStatus
import info.cepheus.axon.infrastructure.boundary.query.QueryProjectionStatus.Companion.ofAll
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.eventhandling.EventTrackerStatus
import org.axonframework.eventhandling.TrackingEventProcessor
import java.util.*
import java.util.logging.Logger
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.transaction.Transactional
import javax.transaction.Transactional.TxType

open class QueryReplayAdapter(private val eventProcessing: EventProcessingConfiguration) : QueryProjectionManagementService {
    /**
     * {@inheritDoc}
     */
    @Transactional(TxType.REQUIRED)
    override fun replayProcessingGroup(processingGroupName: String?) {
        LOGGER.info("Replay of$processingGroupName triggered using $eventProcessing")
        getTrackingEventProcessor(processingGroupName).ifPresent { trackingEventProcessor: TrackingEventProcessor ->
            LOGGER.fine("Replay of$processingGroupName in preparation")
            trackingEventProcessor.shutDown()
            trackingEventProcessor.resetTokens()
            trackingEventProcessor.start()
            LOGGER.fine("Replay of$processingGroupName started")
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun getStatus(processingGroupName: String?): QueryProjectionStatus {
        val eventTrackers = eventTrackerStatusCollection(processingGroupName)
        val features: MutableCollection<QueryProjectionStatus.Feature> = ArrayList(featuresOf(eventTrackers))
        if (getTrackingEventProcessor(processingGroupName).map { obj: TrackingEventProcessor -> obj.isRunning }
                .orElse(false)) {
            features.add(QueryProjectionStatus.Feature.RUNNING)
        }
        return ofAll(features)
    }

    private fun featuresOf(eventTrackers: Iterable<EventTrackerStatus>): Collection<QueryProjectionStatus.Feature> {
        val features: MutableCollection<QueryProjectionStatus.Feature> = HashSet()
        features.add(QueryProjectionStatus.Feature.CAUGHT_UP)
        for (eventTrackerStatus in eventTrackers) {
            if (eventTrackerStatus.isReplaying) {
                features.add(QueryProjectionStatus.Feature.REPLAYING)
            }
            if (eventTrackerStatus.isErrorState) {
                features.add(QueryProjectionStatus.Feature.ERROR_STATE)
            }
            if (!eventTrackerStatus.isCaughtUp) {
                features.remove(QueryProjectionStatus.Feature.CAUGHT_UP)
            }
        }
        return features
    }

    private fun eventTrackerStatusCollection(processingGroupName: String?): Collection<EventTrackerStatus> {
        return eventTrackerStatusForAll(processingGroupName).collect(Collectors.toList<EventTrackerStatus?>())
    }

    private fun eventTrackerStatusForAll(processingGroupName: String?): Stream<EventTrackerStatus?> {
        return getTrackingEventProcessor(processingGroupName)
            .map { obj: TrackingEventProcessor -> obj.processingStatus() }
            .map { obj: Map<Int?, EventTrackerStatus?> -> obj.values }
            .map { obj: Collection<EventTrackerStatus?> -> obj.stream() }
            .orElseGet { Stream.empty() }
            .peek({ status: EventTrackerStatus -> logTrackerStatus(status) } as ((EventTrackerStatus?) -> Unit))
    }

    private fun getTrackingEventProcessor(processingGroupName: String?): Optional<TrackingEventProcessor> {
        return eventProcessing.eventProcessorByProcessingGroup(processingGroupName, TrackingEventProcessor::class.java)
    }

    override fun toString(): String {
        return "QueryReplayAdapter [eventProcessing=$eventProcessing]"
    }

    companion object {
        private val LOGGER = Logger.getLogger(QueryReplayAdapter::class.java.name)
        private fun logTrackerStatus(status: EventTrackerStatus) {
            LOGGER.finest(
                "EventTrackerStatus:"
                        + " segment:" + status.segment
                        + " caughtUp:" + status.isCaughtUp
                        + " replaying:" + status.isReplaying
                        + " error:" + status.error
            )
        }
    }
}