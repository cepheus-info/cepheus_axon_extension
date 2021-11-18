package info.cepheus.axon.infrastructure.adapter

import info.cepheus.axon.infrastructure.boundary.query.QueryUpdateEmitterService
import org.axonframework.queryhandling.QueryUpdateEmitter
import java.util.function.Predicate

open class QueryUpdateEmitterAdapter(private val queryUpdateEmitter: QueryUpdateEmitter) : QueryUpdateEmitterService {
    override fun <Q, U> emit(queryType: Class<Q>?, filter: Predicate<in Q>?, update: U) {
        queryUpdateEmitter.emit(queryType, filter, update)
    }

    override fun toString(): String {
        return "QueryUpdateEmitterAdapter [queryUpdateEmitter=$queryUpdateEmitter]"
    }
}