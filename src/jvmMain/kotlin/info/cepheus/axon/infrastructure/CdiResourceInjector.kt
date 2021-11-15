package info.cepheus.axon.infrastructure

import org.axonframework.modelling.saga.ResourceInjector
import java.util.function.Function
import javax.enterprise.context.spi.CreationalContext
import javax.enterprise.inject.spi.BeanManager
import javax.enterprise.inject.spi.CDI


class CdiResourceInjector private constructor(private val beanManager: BeanManager) : ResourceInjector {
    /**
     * {@inheritDoc}
     */
    override fun injectResources(resource: Any?) {
        if (resource != null) {
            injectResourcesUsingCdi(resource)
        }
    }

    private fun <T : Any> injectResourcesUsingCdi(resource: T) {
        val type = resource.javaClass
        val annotatedType = beanManager.createAnnotatedType(type)
        val targetFactory = beanManager.getInjectionTargetFactory(annotatedType)
        val target = targetFactory.createInjectionTarget(null)
        val creationalContext: CreationalContext<T> = beanManager.createCreationalContext(null)
        target.inject(resource, creationalContext)
        target.postConstruct(resource)
    }

    companion object {
        fun standard(): ResourceInjector {
            return CdiResourceInjector(CDI.current().beanManager)
        }

        fun <P> useBeanManager(beanManager: BeanManager): Function<P, ResourceInjector> {
            return Function { CdiResourceInjector(beanManager) }
        }
    }
}