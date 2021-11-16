package info.cepheus.axon.infrastructure.boundary.command

/**
 * Provides the expected version of the aggregate that a command targets.
 *
 *
 * This annotation is based on axon framework's "AggregateVersion".
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class CommandTargetAggregateVersion 