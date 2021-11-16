package info.cepheus.axon.infrastructure

import org.axonframework.serialization.RevisionResolver

/**
 * Revision Resolver implementation that checks for the presence of an [EventRevision][Revision] annotation.
 *
 *
 * The implementation is based on axons AnnotationRevisionResolver.
 *
 * @author JohT
 * @see AnnotationRevisionResolver
 */
class AnnotationEventRevisionResolver : RevisionResolver {
    /**
     * {@inheritDoc}
     */
    override fun revisionOf(payloadType: Class<*>): String {
        val revision = payloadType.getAnnotation(EventRevision::class.java)
        return revision.value
    }
}