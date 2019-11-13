package io.cqser.cqs.command

import io.cqser.cqs.utils.ExceptionBuilder.buildException
import io.cqser.cqs.utils.HibernateDeepCloner
import io.cqser.cqs.utils.HibernateDeepCopier
import io.cqser.mediator.core.Event
import io.cqser.mediator.core.Mediator
import io.cqser.mediator.core.Request
import io.cqser.mediator.core.RequestHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

abstract class AbstractUpdateCommand<out TResponse:Any>(val id: UUID, val update: TResponse) : Request<TResponse>

abstract class AbstractUpdateCommandHandler<TRequest : AbstractUpdateCommand<TResponse>, TResponse:Any> protected constructor(
    private val clazz: Class<TResponse>,
    private val jpaRepository: JpaRepository<TResponse, UUID>
) : RequestHandler<TRequest, TResponse> {

    @Transactional
    override fun handle(command: TRequest): TResponse {
        val updated = getActual(command.id)
        val original = getCloned(updated)
        val update = command.update

        validate(original, update, command)
        deepCopyProperties(updated, update)
        handleUpdate(updated, original, command)
        emitEvent(updated, original)

        return updated
    }

    @Autowired
    private val validator: Validator? = null
    @Autowired
    private val mediator: Mediator? = null

    protected open fun validate(original: TResponse?, update: TResponse, command: TRequest) {
        validate(original, update)
    }

    protected open fun validate(original: TResponse?, update: TResponse) {
        Optional
            .ofNullable(getToValidate(original, update))
            .ifPresent { obj ->
            val constraintViolations = validator!!.validate(obj)
            if (constraintViolations.isNotEmpty()) {
                throw ConstraintViolationException(constraintViolations)
            }
        }
    }

    protected open fun getToValidate(original: TResponse?, update: TResponse): Any? {
        return null
    }

    protected open fun handleUpdate(updated: TResponse, original: TResponse?, command: TRequest) {
        handleUpdate(updated, original)
    }

    protected open fun handleUpdate(updated: TResponse, original: TResponse?) {

    }

    protected open fun emitEvent(updated: TResponse, original: TResponse?) {
        Optional
            .ofNullable(eventToEmit(updated, original))
            .ifPresent { event -> mediator!!.emit(event) }
    }

    protected open fun eventToEmit(updated: TResponse, original: TResponse?): Event? {
        return null
    }

    protected open fun getActual(id: UUID): TResponse {
        return jpaRepository.findById(id).orElseThrow { buildException(id, clazz) }
    }

    protected open fun getCloned(original: TResponse): TResponse? {
        return HibernateDeepCloner.deepClone(original)
    }

    protected open fun deepCopyProperties(dest: TResponse, orig: TResponse) {
        HibernateDeepCopier.deepCopy(dest, orig, "id", "created")
    }
}



