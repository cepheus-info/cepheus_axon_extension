package info.cepheus.axon.infrastructure.adapter

import info.cepheus.axon.infrastructure.adapter.ExceptionCause.Companion.of
import info.cepheus.axon.infrastructure.boundary.command.CommandEmitterService
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.gateway.CommandGateway
import java.util.concurrent.CompletableFuture
import javax.transaction.Transactional

open class CommandEmitterAdapter(private val commandGateway: CommandGateway) : CommandEmitterService {
    @Transactional(Transactional.TxType.REQUIRED)
    @Throws(IllegalStateException::class)
    override fun <R> sendAndWaitFor(command: Any?): R {
        return try {
            commandGateway.sendAndWait(command)
        } catch (e: CommandExecutionException) {
            throw of(e).unwrapped()
        }
    }

    @Transactional(Transactional.TxType.REQUIRED)
    @Throws(IllegalStateException::class)
    override fun <R> send(command: Any?): CompletableFuture<R> {
        return commandGateway.send(command)
    }

    override fun toString(): String {
        return "CommandEmitterAdapter [commandGateway=$commandGateway]"
    }
}