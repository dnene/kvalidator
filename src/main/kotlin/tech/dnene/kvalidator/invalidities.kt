package tech.dnene.kvalidator

import kotlin.reflect.KProperty1

data class TooSmall(override val message: String, val prop: KProperty1<*,*>, val min: Int, val actual: Int): Invalidity
data class TooLarge(override val message: String, val prop: KProperty1<*,*>, val max: Int, val actual: Int): Invalidity
data class IncorrectPattern(override val message: String, val prop: KProperty1<*,*>, val pattern: String, val value: String): Invalidity








