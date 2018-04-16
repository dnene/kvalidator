package tech.dnene.kvalidator.validators

import tech.dnene.kvalidator.*
import kotlin.reflect.KProperty1

class MinValidator<in T>(val c: Min, val prop: KProperty1<in T, Int>): Validator<Min, T> {
    override fun toString() = "MinValidator(${prop}=>${c})"
    override val defaultMessage = "value-less-than-desired-minimum"
    val message = if (c.message.isBlank()) defaultMessage else c.message
    override fun validate(a: T): List<Invalidity> =
            prop.get(a).let { len ->
                if (len < c.min) listOf(TooSmall(message, prop, c.min, len))
                else EMPTY_LIST
            }
}

@Target(AnnotationTarget.PROPERTY)
@Constraint(validatedBy = MinValidator::class)
annotation class Min(val min: Int = 0, val message: String = "")

