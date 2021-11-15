package info.cepheus.axon.infrastructure.boundary.query

/**
 * Provides services to replay projections.
 *
 * @author JohT
 */
interface QueryProjectionManagementService {
    /**
     * This method start a replay of all events for the given processing group ("name of the projection").
     *
     * @param processingGroupName [String]
     */
    fun replayProcessingGroup(processingGroupName: String?)

    /**
     * Returns the status of the the processing group.
     *
     * @param processingGroupName [String]
     * @return QueryProjectionStatus
     */
    fun getStatus(processingGroupName: String?): QueryProjectionStatus
}