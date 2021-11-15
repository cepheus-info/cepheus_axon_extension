package info.cepheus.axon.infrastructure.adapter

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Deals with query results wrapped inside [CompletableFuture]s, <br></br>
 * that need to be provided synchronously (wait for the result).
 *
 * @author JohT
 *
 * @param <T>
</T> */
internal class SynchonousQueryResult<T> protected constructor(future: CompletableFuture<T>) {
    private val future: CompletableFuture<T>

    /**
     * Waits for the result until the timeout for queries is reached or another Exception occured.
     *
     * @param messageDetailsForErrors [String] in case of an unsuccessful query
     * @return query result
     */
    fun waitAndGet(messageDetailsForErrors: String): T {
        return try {
            future.get(QUERYING_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            throw logged(ExceptionCause.of(e).unwrapped(), messageDetailsForErrors)
        } catch (e: InterruptedException) {
            throw logged(IllegalArgumentException(messageDetailsForErrors, e), messageDetailsForErrors)
        } catch (e: TimeoutException) {
            throw logged(IllegalArgumentException(messageDetailsForErrors, e), messageDetailsForErrors)
        }
    }

    /**
     * Return the original, wrapped [CompletableFuture].
     *
     * @return [CompletableFuture]
     */
    fun getFuture(): CompletableFuture<T> {
        return future
    }

    override fun toString(): String {
        return "SynchonousQueryResult [future=$future]"
    }

    companion object {
        private const val QUERYING_TIMEOUT_IN_SECONDS: Long = 30
        private val LOGGER: Logger = Logger.getLogger(SynchonousQueryResult::class.java.name)
        fun <T> of(future: CompletableFuture<T>): SynchonousQueryResult<T> {
            return SynchonousQueryResult(future)
        }

        private fun <E : Throwable?> logged(exception: E, messagedetails: String): E {
            LOGGER.log(Level.WARNING, messagedetails, exception)
            return exception
        }
    }

    init {
        this.future = future
    }
}