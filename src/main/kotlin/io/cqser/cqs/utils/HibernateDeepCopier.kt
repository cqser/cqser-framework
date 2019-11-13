package io.cqser.cqs.utils

import io.cqser.cqs.utils.HibernateReflectionUtils.getFields
import io.cqser.cqs.utils.HibernateReflectionUtils.getProperty
import io.cqser.cqs.utils.HibernateReflectionUtils.setProperty
import org.springframework.beans.BeanUtils

import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.reflect.Field
import java.util.UUID

object HibernateDeepCopier {

    private val isOneToManyCollection = { field : Field -> field.isAnnotationPresent(OneToMany::class.java) }
    private val isOneToOneCollection = { field : Field-> field.isAnnotationPresent(OneToOne::class.java) }

    @JvmStatic
    fun <T:Any> deepCopy(destination: T, original: T, vararg fieldsToIgnore: String) {
        copySimpleProperties<T>(destination, original, *fieldsToIgnore)
        updateOneToManyCollections(destination, original, *fieldsToIgnore)
        updateOneToOneObjects(destination, original, *fieldsToIgnore)
    }

    private fun <T:Any> updateOneToManyCollections(destination: T, original: T, vararg fieldsToIgnore: String) {
        getFields(destination, *fieldsToIgnore)
            .filter(isOneToManyCollection)
            .forEach { field -> updateOneToManyCollection<T, Any>(field, destination, original) }
    }

    private fun <T:Any> updateOneToOneObjects(destination: T, original: T, vararg fieldsToIgnore: String) {
        getFields(destination, *fieldsToIgnore)
            .filter(isOneToOneCollection)
            .forEach { field -> updateOneToOneObject(field, destination, original) }
    }

    private fun <T:Any, U:Any> updateOneToManyCollection(field: Field, destination: T, original: T) {
        field.isAccessible = true
        val destinationCollection = field.get(destination) as MutableCollection<U>
        val originalCollection = field.get(original) as Collection<U>

        updateValueObjects(destinationCollection, originalCollection)
        removeItemsInDestinationWhichAreNotInOriginal(destinationCollection, originalCollection)
        updateItemsInDestinationUsingOriginal(destinationCollection, originalCollection)
        addItemsInDestinationFromOriginal(destinationCollection, originalCollection, destination)
    }

    private fun <T:Any> updateOneToOneObject(field: Field, destination: T, original: T) {
        field.isAccessible = true
        val destinationObject = field.get(destination)
        val originalObject = field.get(original)
        copySimpleProperties(destinationObject, originalObject)
    }

    private fun <U:Any> updateValueObjects(destinationCollection: Collection<U>, originalCollection: Collection<U>) {
        val destinationMap = destinationCollection
            .filter { isValueObject(it) }
            .associateBy ({ getValueObjectValue(it)}, { it })

        originalCollection
            .filter { isValueObject(it) }
            .forEach { obj ->
                val item = destinationMap.getOrDefault(getValueObjectValue(obj), obj)
                setIdIfNotSet(obj, getId(item))
            }
    }

    private fun <T:Any> removeItemsInDestinationWhichAreNotInOriginal(destinationCollection: MutableCollection<T>, originalCollection: Collection<T>) {
        destinationCollection.removeIf { destinationItem ->
            val toRemove = originalCollection.none { originalItem -> getId(destinationItem) != null && getId(destinationItem) == getId(originalItem) }
            if (toRemove) {
                val joinColumnField = joinColumnField(destinationItem!!)
                joinColumnField
                if (joinColumnField != null) {
                    setProperty(destinationItem, joinColumnField, null)
                }
            }
            toRemove
        }
    }

    private fun <T:Any> updateItemsInDestinationUsingOriginal(destinationCollection: Collection<T>, originalCollection: Collection<T>) {
        destinationCollection.forEach { destination ->
            val original = originalCollection.first { originalItem -> getId(destination) == getId(originalItem) }
            copySimpleProperties(destination, original, joinColumnField(destination!!))
        }
    }

    private fun <T:Any, U:Any> addItemsInDestinationFromOriginal(destinationCollection: MutableCollection<U>, originalCollection: Collection<U>, destination: T) {
        originalCollection
            .filter { item -> getId(item) == null }
            .forEach { item -> destinationCollection.add(updateFieldWithJoinColumn(item, destination)) }
    }

    private fun <T:Any> copySimpleProperties(destination: T?, original: T?, vararg fieldsToIgnore: String?) {
        if (destination == null && original == null) return
        if(fieldsToIgnore.filterNotNull().isEmpty()) {
            BeanUtils.copyProperties(original!!, destination!!)
        } else {
            BeanUtils.copyProperties(original!!, destination!!, *fieldsToIgnore)
        }
    }

    private fun <U:Any, T:Any> updateFieldWithJoinColumn(item: U, destination: T): U {
        val joinColumnField = getFields(item).first { field -> field.isAnnotationPresent(JoinColumn::class.java) }
        if (joinColumnField != null) {
            setProperty(joinColumnField, item, destination)
        }
        return item
    }

    private fun joinColumnField(destination: Any): String? {
        return getFields(destination)
            .filter { field -> field.isAnnotationPresent(JoinColumn::class.java) }
            .map { field -> field.name }
            .first()
    }

    private fun <T:Any> getValueObjectValue(destination: T): Any {
        val valueObjectAnnotation = destination!!.javaClass.getAnnotation(ValueObject::class.java)
        return getProperty(destination, valueObjectAnnotation.value)
    }

    private fun <T:Any> isValueObject(destination: T): Boolean {
        return destination!!.javaClass.getAnnotation(ValueObject::class.java) != null
    }

    private fun <T:Any> getId(pojo: T): UUID? {
        return getProperty(pojo, "id") as UUID
    }

    private fun <T:Any> setIdIfNotSet(bean: T, value: Any?) {
        if (value == null) return
        if (getId(bean) != null) return
        setProperty(bean, "id", value)
    }

    @Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    annotation class ValueObject(val value: String)

}

