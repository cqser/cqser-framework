package io.cqser.cqs.utils

import java.lang.RuntimeException
import java.util.*
import javax.persistence.EntityNotFoundException

object ExceptionBuilder {

    private var exceptionBuilder: ((UUID, Class<*>) -> RuntimeException)? = null

    @JvmStatic
    fun setBuilder(exceptionBuilder :(UUID, Class<*>) -> RuntimeException) {
        this.exceptionBuilder = exceptionBuilder;
    }

    @JvmStatic
    fun buildException(id:UUID, clazz: Class<*>) : RuntimeException {
        return exceptionBuilder?.invoke(id, clazz) ?: EntityNotFoundException("Entity of type ${clazz.name} with id $id is not found")

    }
}