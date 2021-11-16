package info.cepheus.axon.infrastructure.boundary.command

import org.axonframework.modelling.command.AggregateIdentifier

/**
 * Marks an Aggregate (Domain Driven Design), that can be used for CQRS and event-souring.
 *
 *
 * This annotation is based on axon framework's "@AggregateIdentifier".
 *
 * @see AggregateIdentifier
 */
@MustBeDocumented
@AggregateIdentifier
@Target(AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class CommandModelAggregateIdentifier(
        /**
         * Get the name of the routing key property on commands that provides the
         * identifier that should be used to target the aggregate root with the
         * annotated field.
         *
         *
         * Optional. If left empty this defaults to field name.
         */
        val routingKey: String = "")