package info.cepheus.axon.infrastructure

/**
 * Provides the (custom/local) revision of the event for upcasting purposes.
 */
@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class EventRevision(
        /**
         * The revision identifier for this object.
         */
        val value: String
)