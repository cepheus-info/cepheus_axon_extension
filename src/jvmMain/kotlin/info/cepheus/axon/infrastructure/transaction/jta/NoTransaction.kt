package info.cepheus.axon.infrastructure.transaction.jta

import org.axonframework.common.transaction.Transaction

internal enum class NoTransaction : Transaction {
    INSTANCE {
        override fun commit() {
            // No action
        }

        override fun rollback() {
            // no action
        }
    }
}