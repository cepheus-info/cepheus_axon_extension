package info.cepheus.axon.infrastructure.boundary.command

import java.util.concurrent.CompletableFuture

/**
 * Provides methods to send command messages.
 *
 * @author JohT
 */
interface CommandEmitterService {
    /**
     * Sends the given `command` synchronously (blocks until complete).<br></br>
     * is interrupted, this method returns `null`.
     *
     * @param command The command to dispatch
     * @param         <R> The type of result expected from command execution
     * @return the result of command execution, or `null` if the thread was interrupted while waiting for the command
     * to execute
     * @throws IllegalStateException when an checked exception occurred while processing the command
    </R> */
    fun <R> sendAndWaitFor(command: Any?): R

    fun <R> send(command: Any?): CompletableFuture<R>
}