package info.cepheus.axon.infrastructure.boundary.command

/**
 * Provides the identifier of the aggregate that a command targets.
 *
 *
 * This annotation is based on axon framework's "AggregateIdentifier".
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class CommandTargetAggregateIdentifier 