package info.cepheus.axon.infrastructure.boundary.query

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Provides methods to submit queries.
 *
 * @author JohT
 */
interface QuerySubmitterService {
    /**
     * Submits the given `query`. Expects a response with the given `responseType` from a single source.
     *
     * @param query        The `query` to be sent
     * @param responseType The response type used for this query
     * @param              <R> The response class contained in the given `responseType`
     * @param              <Q> The query class
     * @return A [CompletableFuture] containing the query result as dictated by the given
     * `responseType`
    </Q></R> */
    fun <R, Q> query(query: Q, responseType: Class<R>): CompletableFuture<R>

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
    // void handle(Consumer<? super I> initialResultConsumer, Consumer<? super U> updateConsumer)
    fun <Q, R> querySubscribedList(query: Q, responseElementType: Class<R>,
                                   resultUpdateAction: Consumer<in List<R>>): CompletableFuture<List<R>>

    /**
     * Supports synchronous queries to wait for the result, including timeout-handling.
     *
     * @param queryResult  [CompletableFuture]
     * @param queryDetails [String]
     * @return query result
     */
    fun <R> waitFor(queryResult: CompletableFuture<R>, queryDetails: String?): R
}