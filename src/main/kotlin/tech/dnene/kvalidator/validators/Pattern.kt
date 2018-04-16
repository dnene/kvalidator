package tech.dnene.kvalidator.validators

import tech.dnene.kvalidator.*
import java.util.regex.Pattern
import kotlin.reflect.KProperty1

class PatternValidator<in T>(val c: tech.dnene.kvalidator.validators.Pattern, val prop: KProperty1<in T, String>): Validator<tech.dnene.kvalidator.validators.Pattern, T> {
    override fun toString() = "PatternValidator(${prop}=>${c})"
    override val defaultMessage = "value-does-not-match-desired-pattern"
    val message = if (c.message.isBlank()) defaultMessage else c.message
    val pattern = Pattern.compile(c.pattern)
    override fun validate(a: T): List<Invalidity> =
            prop.get(a).let { str ->
                if(!pattern.matcher(str).find()) listOf(IncorrectPattern(message, prop, c.pattern, str)) else EMPTY_LIST
            }
}

@Target(AnnotationTarget.PROPERTY)
@Constraint(validatedBy = PatternValidator::class)
annotation class Pattern(val pattern: String=".*", val message: String = "")

