package info.cepheus.axon.infrastructure

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.config.AggregateConfigurer
import org.axonframework.config.Configuration
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.modelling.command.AggregateRoot
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.serialization.upcasting.event.EventUpcaster
import java.util.function.Function
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Typed
import javax.enterprise.inject.spi.Bean
import javax.enterprise.inject.spi.BeanManager
import javax.enterprise.util.AnnotationLiteral
import javax.inject.Inject

/**
 * Discovers all components for axon using CDI's [BeanManager.getBeans].
 *
 * @author JohT
 */
@ApplicationScoped
class AxonComponentDiscovery {
    @Inject
    lateinit var beanManager: BeanManager

    /**
     * Attaches all discovered components to the given [AxonComponentDiscoveryContext.getConfigurer].
     *
     * @param context [AxonComponentDiscoveryContext]
     */
    fun addDiscoveredComponentsTo(context: AxonComponentDiscoveryContext) {
        val beanTypes: RegisteredAnnotatedTypes = beanTypes
        context.forEachDiscoveredAnnotation(beanTypes::forEachAnnotatedType)
        registerAggregates(context, beanTypes)
        registerEventHandlers(context, beanTypes)
        registerEventUpcasters(context, beanTypes)
        registerCommandHandlers(context, beanTypes)
        registerQueryHandlers(context, beanTypes)
        registerSagas(context, beanTypes)
        registerResourceInjector(context)
    }

    private fun registerAggregates(context: AxonComponentDiscoveryContext, beanTypes: RegisteredAnnotatedTypes) {
        beanTypes.annotatedWith(AggregateRoot::class.java)
                .map { aggregateType: Class<*>? -> AggregateConfigurer.defaultConfiguration(aggregateType) }.forEach { configurer ->
                    context.onAggregateConfiguration.accept(configurer)
                    context.configurer.configureAggregate(configurer)
                }
    }

    private fun registerEventHandlers(context: AxonComponentDiscoveryContext, beanTypes: RegisteredAnnotatedTypes) {
        beanTypes.annotatedWithAnyOf(ProcessingGroup::class.java, EventHandler::class.java)
                .map { typeToLookUp: Class<*> -> lookedUp(typeToLookUp) }
                .forEach(context.configurer.eventProcessing()::registerEventHandler)
    }

    private fun registerEventUpcasters(context: AxonComponentDiscoveryContext, beanTypes: RegisteredAnnotatedTypes) {
        beanTypes.subtypeOf(EventUpcaster::class.java)
                .map { typeToLookUp: Class<*> -> lookedUpEventUpcaster(typeToLookUp) }
                .forEach(context.configurer::registerEventUpcaster)
    }

    private fun registerCommandHandlers(context: AxonComponentDiscoveryContext, beanTypes: RegisteredAnnotatedTypes) {
        beanTypes.annotatedWith(CommandHandler::class.java)
                .filter(beanTypes.without(AggregateRoot::class.java))
                .map { typeToLookUp: Class<*> -> lookedUp(typeToLookUp) }
                .forEach(context.configurer::registerCommandHandler)
    }

    private fun registerQueryHandlers(context: AxonComponentDiscoveryContext, beanTypes: RegisteredAnnotatedTypes) {
        beanTypes.annotatedWith(QueryHandler::class.java)
                .map { typeToLookUp: Class<*> -> lookedUp(typeToLookUp) }
                .forEach(context.configurer::registerQueryHandler)
    }

    private fun registerSagas(context: AxonComponentDiscoveryContext, beanTypes: RegisteredAnnotatedTypes) {
        beanTypes.annotatedWith(SagaEventHandler::class.java)
                .forEach { type -> context.configurer.eventProcessing().registerSaga(type) }
    }

    private fun registerResourceInjector(context: AxonComponentDiscoveryContext) {
        context.configurer.configureResourceInjector(CdiResourceInjector.useBeanManager(beanManager))
    }

    private fun lookedUp(typeToLookUp: Class<*>): Function<Configuration?, Any?> {
        return Function { lookup<Any>(typeToLookUp) }
    }

    private fun lookedUpEventUpcaster(typeToLookUp: Class<*>): Function<Configuration?, EventUpcaster?> {
        return Function { lookup<Any>(typeToLookUp) as EventUpcaster }
    }

    private fun <U> lookup(type: Class<*>, vararg qualifiers: Annotation): Any {
        val bean = beanManager.getBeans(type, *qualifiers).iterator().next()
        val ctx = beanManager.createCreationalContext(bean)
        return beanManager.getReference(bean, type, ctx)
    }

    private val beanTypes: RegisteredAnnotatedTypes
        get() {
            val beans = beanManager.getBeans(Any::class.java, AnnotationLiteralAnyKt.ANY)
            return RegisteredAnnotatedTypes.ofStream(beans.stream().filter { bean: Bean<*> -> isBeanWithAtLeastOneType(bean) }.map(Function { obj: Bean<*> -> obj.beanClass }))
        }

    private fun isBeanWithAtLeastOneType(bean: Bean<*>): Boolean {
        val annotation = bean.beanClass.getAnnotation(Typed::class.java)
        return annotation?.value?.isNotEmpty() ?: true
    }

    class AnnotationLiteralAny : AnnotationLiteral<javax.enterprise.inject.Any>()

    private object AnnotationLiteralAnyKt : AnnotationLiteral<javax.enterprise.inject.Any?>() {
        private const val serialVersionUID = 1L
        val ANY: AnnotationLiteral<javax.enterprise.inject.Any> = AnnotationLiteralAny()
    }
}