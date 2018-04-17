package tech.dnene.kvalidator.validators

import tech.dnene.kvalidator.*
import tech.dnene.kvalidator.types.Invoice
import kotlin.reflect.KClass

fun KClass<Invoice>.inSequence(message: String? = null): Validating<Invoice, Invalidity> = { t: Invoice ->
            t.items.map { it.num}.toSet().let { set ->
                if (set.size == 0) EMPTY_LIST
                else if ((set.max()!! - set.min()!!) != (set.size - 1))
                    listOf(NonSequentialInvoiceItems(
                            message ?: "invoice items are not sequential",
                            set.min()!!, set.max()!!, set.size))
                else EMPTY_LIST
            }
        }


data class NonSequentialInvoiceItems(override val message: String, val min: Int, val max: Int, val count: Int): Invalidity {

}
class SequentialInvoiceItemsValidator(val c: SequentialInvoiceItems): Validator<SequentialInvoiceItems, Invoice> {
    override fun toString() = "SequentialInvoiceItemsValidator(${c})"
    override val defaultMessage = "invoice-items-are-not-sequential"
    val message = if (c.message.isBlank()) defaultMessage else c.message
    override fun validate(t: Invoice): List<Invalidity> = Invoice::class.inSequence(message)(t)
}

@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = SequentialInvoiceItemsValidator::class)
annotation class SequentialInvoiceItems(val message: String = "")

