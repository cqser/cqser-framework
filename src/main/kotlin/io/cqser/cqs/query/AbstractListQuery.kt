package io.cqser.cqs.query

import io.cqser.mediator.core.Request
import io.cqser.mediator.core.RequestHandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

abstract class AbstractListQuery<TResponse> : Request<Iterable<TResponse>>

abstract class AbstractListQueryHandler<TRequest : AbstractListQuery<TResponse>, TResponse> protected constructor(
    private val clazz: Class<TResponse>,
    protected open val jpaRepository: JpaRepository<TResponse, UUID>
) : RequestHandler<TRequest, Iterable<TResponse>> {

    @Transactional
    override fun handle(query: TRequest): Iterable<TResponse> {
        return jpaRepository.findAll()
    }
}


