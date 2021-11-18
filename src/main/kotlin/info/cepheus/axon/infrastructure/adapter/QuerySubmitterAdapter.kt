package info.cepheus.axon.infrastructure.adapter

import info.cepheus.axon.infrastructure.boundary.query.QuerySubmitterService
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import reactor.core.publisher.Flux
import reactor.util.Loggers
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.logging.Level

open class QuerySubmitterAdapter(private val queryGateway: QueryGateway) : QuerySubmitterService {

    override fun <R, Q> query(query: Q, responseType: Class<R>): CompletableFuture<R> {
        return queryGateway.query(query, responseType)
    }

    /**
     * Submits the given `query`. <br></br>
     * Returns the initial result as a [CompletableFuture]-[List] of
     * response elements. <br></br>
     * Gets notified, when the result changes.
     *
     * @param query               The `query` to be sent
     * @param responseElementType The response element type used for this query
     * @param resultUpdateAction  [Consumer] that gets notified on new
     * results.
     * @param                     <Q> The type of the query
     * @param                     <R> The response class contained in the given
     * `responseType`
     * @return A [CompletableFuture] containing the query
     * result as dictated by the given `responseElementType`
    </R></Q> */
    override fun <Q, R> querySubscribedList(query: Q, responseElementType: Class<R>,
                                            resultUpdateAction: Consumer<in List<R>>): CompletableFuture<List<R>> {
        val fetchQuery: SubscriptionQueryResult<List<R>, R> = queryGateway.subscriptionQuery(query,
                ResponseTypes.multipleInstancesOf(responseElementType),  //
                ResponseTypes.instanceOf(responseElementType))
        val updates: Flux<List<R>> = fetchQuery.updates().log(REACTIVE_LOGGER, Level.FINEST, false).buffer(Duration.ofMillis(125))
        updates.subscribe(resultUpdateAction)
        return fetchQuery.initialResult().toFuture()
    }

    override fun <R> waitFor(queryResult: CompletableFuture<R>, queryDetails: String?): R {
        val details = String.format("Query %s failed", queryDetails)
        return SynchonousQueryResult.of(queryResult).waitAndGet(details)
    }

    override fun toString(): String {
        return "QuerySubmitterAdapter [queryGateway=$queryGateway]"
    }

    companion object {
        private val REACTIVE_LOGGER: reactor.util.Logger = Loggers.getLogger(QuerySubmitterAdapter::class.java.name)
    }

}