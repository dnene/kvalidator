package tech.dnene.kvalidator.tests

import org.junit.Assert.assertEquals
import org.junit.Test
import tech.dnene.kvalidator.IncorrectPattern
import tech.dnene.kvalidator.Invalidity
import tech.dnene.kvalidator.TooLarge
import tech.dnene.kvalidator.TooSmall
import tech.dnene.kvalidator.types.Invoice
import tech.dnene.kvalidator.types.InvoiceItem
import tech.dnene.kvalidator.validators.*
import java.math.BigDecimal
import java.util.regex.Pattern
import kotlin.reflect.KProperty1

operator fun <T> List<(T)->List<Invalidity>>.invoke(t: T) = this.flatMap { it(t) }
fun <T> T.validate(v: List<(T)->List<Invalidity>>) = v.flatMap { it(this) }

val validations = listOf(
        Invoice::counterparty.range(10,32),
        Invoice::number.pattern("^[0-9a-zA-Z\\/-]{1,16}$"),
        Invoice::class.inSequence(),
        Invoice::items.recurse(listOf(
                InvoiceItem::num.min(0)
        )))


class TestFunctional {
    @Test
    fun testValidInvoice() {
        val invoice = Invoice("123abcXY/-", "counterparty name", listOf(InvoiceItem(1, BigDecimal("123.45"))))
        assertEquals("Valid invoice should've had no errors", listOf<Invalidity>(), invoice.validate(validations))
        assertEquals("Valid invoice should've had no errors", listOf<Invalidity>(), validations(invoice))
    }

    @Test
    fun testInValidInvoiceNumber() {
        val invoice = Invoice("123abcXY/-#", "counterparty name", listOf(InvoiceItem(1, BigDecimal("123.45"))))
        assertEquals("Invalid invoice number should've had one error", listOf<Invalidity>(IncorrectPattern(message="number does not match expected pattern", prop=Invoice::number, pattern="^[0-9a-zA-Z\\/-]{1,16}$", value="123abcXY/-#")), invoice.validate(validations))
    }

    @Test
    fun testShortCounterpartyName() {
        val invoice = Invoice("123abcXY/-", "name", listOf(InvoiceItem(1, BigDecimal("123.45"))))
        assertEquals("expected one error for short counterparty name", listOf<Invalidity>(TooSmall(message="counterparty length too small", prop=Invoice::counterparty, min=10, actual=4)), invoice.validate(validations))
    }

    @Test
    fun testLongCounterpartyName() {
        val invoice = Invoice("123abcXY/-", "this is a looooong loooong loooong name", listOf(InvoiceItem(1, BigDecimal("123.45"))))
        assertEquals("expected one error for long counterparty name",
                listOf<Invalidity>(TooLarge(message="counterparty length too large", prop=Invoice::counterparty, max=32, actual=39)), invoice.validate(validations))
    }

    @Test
    fun testRecursion() {
        val invoice = Invoice("123abcXY/-", "counterparty name", listOf(InvoiceItem(-1, BigDecimal("123.45"))))
        val expected = listOf(TooSmall("num too small", InvoiceItem::num, 0, -1))
        assertEquals("Valid invoice should've had no errors", expected, invoice.validate(validations))
    }
}

