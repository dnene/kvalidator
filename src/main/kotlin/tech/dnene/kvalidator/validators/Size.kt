package tech.dnene.kvalidator.validators

import tech.dnene.kvalidator.*
import kotlin.reflect.KProperty1

fun <T> KProperty1<T, String>.range(minLength: Int, maxLength: Int, message: String? = null): Validating<T, Invalidity> =
        { t: T ->
            this.get(t)?.let {str ->
                if (str.length < minLength)
                    listOf(TooSmall(message ?: "${this.name} length too small", this, minLength, str.length))
                else if (str.length > maxLength)
                    listOf(TooLarge(message ?: "${this.name} length too large", this, maxLength, str.length))
                else
                    EMPTY_LIST
            }
        }

class SizeValidator<in T>(val c: Size, val prop: KProperty1<in T, String>): Validator<Size, T> {
    override fun toString() = "SizeValidator(${prop}=>${c})"
    override val defaultMessage = "size-not-in-desired-range"
    val message = if (c.message.isBlank()) defaultMessage else c.message
    override fun validate(t: T): List<Invalidity> = prop.range(c.min, c.max, message)(t)
}

@Target(AnnotationTarget.PROPERTY)
@Constraint(validatedBy = SizeValidator::class)
annotation class Size(val min: Int = 0, val max: Int = Int.MAX_VALUE, val message: String = "")
