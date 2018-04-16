package tech.dnene.kvalidator

import arrow.data.*
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

object AnnotationsManager {
    val log = LoggerFactory.getLogger(AnnotationsManager::class.java)
    val classValidators: MutableMap<KClass<*>, Pair<List<Validator<*, *>>, Map<out KProperty1<*,*>, List<Validator<*, *>>>>> = mutableMapOf()
    val noValidators = mutableSetOf<KClass<*>>()

    fun <T: Any> scanClassMembersForClassValidators(klass: KClass<T>):List<Validator<*, *>> =
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


    fun <T: Any> scanClassMembersForPropertyValidators(klass: KClass<T>): Map<out KProperty1<*,*>, List<Validator<*, *>>> =
            log.debug("Scanning class ${klass} for property validators").run {
                klass.memberProperties.mapNotNull { property ->
                    val validators = property.annotations.mapNotNull { annotation ->
                        annotation.annotationClass.annotations.mapNotNull { it as? Constraint }.firstOrNull()?.let { candidate ->
                            candidate.validatedBy.primaryConstructor?.let {
                                log.debug("Found constructor ${it}")
                                if (it.parameters.size == 2)
                                    it.call(annotation, property).apply {
                                        log.debug("Loaded property validator ${property.name} -> ${this}")
                                    }
                                else null
                            }
                        }
                    }
                    Pair(property, validators)
                }.toMap()
            }



    fun getClassMembers(klass: KClass<*>): Pair<List<Validator<*, *>>, Map<out KProperty1<*,*>, List<Validator<*, *>>>>? =
            classValidators.get(klass).let { pair ->
                if (pair == null) {
                    if (noValidators.contains(klass)) null else {
                        val newPropertyValidators = scanClassMembersForPropertyValidators(klass)
                        val newClassValidators = scanClassMembersForClassValidators(klass)
                        if (newPropertyValidators.isEmpty() && newClassValidators.isEmpty()) {
                            noValidators.add(klass)
                            null
                        } else {
                            Pair(newClassValidators, newPropertyValidators).apply {
                                log.debug("Loaded class ${klass}")
                                classValidators[klass] = this
                            }
                        }
                    }
                } else pair
            }



}

object DataValidator: ClassValidator<Any> {
    val log = LoggerFactory.getLogger(DataValidator::class.java)
    fun validateObject(t: Any): List<Invalidity> {
        val validatorPair = AnnotationsManager.getClassMembers(t::class) as Pair<List<Validator<*, Any>>,Map<KProperty1<Any,*>, List<Validator<*, Any>>>>?
        return if (validatorPair != null) {
            val propertyInvalidities = t::class.memberProperties.mapNotNull {
                validatorPair.second.get(it)?.let { validators ->
                    validators.flatMap {
                        it.validate(t)
                    }
                }
            }.flatten()
            val classInvalidities: List<Invalidity> = validatorPair.first.flatMap { it.validate(t) }
            return propertyInvalidities + classInvalidities
        } else {
            EMPTY_LIST
        }
    }

    override fun validate(t: Any): ValidatedNel<Invalidity, Any> =
        validateObject(t).let { result ->
            log.debug("Result is ${result}")
            if (result.isEmpty()) t.validNel() else Invalid(NonEmptyList.fromListUnsafe(result))
        }
    fun validateRecurse(iter: Iterable<*>) =
        iter.flatMap {
            if (it != null) {
                validateObject(it)
            } else EMPTY_LIST
        }


}
