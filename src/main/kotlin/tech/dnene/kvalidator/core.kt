package tech.dnene.kvalidator

interface Invalidity {
  val message: String
}

val EMPTY_LIST = listOf<Invalidity>()

typealias Validating<T, I> = (T) -> List<I>

interface Validator<C, in T> {
    val defaultMessage: String
    fun validate(t: T): List<Invalidity>
}


