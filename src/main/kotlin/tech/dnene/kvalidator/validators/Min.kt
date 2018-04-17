package tech.dnene.kvalidator.validators

import tech.dnene.kvalidator.*
import kotlin.reflect.KProperty1

fun <T> KProperty1<T, Int>.min(min: Int, message: String? = null): Validating<T, Invalidity> =
        { t: T ->
            this.get(t)?.let {
                if (it < min)
                    listOf(TooSmall(message ?: "${this.name} too small", this,min, it))
                else
                    listOf()
            }
        }


class MinValidator<in T>(val c: Min, val prop: KProperty1<in T, Int>): Validator<Min, T> {
    override fun toString() = "MinValidator(${prop}=>${c})"
    override val defaultMessage = "value-less-than-desired-minimum"
    val message = if (c.message.isBlank()) defaultMessage else c.message
    override fun validate(t: T): List<Invalidity> = prop.min(c.min,message)(t)
}

@Target(AnnotationTarget.PROPERTY)
@Constraint(validatedBy = MinValidator::class)
annotation class Min(val min: Int = 0, val message: String = "")

