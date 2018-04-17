package tech.dnene.kvalidator.validators

import tech.dnene.kvalidator.*
import java.util.regex.Pattern
import kotlin.reflect.KProperty1

fun <T> KProperty1<T, String>.pattern(regex: String, message: String? = null): Validating<T, Invalidity> =
        Pattern.compile(regex).let {compiled ->
            { t: T ->
                this.get(t)?.let {str ->
                    if (!compiled.matcher(str).find())
                        listOf(IncorrectPattern(message ?: "${this.name} does not match expected pattern", this, regex, str))
                    else
                        listOf()
                }
            }
        }
class PatternValidator<in T>(val c: tech.dnene.kvalidator.validators.Pattern, val prop: KProperty1<in T, String>): Validator<tech.dnene.kvalidator.validators.Pattern, T> {
    override fun toString() = "PatternValidator(${prop}=>${c})"
    override val defaultMessage = "value-does-not-match-desired-pattern"
    val message = if (c.message.isBlank()) defaultMessage else c.message
    val pattern = Pattern.compile(c.pattern)
    override fun validate(t: T): List<Invalidity> = prop.pattern(c.pattern,message)(t)
}

@Target(AnnotationTarget.PROPERTY)
@Constraint(validatedBy = PatternValidator::class)
annotation class Pattern(val pattern: String=".*", val message: String = "")

