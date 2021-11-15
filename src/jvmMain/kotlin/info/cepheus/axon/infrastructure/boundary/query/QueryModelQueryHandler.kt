package info.cepheus.axon.infrastructure.boundary.query

import org.axonframework.queryhandling.QueryHandler

/**
 * Mark a service method as being a QueryHandler.
 *
 *
 * The annotated method's first parameter is the query handled by that method.
 *
 *
 * This annotation is based on axon framework's "@QueryHandler".
 */
@MustBeDocumented
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.ANNOTATION_CLASS)
@QueryHandler
annotation class QueryModelQueryHandler(
        /**
         * The name of the Query this handler listens to.
         *
         * @return The query name
         */
        val queryName: String = "")