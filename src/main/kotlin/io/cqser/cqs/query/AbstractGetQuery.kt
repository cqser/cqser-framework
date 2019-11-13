package io.cqser.cqs.query

import io.cqser.cqs.utils.ExceptionBuilder.buildException
import io.cqser.mediator.core.Request
import io.cqser.mediator.core.RequestHandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

abstract class AbstractGetQuery<TResponse>(val id: UUID) : Request<TResponse>

abstract class AbstractGetQueryHandler<TRequest : AbstractGetQuery<TResponse>, TResponse> protected constructor(
    private val clazz: Class<TResponse>,
    private val jpaRepository: JpaRepository<TResponse, UUID>
) : RequestHandler<TRequest, TResponse> {

    @Transactional
    override fun handle(query: TRequest): TResponse {
        return jpaRepository.findById(query.id).orElseThrow { buildException(query.id, clazz) }
    }
}


