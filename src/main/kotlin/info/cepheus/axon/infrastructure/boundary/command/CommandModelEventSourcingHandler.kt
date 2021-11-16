package info.cepheus.axon.infrastructure.boundary.command

import org.axonframework.eventsourcing.EventSourcingHandler
import kotlin.reflect.KClass

/**
 * Marks a EventHandler inside an eventsourcing-capable Aggregate (Domain Driven Design).
 *
 *
 * This annotation is based on axon framework's "@EventSourcingHandler".
 *
 * @see EventSourcingHandler
 */
@MustBeDocumented
@EventSourcingHandler
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.ANNOTATION_CLASS)
annotation class CommandModelEventSourcingHandler(
        /**
         * The type of event this method handles. If specified, this handler will only be invoked for message that have a
         * payload assignable to the given payload type.
         *
         *
         * Optional. If unspecified, the first parameter of the method defines the type of supported event.
         *
         * @return The type of the event this method handles.
         */
        val payloadType: KClass<*> = Any::class)