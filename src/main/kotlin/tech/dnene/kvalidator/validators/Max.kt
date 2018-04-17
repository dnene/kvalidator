package tech.dnene.kvalidator.validators

import tech.dnene.kvalidator.*
import kotlin.reflect.KProperty1

fun <T> KProperty1<T, Int>.max(max: Int, message: String? = null): Validating<T, Invalidity> =
        { t: T ->
            this.get(t)?.let {
                if (it > max)
                    listOf(TooLarge(message ?: "${this.name} too long", this,max, it))
                else
                    listOf()
            }
        }

class MaxValidator<in T>(val c: Max, val prop: KProperty1<in T, Int>): Validator<Max, T> {
    override fun toString() = "MaxValidator(${prop}=>${c})"
    override val defaultMessage = "value-less-than-desired-maximum"
    val message = if (c.message.isBlank()) defaultMessage else c.message
    override fun validate(t: T): List<Invalidity> = prop.max(c.max,message)(t)
}

@Target(AnnotationTarget.PROPERTY)
@Constraint(validatedBy = MaxValidator::class)
annotation class Max(val max: Int = 0, val message: String = "")
