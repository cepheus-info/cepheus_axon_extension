package info.cepheus.axon.infrastructure.transaction.jta

import org.axonframework.common.transaction.Transaction
import java.util.logging.Logger
import javax.transaction.Status
import javax.transaction.TransactionSynchronizationRegistry

/**
 * This transaction uses or contributes to an leading/driving transaction <br></br>
 * and does not perform commit's or rollback's on its own.
 *
 * @author JohT
 */
internal class ParticipatingRegisteredTransaction private constructor(private val transactionRegistry: TransactionSynchronizationRegistry) : Transaction {
    override fun commit() {
        LOGGER.fine("participation transaction - commit ommited")
    }

    @Synchronized
    override fun rollback() {
        if (transactionRegistry.transactionStatus == Status.STATUS_NO_TRANSACTION) {
            LOGGER.fine("TransactionSynchronizationRegistry no transaction to rollback")
            return
        }
        try {
            transactionRegistry.setRollbackOnly()
            LOGGER.fine("TransactionSynchronizationRegistry successfully marked for rollback")
        } catch (e: IllegalStateException) {
            LOGGER.info("TransactionSynchronizationRegistry could not be marked for rollback: $e")
        } catch (e: UnsupportedOperationException) {
            LOGGER.info("TransactionSynchronizationRegistry could not be marked for rollback: $e")
        }
    }

    override fun toString(): String {
        return "ParticipatingRegisteredTransaction [transactionRegistry=$transactionRegistry]"
    }

    companion object {
        private val LOGGER = Logger.getLogger(ParticipatingRegisteredTransaction::class.java.name)
        fun usingRegistry(transactionRegistry: TransactionSynchronizationRegistry): Transaction {
            return ParticipatingRegisteredTransaction(transactionRegistry)
        }
    }
}