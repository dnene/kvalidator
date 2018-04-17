package tech.dnene.kvalidator.validators

import tech.dnene.kvalidator.*
import kotlin.reflect.KProperty1

fun <T, R> KProperty1<T, Iterable<R>>.recurse(subValidations: List<Validating<R,Invalidity>>, message: String? = null): Validating<T, Invalidity> =
        { t: T ->
            this.get(t).let { iter: Iterable<R> ->
                iter.flatMap { item: R ->
                    subValidations.flatMap { it(item) }
                }
            }
        }

class RecurseValidator<T, out R:Any>(val c: Recurse, val prop: KProperty1<T, Collection<R>>, val validators: ClassValidators): Validator<Recurse, T> {
    override fun toString() = "RecurseValidator(${prop}=>${c})"
    override val defaultMessage = "nested-collection-is-not-valid"
    override fun validate(t: T): List<Invalidity> = prop.get(t).flatMap {
      validators.validate<R>(it)
    }
}

@Target(AnnotationTarget.PROPERTY)
@Constraint(validatedBy = RecurseValidator::class)
annotation class Recurse() {

}
