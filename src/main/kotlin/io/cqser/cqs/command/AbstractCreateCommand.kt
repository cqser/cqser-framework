package io.cqser.cqs.command

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

abstract class AbstractCreateCommand<T>(val toCreate: T) : Request<T>

abstract class AbstractCreateCommandHandler<TRequest : AbstractCreateCommand<TResponse>, TResponse> protected constructor(
    private val clazz: Class<TResponse>,
    private val jpaRepository: JpaRepository<TResponse, UUID>
) : RequestHandler<TRequest, TResponse> {

    @Autowired
    private val validator: Validator? = null
    @Autowired
    private val mediator: Mediator? = null

    @Transactional
    override fun handle(command: TRequest): TResponse {
        val toCreate = command.toCreate

        validate(toCreate)
        handleCreate(toCreate)
        save(toCreate)
        emitEvent(toCreate)

        return toCreate
    }

    protected open fun save(toCreate: TResponse) {
        jpaRepository.save(toCreate)
    }

    protected open fun validate(toCreate: TResponse) {
        Optional
            .ofNullable(getToValidate(toCreate))
            .ifPresent { obj ->
                val constraintViolations = validator!!.validate(obj)
                if (constraintViolations.isNotEmpty()) {
                    throw ConstraintViolationException(constraintViolations)
                }
            }
    }

    protected open fun getToValidate(toCreate: TResponse): Any? {
        return null
    }

    protected open fun handleCreate(toCreate: TResponse) {

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