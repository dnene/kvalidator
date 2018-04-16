package tech.dnene.kvalidator.validators

import tech.dnene.kvalidator.*
import kotlin.reflect.KProperty1

class MaxValidator<in T>(val c: Max, val prop: KProperty1<in T, Int>): Validator<Max, T> {
    override fun toString() = "MaxValidator(${prop}=>${c})"
    override val defaultMessage = "value-less-than-desired-maximum"
    val message = if (c.message.isBlank()) defaultMessage else c.message
    override fun validate(a: T): List<Invalidity> =
            prop.get(a).let { len ->
                if (len > c.max) listOf(TooLarge(message, prop, c.max, len))
                else EMPTY_LIST
            }
}

@Target(AnnotationTarget.PROPERTY)
@Constraint(validatedBy = MaxValidator::class)
annotation class Max(val max: Int = 0, val message: String = "")
