package info.cepheus.axon.infrastructure.boundary.query

import org.axonframework.eventhandling.Timestamp

/**
 * Injects the event creation time into the annotated [Instant]-typed parameter of the event handler method.
 *
 *
 * This annotation is based on axon framework's "@Timestamp".
 *
 * @see Timestamp
 */
@MustBeDocumented
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Timestamp
annotation class EventTimestampParameter 