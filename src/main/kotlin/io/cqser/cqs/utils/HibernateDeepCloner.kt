package io.cqser.cqs.utils

import io.cqser.cqs.utils.HibernateReflectionUtils.getFields
import io.cqser.cqs.utils.HibernateReflectionUtils.getProperty
import io.cqser.cqs.utils.HibernateReflectionUtils.setProperty
import org.hibernate.Hibernate
import org.hibernate.internal.util.SerializationHelper
import java.io.Serializable
import java.lang.reflect.Field
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne

object HibernateDeepCloner {

    @JvmStatic
    fun <T:Any> deepClone(objectToClone: T): T? {
        fullyLoad(objectToClone)
        val clonedObject = SerializationHelper.clone(objectToClone as Serializable)
        return Hibernate.unproxy(clonedObject) as T
    }

    fun <T:Any> fullyLoad(destination: T) {
        getLazyLoadedFields(destination)
            .forEach { fieldName ->
                val propertyToLoad = getProperty(destination, fieldName)
                setProperty(destination, fieldName, Hibernate.unproxy(propertyToLoad))
                fullyLoad(propertyToLoad)
            }
    }

    private fun <T:Any> getLazyLoadedFields(destination: T): List<String> {
        return getFields(destination)
            .filter { isLazyLoaded(it) }
            .map { it.name }
    }

    private fun isLazyLoaded(field: Field): Boolean {
        return (field.isAnnotationPresent(ManyToOne::class.java) && field.getAnnotation(ManyToOne::class.java).fetch == FetchType.LAZY
                || field.isAnnotationPresent(OneToMany::class.java) && field.getAnnotation(OneToMany::class.java).fetch == FetchType.LAZY
                || field.isAnnotationPresent(OneToOne::class.java) && field.getAnnotation(OneToOne::class.java).fetch == FetchType.LAZY)
    }



}
