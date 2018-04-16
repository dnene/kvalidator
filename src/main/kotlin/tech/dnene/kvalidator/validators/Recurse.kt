package tech.dnene.kvalidator.validators

import tech.dnene.kvalidator.Constraint
import tech.dnene.kvalidator.DataValidator
import tech.dnene.kvalidator.Invalidity
import tech.dnene.kvalidator.Validator
import kotlin.reflect.KProperty1

class RecurseValidator<in T>(val c: Recurse, val prop: KProperty1<in T, Iterable<*>>): Validator<Recurse, T> {
    override fun toString() = "PatternValidator(${prop}=>${c})"
    override val defaultMessage = "nested-collection-is-not-valid"
    override fun validate(a: T): List<Invalidity> =
            prop.get(a).let { collection ->
                DataValidator.validateRecurse(collection)
            }
}

@Target(AnnotationTarget.PROPERTY)
@Constraint(validatedBy = RecurseValidator::class)
annotation class Recurse() {

}
