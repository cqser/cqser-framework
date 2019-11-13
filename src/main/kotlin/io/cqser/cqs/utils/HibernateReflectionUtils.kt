package io.cqser.cqs.utils

import org.springframework.util.ReflectionUtils
import java.lang.reflect.Field

object HibernateReflectionUtils {

    fun <T:Any> getFields(destination: T, vararg fieldsToIgnore: String): List<Field> {
        return destination!!.javaClass.declaredFields
            .filter { field -> !fieldsToIgnore.contains(field.name) }
    }

    fun <T:Any> getProperty(bean: T, propertyName: String): Any {
        val propertyField = ReflectionUtils.findField(bean!!.javaClass, propertyName)
        return getProperty(bean, propertyField);
    }

    fun <T:Any> getProperty(bean: T, propertyField: Field): Any {
        propertyField!!.isAccessible = true
        return propertyField.get(bean)
    }

    fun <T:Any> setProperty(bean: T, propertyName: String, value: Any?) {
        if (value != null && value == getProperty(bean, propertyName)) return
        val propertyField = ReflectionUtils.findField(bean!!.javaClass, propertyName)
        setProperty<T>(propertyField, bean, value)
    }

    fun <T:Any> setProperty(propertyField: Field, bean: T, value: Any?) {
        if (value != null && value == getProperty(bean, propertyField)) return
        propertyField!!.isAccessible = true
        propertyField.set(bean, value)
    }

}
