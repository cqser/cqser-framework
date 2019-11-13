package io.cqser.cqs.query

import io.cqser.mediator.core.Request
import io.cqser.mediator.core.RequestHandler
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.transaction.annotation.Transactional

abstract class AbstractPagedListQuery<TResponse>(val specification: Specification<TResponse>, val pageable: Pageable) : Request<Page<TResponse>>

abstract class AbstractPagedListQueryHandler<TRequest : AbstractPagedListQuery<TResponse>, TResponse> protected constructor(
    private val clazz: Class<TResponse>,
    protected open val jpaSpecificationExecutor: JpaSpecificationExecutor<TResponse>
) : RequestHandler<TRequest, Page<TResponse>> {

    @Transactional
    override fun handle(query: TRequest): Page<TResponse> {
        return jpaSpecificationExecutor.findAll(query.specification, query.pageable)
    }
}


