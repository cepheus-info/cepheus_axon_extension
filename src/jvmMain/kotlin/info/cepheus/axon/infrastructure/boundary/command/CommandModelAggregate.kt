package info.cepheus.axon.infrastructure.boundary.command

import org.axonframework.modelling.command.AggregateRoot
import javax.enterprise.context.Dependent
import javax.enterprise.inject.Stereotype

/**
 * Marks an Aggregate (Domain Driven Design), that can be used for CQRS and event-souring. Such types will be the entry
 * point for command messages that target the aggregate.
 *
 *
 * This annotation is based on axon framework's "@AggregateRoot".
 *
 * @see AggregateRoot
 */
@MustBeDocumented
@Dependent
@Stereotype
@AggregateRoot
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class CommandModelAggregate(
        /**
         * Selects the name of the AggregateRepository bean. If left empty a new
         * repository is created. In that case the name of the repository will be based
         * on the simple name of the aggregate's class.
         */
        val repository: String = "",
        /**
         * Get the String representation of the aggregate's type. Optional. This
         * defaults to the simple name of the annotated class.
         */
        val type: String = "")