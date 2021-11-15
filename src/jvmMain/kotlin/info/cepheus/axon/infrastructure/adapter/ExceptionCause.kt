package info.cepheus.axon.infrastructure.adapter

internal open class ExceptionCause protected constructor(private val exception: Throwable) {
    fun unwrapped(): RuntimeException {
        val cause = exception.cause
        if (cause is RuntimeException) {
            return cause
        }
        return if (cause == null) {
            IllegalStateException(exception.message)
        } else IllegalStateException(cause)
    }

    override fun toString(): String {
        return "ExceptionCause [exception=$exception]"
    }

    companion object {
        @kotlin.jvm.JvmStatic
		fun of(exception: Throwable): ExceptionCause {
            return ExceptionCause(exception)
        }
    }
}