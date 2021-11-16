package info.cepheus.axon.infrastructure

import java.lang.reflect.AnnotatedElement
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.logging.Logger
import java.util.stream.Collectors
import java.util.stream.Stream

internal class RegisteredAnnotatedTypes(allTypes: Collection<Class<*>>?) {
    private val allTypes: MutableCollection<Class<*>> = HashSet()
    fun subtypeOf(annotationClass: Class<*>): Stream<Class<*>> {
        return logged(allTypes.stream().filter { cls: Class<*>? -> annotationClass.isAssignableFrom(cls) }.distinct(), annotationClass)
    }

    fun annotatedWith(annotationClass: Class<out Annotation>): Stream<Class<*>> {
        return logged(allTypes.stream().filter { type: Class<*> -> isAnnotationPresent(annotationClass, type) }.distinct(), annotationClass)
    }

    @SafeVarargs
    fun annotatedWithAnyOf(vararg annotationClasses: Class<out Annotation?>): Stream<Class<*>> {
        return Stream.of(*annotationClasses).flatMap { annotationClass: Class<out Annotation> -> annotatedWith(annotationClass) }.distinct()
    }

    fun forEachAnnotatedType(annotationClass: Class<out Annotation>, type: Consumer<in Class<*>>?) {
        annotatedWith(annotationClass).forEach(type)
    }

    fun without(annotationClass: Class<out Annotation>): Predicate<in Class<*>> {
        return Predicate { type: Class<*> -> !annotatedWith(annotationClass).anyMatch { obj: Class<*>? -> type.equals(obj) } }
    }

    override fun toString(): String {
        return "RegisteredAnnotatedTypes [allTypes=$allTypes]"
    }

    companion object {
        private val LOGGER = Logger.getLogger(RegisteredAnnotatedTypes::class.java.name)
        private const val MAX_META_ANNOTATION_RECURSION_DEPTH = 3
        fun ofStream(allTypes: Stream<Class<*>>): RegisteredAnnotatedTypes {
            return ofClasses(allTypes.collect(Collectors.toList()))
        }

        fun ofClasses(allTypes: Collection<Class<*>>?): RegisteredAnnotatedTypes {
            return RegisteredAnnotatedTypes(allTypes)
        }

        private fun isAnnotationPresent(annotationClass: Class<out Annotation>, type: Class<*>): Boolean {
            if (isAnnotationPresent(type, annotationClass, 0)) {
                return true
            }
            for (method in type.declaredMethods) {
                if (isAnnotationPresent(method, annotationClass, 0)) {
                    return true
                }
            }
            for (field in type.declaredFields) {
                if (isAnnotationPresent(field, annotationClass, 0)) {
                    return true
                }
            }
            return false
        }

        private fun isAnnotationPresent(element: AnnotatedElement, annotationType: Class<out Annotation>,
                                        recursion: Int): Boolean {
            if (element.isAnnotationPresent(annotationType)) {
                return true
            }
            if (recursion > MAX_META_ANNOTATION_RECURSION_DEPTH) {
                return false
            }
            for (annotation in element.annotations) {
                if (annotation.annotationClass.java.name.startsWith("java.lang.annotation")) {
                    continue
                }
                if (isAnnotationPresent(annotation.annotationClass.java, annotationType, recursion + 1)) {
                    return true
                }
            }
            return false
        }

        private fun logged(stream: Stream<Class<*>>, queriedType: Class<*>): Stream<Class<*>> {
            return stream.peek { type: Class<*> -> LOGGER.fine { "Found " + type.name + " as " + queriedType } }
        }
    }

    init {
        this.allTypes.addAll(allTypes!!)
    }
}