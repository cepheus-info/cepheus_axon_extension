package info.cepheus.axon.infrastructure.transaction.jta

import org.axonframework.common.transaction.Transaction
import java.util.logging.Logger
import javax.transaction.SystemException
import javax.transaction.UserTransaction

/**
 * This transaction uses or contributes to an leading/driving transaction and
 * does not perform commit or rollback on its own.
 *
 * @author JohT
 */
internal class ParticipatingUserTransaction private constructor(private val userTransaction: UserTransaction) : Transaction {
    override fun commit() {
        LOGGER.fine("participation transaction - commit ommited")
    }

    override fun rollback() {
        try {
            userTransaction.setRollbackOnly()
            LOGGER.fine("ParticipatingUserTransaction successfully marked for rollback")
        } catch (e: IllegalStateException) {
            LOGGER.info("ParticipatingUserTransaction could not be marked for rollback: " + e.message)
        } catch (e: UnsupportedOperationException) {
            LOGGER.info("ParticipatingUserTransaction could not be marked for rollback: " + e.message)
        } catch (e: SystemException) {
            LOGGER.info("ParticipatingUserTransaction could not be marked for rollback: " + e.message)
        }
    }

    override fun toString(): String {
        return "ParticipatingUserTransaction [userTransaction=$userTransaction]"
    }

    companion object {
        private val LOGGER = Logger.getLogger(ParticipatingUserTransaction::class.java.name)
        fun usingUserTransaction(userTransaction: UserTransaction): Transaction {
            return ParticipatingUserTransaction(userTransaction)
        }
    }
}