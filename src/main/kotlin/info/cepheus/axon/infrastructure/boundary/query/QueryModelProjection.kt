package info.cepheus.axon.infrastructure.boundary.query

import org.axonframework.config.ProcessingGroup
import java.lang.annotation.Inherited
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Stream
import javax.enterprise.context.Dependent
import javax.enterprise.inject.Stereotype

/**
 * Hint for the Configuration API that the annotated Event Handler object should be assigned to an Event Processor with the specified name.
 *
 *
 * This annotation is based on axon framework's "@ProcessingGroup".
 *
 * @see ProcessingGroup
 */
// Note: For meta-annotations (here for @ProcessingGroup) with value() parameter,
// the parameter needs to be renamed to the simple class name of the original annotation.
// Thus, "value" cannot be used here. It needs to be defined as processingGroup().
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD,
    AnnotationTarget.ANNOTATION_CLASS
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Inherited
@Stereotype
@Dependent
@MustBeDocumented
@ProcessingGroup("")
annotation class QueryModelProjection(
    /**
     * The name of the Event Processor to assign the annotated Event Handler object to.
     *
     * @return the name of the Event Processor to assign objects of this type to
     */
    // Note: processingGroup can be optional, as soon as this issue is fixed:
    // https://github.com/AxonFramework/AxonFramework/issues/940
    val processingGroup: String,
    /**
     * Specifies the processor name that will be assigned to the [.processingGroup].<br></br>
     * The processor name represents a processing configuration.
     *
     *
     * Defaults to [QueryProcessor.TRACKING]
     *
     * @return [QueryProcessor]
     */
    val processor: QueryProcessor = QueryProcessor.TRACKING
) {
    /**
     * Gets the assignment between [io.github.joht.showcase.quarkuseventsourcing.messaging.query.boundary.QueryModelProjection.processingGroup] and [io.github.joht.showcase.quarkuseventsourcing.messaging.query.boundary.QueryModelProjection.processor] for the given
     * [Class] type or [Optional.empty], if there is no Annotation present or there is no assignment (default settings).
     *
     * @author JohT
     */
    class ProcessorAssignment : Function<Class<*>, Optional<QueryModelProjection>> {

        override fun apply(type: Class<*>): Optional<QueryModelProjection> {
            if (!type.isAnnotationPresent(QueryModelProjection::class.java)) {
                return Optional.empty()
            }
            val annotation = type.getAnnotation(QueryModelProjection::class.java)
            if (annotation.processor.isDefault) {
                return Optional.empty()
            }
            if (annotation.processingGroup.isEmpty()) {
                val message =
                    "Assigning the non default processor %s to %s requires an explicitly defined, non empty processing group."
                throw IllegalStateException(
                    java.lang.String.format(
                        message,
                        annotation.processor.toString(),
                        type.simpleName
                    )
                )
            }
            return Optional.of(annotation)
        }

        companion object {
            fun forType(type: Class<*>, consumer: Consumer<QueryModelProjection>) {
                Stream.of(type)
                    .map(ProcessorAssignment()::apply)
                    .forEach { p -> p.ifPresent(consumer) }
            }
        }
    }
}