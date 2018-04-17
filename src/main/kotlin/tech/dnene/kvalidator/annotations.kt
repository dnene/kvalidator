package tech.dnene.kvalidator

import arrow.data.Invalid
import arrow.data.NonEmptyList
import arrow.data.Valid
import arrow.data.ValidatedNel
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class Constraint(val validatedBy: KClass<out Validator<*, *>>)

typealias ClassValidators = Pair<List<Validator<*, *>>, Map<out KProperty1<*, *>, List<Validator<*, *>>>>

fun <T: Any> ClassValidators.validate(t: T): List<Invalidity> {
  val propertyInvalidities = t::class.memberProperties.mapNotNull {
    this.second.get(it)?.let { validators ->
      validators.flatMap {
        (it as Validator<*, T>).validate(t)
      }
    }
  }.flatten()
  val classInvalidities: List<Invalidity> = this.first.flatMap { (it as Validator<*, T>).validate(t) }
  return propertyInvalidities + classInvalidities
}

fun <T: Any> ClassValidators.validateToNel(t: T): ValidatedNel<Invalidity, T> =
  validate(t).let {invalidities ->
    if (invalidities.isEmpty()) Valid(t)
    else Invalid(NonEmptyList.fromListUnsafe(invalidities))
  }

object AnnotationsScanner {
  val log = LoggerFactory.getLogger(AnnotationsScanner::class.java)
  val classValidators = mutableMapOf<KClass<*>, ClassValidators>()
  fun getClassValidatorsFor(klass: KClass<*>) = classValidators.getOrPut(klass, { scanClass(klass) })

  private fun <T : Any> scanClassMembersForClassValidators(klass: KClass<T>): List<Validator<*, *>> =
      log.debug("Scanning class ${klass} for class validators").run {
        klass.annotations.mapNotNull { annotation ->
          log.debug("Processing annotation ${annotation}")
          annotation.annotationClass.annotations.mapNotNull { it as? Constraint }.firstOrNull()?.let { candidate ->
            candidate.validatedBy.primaryConstructor?.let {
              log.debug("Found constructor ${it}")
              if (it.parameters.size == 1)
                it.call(annotation).apply {
                  log.debug("Loaded class validator ${this}")
                }
              else null
            }
          }
        }
      }


  private fun <T : Any> scanClassMembersForPropertyValidators(klass: KClass<T>): Map<out KProperty1<*, *>, List<Validator<*, *>>> =
      log.debug("Scanning class ${klass} for property validators").run {
        klass.memberProperties.mapNotNull { property ->
          val validators = property.annotations.mapNotNull { annotation ->
            annotation.annotationClass.annotations.mapNotNull { it as? Constraint }.firstOrNull()?.let { candidate ->
              candidate.validatedBy.primaryConstructor?.let {
                log.debug("Found constructor ${it}")
                if (it.parameters.size == 2) {
                  it.call(annotation, property).apply {
                    log.debug("Loaded property validator ${property.name} -> ${this}")
                  }
                } else if (it.parameters.size == 3) {
                  if (property.returnType.isSubtypeOf((Collection::class).starProjectedType)) {
                    val validators = (property.returnType.arguments.get(0)!!.type!!.classifier as? KClass<*>)?.let { getClassValidatorsFor(it)}
                    if (validators != null) {
                      it.call(annotation, property, validators)
                    } else null
                  } else null
                }
                else null
              }
            }
          }
          Pair(property, validators)
        }.toMap()
      }

  fun scanClass(klass: KClass<*>): ClassValidators = Pair(scanClassMembersForClassValidators(klass), scanClassMembersForPropertyValidators(klass))
  fun scan(vararg klasses: KClass<*>): Map<KClass<*>, Pair<List<Validator<*, *>>, Map<out KProperty1<*, *>, List<Validator<*, *>>>>> =
      klasses.map { it to scanClass(it) }.toMap()

  fun <T: Any> validate(t: T): List<Invalidity> = scanClass(t::class).validate(t)
  fun <T: Any> validateToNel(t: T): ValidatedNel<Invalidity, T> = scanClass(t::class).validateToNel(t)

}


