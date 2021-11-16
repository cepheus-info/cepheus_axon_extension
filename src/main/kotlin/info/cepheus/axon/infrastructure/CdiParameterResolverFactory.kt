package info.cepheus.axon.infrastructure

import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.ParameterResolver
import org.axonframework.messaging.annotation.ParameterResolverFactory
import java.lang.reflect.Executable
import java.lang.reflect.Parameter
import java.util.*
import java.util.function.Supplier
import java.util.logging.Level
import java.util.logging.Logger
import javax.enterprise.inject.Instance
import javax.enterprise.inject.Typed
import javax.enterprise.inject.spi.CDI


/**
 * Resolves method parameters using CDI.
 */
class CdiParameterResolverFactory @JvmOverloads constructor(cdiSupplier: Supplier<CDI<Any>> = Supplier { currentCdi() }) : ParameterResolverFactory {
    private val cdiSupplier: Supplier<CDI<Any>>
    override fun createInstance(executable: Executable, parameters: Array<Parameter>, parameterIndex: Int): ParameterResolver<*> {
        val parameter = parameters[parameterIndex]
        val parameterType = parameter.type
        val parameterAnnotations = parameter.annotations
        val parameterAnnotationNames = Arrays.toString(parameterAnnotations)
        LOGGER.finest(String.format(RESOLVE_CDI_REFERENCE, parameterType.name, parameterAnnotationNames))
        val instance = cdiSupplier.get().select(parameterType, *parameterAnnotations)
        if (instance.isUnsatisfied) {
            throw logged(resolutionFailure(UNSATISFIED_CDI_REFERENCE, parameterType.name, parameterAnnotationNames))
        }
        if (instance.isAmbiguous) {
            throw logged(resolutionFailure(AMBIGUOUS_CDI_REFERENCE, parameterType.name, parameterAnnotationNames))
        }
        LOGGER.finest(String.format(RESOLVED_CDI_REFERENCE, parameterType.name, parameterAnnotationNames))
        return CdiParameterResolver(instance)
    }

    override fun toString(): String {
        return "CdiParameterResolverFactory [cdi=$cdiSupplier]"
    }

    @Typed
    class CdiParameterResolver(private val instance: Instance<*>) : ParameterResolver<Any> {
        override fun resolveParameterValue(message: Message<*>?): Any {
            return instance.get()
        }

        override fun matches(message: Message<*>?): Boolean {
            return true
        }

        override fun toString(): String {
            return "CdiParameterResolver [instance=$instance]"
        }
    }

    companion object {
        private val LOGGER = Logger.getLogger(CdiParameterResolverFactory::class.java.name)
        private const val CDI_NOT_AVAILABLE = "CDI parameter resolving not supported"
        private const val AMBIGUOUS_CDI_REFERENCE = "Ambiguous CDI reference for parameter type %s with annotations %s"
        private const val UNSATISFIED_CDI_REFERENCE = "Unsatisfied CDI reference for parameter type %s with annotations %s"
        private const val RESOLVE_CDI_REFERENCE = "Starting to resolve CDI reference for parameter type %s with annotations %s"
        private const val RESOLVED_CDI_REFERENCE = "Finished resolving CDI reference for parameter type %s with annotations %s"
        private fun currentCdi(): CDI<Any> {
            return try {
                CDI.current()
            } catch (e: IllegalStateException) {
                throw logged(resolutionFailure(CDI_NOT_AVAILABLE, e))
            }
        }

        private fun <T : RuntimeException?> logged(exception: T): T {
            LOGGER.log(Level.WARNING, exception!!.message, exception)
            return exception
        }

        private fun resolutionFailure(message: String, vararg args: Any): RuntimeException {
            return IllegalStateException(String.format(message, *args))
        }
    }

    init {
        this.cdiSupplier = Objects.requireNonNull(cdiSupplier)
    }
}