package io.cqser.cqs.command

import io.cqser.cqs.utils.ExceptionBuilder.buildException
import io.cqser.mediator.core.Event
import io.cqser.mediator.core.Mediator
import io.cqser.mediator.core.Request
import io.cqser.mediator.core.RequestHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException
import javax.validation.Validator

abstract class AbstractCreateCommandWithRelation<T>(val relationId:UUID, val toCreate: T) : Request<T>

abstract class AbstractCreateCommandHandlerWithRelation<TRelation, TRequest : AbstractCreateCommandWithRelation<TResponse>, TResponse> protected constructor(
    private val clazz: Class<TResponse>,
    private val relationJpaRepository: JpaRepository<TRelation, UUID>,
    private val jpaRepository: JpaRepository<TResponse, UUID>
) : RequestHandler<TRequest, TResponse> {

    @Autowired
    private val validator: Validator? = null
    @Autowired
    private val mediator: Mediator? = null

    @Transactional
    override fun handle(command: TRequest): TResponse {
        val toCreate = command.toCreate
        val relation = relationJpaRepository.findById(command.relationId).orElseThrow { buildException(command.relationId, clazz) }

        validate(toCreate, relation, command)
        handleCreate(toCreate, relation, command)
        save(toCreate)
        emitEvent(toCreate)

        return toCreate
    }

    protected open fun save(toCreate: TResponse) {
        jpaRepository.save(toCreate)
    }

    protected open fun validate(toCreate: TResponse, relation: TRelation, command: TRequest) {
        validate(toCreate, relation)
    }

    protected open fun validate(toCreate: TResponse, relation: TRelation) {
        Optional
            .ofNullable(getToValidate(toCreate, relation))
            .ifPresent { obj ->
                val constraintViolations = validator!!.validate(obj)
                if (constraintViolations.isNotEmpty()) {
                    throw ConstraintViolationException(constraintViolations)
                }
            }
    }

    protected open fun getToValidate(toCreate: TResponse, relation: TRelation): Any? {
        return null
    }

    protected open fun handleCreate(toCreate: TResponse, relation: TRelation, command: TRequest) {
        handleCreate(toCreate, relation)
    }

    protected open fun handleCreate(toCreate: TResponse, relation: TRelation) {

    }

    private fun emitEvent(toCreate: TResponse) {
        Optional
            .ofNullable(eventToEmit(toCreate))
            .ifPresent { event -> mediator!!.emit(event) }
    }

    protected open fun eventToEmit(toCreate: TResponse): Event? {
        return null
    }
}