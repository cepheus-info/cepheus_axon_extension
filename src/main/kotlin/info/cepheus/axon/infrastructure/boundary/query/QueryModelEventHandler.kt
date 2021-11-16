package info.cepheus.axon.infrastructure.boundary.query

import org.axonframework.eventhandling.EventHandler
import kotlin.reflect.KClass

/**
 * Annotation to be placed on methods that can handle events.
 *
 *
 * This annotation is based on axon framework's "@EventHandler".
 */
@MustBeDocumented
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.ANNOTATION_CLASS)
@EventHandler
annotation class QueryModelEventHandler(
        /**
         * The type of event this method handles. This handler will only be considered
         * for invocation if the event message's payload is assignable to this type.
         *
         *
         * Optional. If unspecified, the first parameter of the method defines the type
         * of supported event.
         *
         * @return The type of the event this method handles.
         */
        val payloadType: KClass<*> = Any::class)