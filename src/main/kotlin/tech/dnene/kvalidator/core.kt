package tech.dnene.kvalidator

import arrow.data.ValidatedNel
import kotlin.reflect.KClass

interface ClassValidator<T> {
    fun validate(t: T): ValidatedNel<Invalidity, T>
}

interface Validator<C, in T> {
    val defaultMessage: String
    fun validate(a: T): List<Invalidity>
}

@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class Constraint(val validatedBy: KClass<out Validator<*, *>>)

