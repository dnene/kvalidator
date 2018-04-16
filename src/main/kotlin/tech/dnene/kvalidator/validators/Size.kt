package tech.dnene.kvalidator.validators

import tech.dnene.kvalidator.*
import kotlin.reflect.KProperty1

class SizeValidator<in T>(val c: Size, val prop: KProperty1<in T, String>): Validator<Size, T> {
    override fun toString() = "SizeValidator(${prop}=>${c})"
    override val defaultMessage = "size-not-in-desired-range"
    val message = if (c.message.isBlank()) defaultMessage else c.message
    override fun validate(a: T): List<Invalidity> =
            prop.get(a).length.let { len ->
                if (len < c.min) listOf(TooSmall(message, prop, c.min, len))
                else if (len > c.max) listOf(TooLarge(message, prop, c.max, len))
                else EMPTY_LIST
            }
}

@Target(AnnotationTarget.PROPERTY)
@Constraint(validatedBy = SizeValidator::class)
annotation class Size(val min: Int = 0, val max: Int = Int.MAX_VALUE, val message: String = "")
