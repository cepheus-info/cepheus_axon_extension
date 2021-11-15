package info.cepheus.axon.infrastructure.transaction.jta

import org.axonframework.common.transaction.Transaction
import org.axonframework.common.transaction.TransactionManager
import java.util.logging.Logger
import javax.transaction.Status
import javax.transaction.SystemException
import javax.transaction.TransactionSynchronizationRegistry
import javax.transaction.UserTransaction

/**
 * Provides the [TransactionManager] for Axon.
 *
 * @author JohT
 */
class JtaTransactionManager protected constructor(private val userTransaction: UserTransaction?,
                                                  private val transactionRegistry: TransactionSynchronizationRegistry?) : TransactionManager {
    /**
     * {@inheritDoc}
     */
    override fun startTransaction(): Transaction {
        if (isTransactionynchronizationRegistryAvailable) {
            return ParticipatingRegisteredTransaction.usingRegistry(transactionRegistry!!)
        }
        return if (isUserTransactionAvailable) {
            if (isInTransaction) {
                ParticipatingUserTransaction.usingUserTransaction(userTransaction!!)
            } else LeadingUserTransaction.usingUserTransaction(userTransaction!!)
        } else NoTransaction.INSTANCE
    }

    private val isUserTransactionAvailable: Boolean
        get() = userTransaction != null
    private val isInTransaction: Boolean
        get() = try {
            userTransaction!!.status != Status.STATUS_NO_TRANSACTION
        } catch (e: SystemException) {
            LOGGER.info("UserTransaction not availalbe: " + e.message)
            false
        } catch (e: IllegalStateException) {
            LOGGER.info("UserTransaction not availalbe: " + e.message)
            false
        } catch (e: UnsupportedOperationException) {
            LOGGER.info("UserTransaction not availalbe: " + e.message)
            false
        }
    private val isTransactionynchronizationRegistryAvailable: Boolean
        get() = transactionRegistry != null

    override fun toString(): String {
        return ("JtaTransactionManager [userTransaction=" + userTransaction + ", transactionRegistry="
                + transactionRegistry + "]")
    }

    companion object {
        private val LOGGER = Logger.getLogger(JtaTransactionManager::class.java.getName())
        fun using(user: UserTransaction?, registry: TransactionSynchronizationRegistry?): TransactionManager {
            return JtaTransactionManager(user, registry)
        }
    }
}