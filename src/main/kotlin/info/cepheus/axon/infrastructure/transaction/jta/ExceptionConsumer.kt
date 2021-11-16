package info.cepheus.axon.infrastructure.transaction.jta

/**
 * [Consumer], that may throw a (here hard-typed) [Exception].
 * @author Johannes Troppacher
 *
 * @see Consumer
 *
 * @param <T>
</T> */
internal fun interface ExceptionConsumer<T> {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    @Throws(Exception::class)
    fun accept(t: T)
}