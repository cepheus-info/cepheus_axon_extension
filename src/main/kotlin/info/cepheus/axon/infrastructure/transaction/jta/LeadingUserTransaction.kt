package info.cepheus.axon.infrastructure.transaction.jta

import org.axonframework.common.transaction.Transaction
import org.axonframework.eventhandling.TransactionMethodExecutionException
import java.util.logging.Level
import java.util.logging.Logger
import javax.transaction.UserTransaction

/**
 * This transaction wrapper is used for the leading/driving transaction and is
 * responsible to commit or rollback the transaction.
 *
 * @author JohT
 */
internal class LeadingUserTransaction private constructor(private val userTransaction: UserTransaction) : Transaction {
    override fun commit() {
        exceptionHandled({ obj: UserTransaction -> obj.commit() }, "transaction commit")
    }

    override fun rollback() {
        exceptionHandled({ obj: UserTransaction -> obj.rollback() }, "transaction rollback")
    }

    private fun exceptionHandled(action: ExceptionConsumer<UserTransaction>, actionDescription: String) {
        try {
            action.accept(userTransaction)
            LOGGER.fine("$actionDescription successful.")
        } catch (exception: Exception) {
            throw logged(transactionFailure(exception), "$actionDescription failed.")
        }
    }

    companion object {
        private val LOGGER = Logger.getLogger(LeadingUserTransaction::class.java.name)
        fun usingUserTransaction(userTransaction: UserTransaction): Transaction {
            return LeadingUserTransaction(userTransaction)
        }

        private fun <T : Throwable?> logged(exception: T, message: String): T {
            LOGGER.log(Level.SEVERE, message, exception)
            return exception
        }

        private fun transactionFailure(exception: Exception): TransactionMethodExecutionException {
            return TransactionMethodExecutionException(exception.message, exception)
        }
    }

    init {
        exceptionHandled({ obj: UserTransaction -> obj.begin() }, "transaction begin")
    }
}