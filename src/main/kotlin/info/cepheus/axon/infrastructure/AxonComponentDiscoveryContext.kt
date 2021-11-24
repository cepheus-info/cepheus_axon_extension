package info.cepheus.axon.infrastructure

import org.axonframework.config.AggregateConfigurer
import org.axonframework.config.Configurer
import java.util.function.BiConsumer
import java.util.function.Consumer


/**
 * Provides the [Configurer] to attach all discovered components to the axon configuration.
 *
 *
 * Some of the components can be configured more detailed by their own configurations. <br></br>
 * [Consumer]s may optionally be specified for those. <br></br>
 * They will get called for every discovered component, that provides detailed configuration. <br></br>
 * The [Consumer]s are preset to do nothing, if not specified otherwise.
 *
 * @author JohT
 */
class AxonComponentDiscoveryContext private constructor() {
    lateinit var configurer: Configurer

    var onAggregateConfiguration: Consumer<AggregateConfigurer<*>> = NO_OPERATION_AGGREGATE_CONFIG

    private val onDiscoveredAnnotation: MutableMap<Class<out Annotation?>, Consumer<Class<*>>> = HashMap()

    fun forEachDiscoveredAnnotation(action: BiConsumer<in Class<out Annotation?>, in Consumer<Class<*>>>?) {
        onDiscoveredAnnotation.forEach(action!!)
    }

    fun addComponentDiscovery(componentDiscovery: AxonComponentDiscovery) {
        componentDiscovery.addDiscoveredComponentsTo(this)
    }

    override fun toString(): String {
        return ("AxonComponentDiscoveryContext [configurer=" + configurer + ", onAggregateConfiguration=" + onAggregateConfiguration
                + ", onDiscoveredAnnotation=" + onDiscoveredAnnotation + "]")
    }

    class Builder {
        private var context: AxonComponentDiscoveryContext? = AxonComponentDiscoveryContext()

        /**
         * Takes all settings of the given template [AxonComponentDiscoveryContext] <br></br>
         * enabling to change single settings an use all others of the template.
         *
         * @param template [AxonComponentDiscoveryContext]
         * @return [Builder]
         */
        fun template(template: AxonComponentDiscoveryContext): Builder {
            configurer(template.configurer)
            onAggregateConfiguration(template.onAggregateConfiguration)
            template.onDiscoveredAnnotation.forEach { (annotationType: Class<out Annotation?>, typeConsumer: Consumer<Class<*>>) -> onDiscoveredType(annotationType, typeConsumer) }
            return this
        }

        /**
         * Sets the mandatory [Configurer]. May not be null
         *
         * @param configurer
         * @return
         */
        fun configurer(configurer: Configurer?): Builder {
            context!!.configurer = configurer!!
            return this
        }

        /**
         * The given [Consumer] will be called for every aggregate that is
         * discovered. This enables to specify further configurations for aggregates
         * provided by the [AggregateConfigurer].
         *
         *
         * Defaults to a "no operation" [Consumer] doing nothing when getting
         * called.
         *
         * @param configurationConsumer [Consumer] of [AggregateConfigurer]
         * @return [Builder]
         */
        fun onAggregateConfiguration(configurationConsumer: Consumer<AggregateConfigurer<*>>): Builder {
            context!!.onAggregateConfiguration = getOrDefault(configurationConsumer, NO_OPERATION_AGGREGATE_CONFIG)
            return this
        }

        /**
         * The given [Consumer] will be called for every type that is discovered. <br></br>
         * This enables to specify custom actions that need to be done for all discovered types annotated with the given annotation.
         *
         *
         * Defaults to a "no operation" [Consumer] doing nothing when getting called.
         *
         * @param annotationType [Class] that is a sub type of [Annotation]
         * @param typeConsumer [Consumer] of discovered [Class]
         * @return [Builder]
         */
        fun onDiscoveredType(annotationType: Class<out Annotation?>, typeConsumer: Consumer<Class<*>>): Builder {
            context!!.onDiscoveredAnnotation[annotationType] = getOrDefault(typeConsumer, NO_OPERATION_EVENT_HANDLER)
            return this
        }

        /**
         * Completes the build of [AxonComponentDiscoveryContext]. May only be used once per builder instance.
         *
         * @return [AxonComponentDiscoveryContext]
         */
        fun build(): AxonComponentDiscoveryContext? {
            notNull<Configurer?>(context!!.configurer, "configurer may not be null")
            return try {
                context
            } finally {
                context = null
            }
        }

        override fun toString(): String {
            return "Builder [context=$context]"
        }

        companion object {
            private fun <T> getOrDefault(value: T?, defaultValue: T): T {
                return value ?: defaultValue
            }

            private fun <T> notNull(value: T?, message: String): T {
                requireNotNull(value) { message }
                return value
            }
        }
    }

    companion object {
        private val NO_OPERATION_AGGREGATE_CONFIG = Consumer { _: AggregateConfigurer<*> -> }
        private val NO_OPERATION_EVENT_HANDLER = Consumer { _: Class<*> -> }
        fun builder(): Builder {
            return Builder()
        }
    }
}