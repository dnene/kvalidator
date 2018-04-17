package tech.dnene.kvalidator.tests

import arrow.core.left
import arrow.core.right
import arrow.data.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import tech.dnene.kvalidator.*
import tech.dnene.kvalidator.types.Invoice
import tech.dnene.kvalidator.types.InvoiceItem
import tech.dnene.kvalidator.validators.NonSequentialInvoiceItems
import java.math.BigDecimal
import kotlin.coroutines.experimental.EmptyCoroutineContext.fold

class TestInvoice {
  @Test
  fun testValidInvoice() {
    val inv = Invoice("123abcXY/-", "counterparty name", listOf(InvoiceItem(1, BigDecimal("123.45"))))
    assertEquals("Invoice should have been considered valid", listOf<Invalidity>(), AnnotationsScanner.validate(inv))
  }

  @Test
  fun testInvalidInvoiceNumber() {
    val inv = Invoice("123abcXY/-#", "counterparty name", listOf(InvoiceItem(1, BigDecimal("123.45"))))
    val expected = listOf<Invalidity>(IncorrectPattern(message = "value-does-not-match-desired-pattern", prop = Invoice::number, pattern = "^[0-9a-zA-Z\\/-]{1,16}$", value = "123abcXY/-#"))
    assertEquals("invalid invoice number test failed", expected, AnnotationsScanner.validate(inv))
  }

  @Test
  fun testShortCounterpartyName() {
    val inv = Invoice("123abcXY/-", "name", listOf(InvoiceItem(1, BigDecimal("123.45"))))
    val expected = listOf<Invalidity>(TooSmall(message = "counterparty name too short or too long", prop = Invoice::counterparty, min = 10, actual = 4))
    assertEquals("too short counterparty name test failed", expected, AnnotationsScanner.validate(inv))
  }

  @Test
  fun testLongCounterpartyName() {
    val inv = Invoice("123abcXY/-", "this is a looooong loooong loooong name", listOf(InvoiceItem(1, BigDecimal("123.45"))))
    val expected = listOf<Invalidity>(TooLarge(message = "counterparty name too short or too long", prop = Invoice::counterparty, max = 32, actual = 39))
    assertEquals("too long counterparty name test failed", expected, AnnotationsScanner.validate(inv))
  }

  @Test
  fun testRecursion() {
    val inv = Invoice("123abcXY/-", "counterparty name", listOf(InvoiceItem(-1, BigDecimal("123.45"))))
    val expected = listOf<Invalidity>(TooSmall(message = "Must be a positive value", prop = InvoiceItem::num, min = 0, actual = -1))
    assertEquals("recursion test failed", expected, AnnotationsScanner.validate(inv))
  }

  @Test
  fun testClassValidators() {
    val inv = Invoice("123abcXY/-", "counterparty name",
        listOf(InvoiceItem(1, BigDecimal("123.45")), InvoiceItem(3, BigDecimal("123.45"))))
    val expected = listOf<Invalidity>(NonSequentialInvoiceItems(message = "Invoice item numbers are not sequential", min = 1, max = 3, count = 2))
    assertEquals("class validator test failed", expected, AnnotationsScanner.validate(inv))
  }

  @Test
  fun testMultipleInvalidities() {
    val inv = Invoice("123abcXY/-#", "name", listOf(InvoiceItem(-1, BigDecimal("123.45")), InvoiceItem(1, BigDecimal("123.45"))))
    val expected = listOf<Invalidity>(
        TooSmall(message = "counterparty name too short or too long", prop = Invoice::counterparty, min = 10, actual = 4),
        TooSmall(message = "Must be a positive value", prop = InvoiceItem::num, min = 0, actual = -1),
        IncorrectPattern(message = "value-does-not-match-desired-pattern", prop = Invoice::number, pattern = "^[0-9a-zA-Z\\/-]{1,16}$", value = "123abcXY/-#"),
        NonSequentialInvoiceItems(message = "Invoice item numbers are not sequential", min = -1, max = 1, count = 2))
    assertEquals("multiple invalidities test failed", expected, AnnotationsScanner.validate(inv))
  }

  @Test
  fun testValidInvoiceToValidNel() {
    val inv = Invoice("123abcXY/-", "counterparty name", listOf(InvoiceItem(1, BigDecimal("123.45"))))
    AnnotationsScanner.validateToNel(inv).fold(
        { fail("invoice should've been validated successfully") },
        { ; }
    )
  }

  @Test
  fun testMultipleInvaliditiesToNel() {
    val inv = Invoice("123abcXY/-#", "name", listOf(InvoiceItem(-1, BigDecimal("123.45")), InvoiceItem(1, BigDecimal("123.45"))))
    val expected = listOf<Invalidity>(
        TooSmall(message = "counterparty name too short or too long", prop = Invoice::counterparty, min = 10, actual = 4),
        TooSmall(message = "Must be a positive value", prop = InvoiceItem::num, min = 0, actual = -1),
        IncorrectPattern(message = "value-does-not-match-desired-pattern", prop = Invoice::number, pattern = "^[0-9a-zA-Z\\/-]{1,16}$", value = "123abcXY/-#"),
        NonSequentialInvoiceItems(message = "Invoice item numbers are not sequential", min = -1, max = 1, count = 2))
    AnnotationsScanner.validateToNel(inv).fold(
        {  assertEquals("multiple invalidities test failed", expected, it.all) },
        { fail("object validation should have resulted into invalidites")}
    )

        // code does not compile
//    val result = validated.withEither {
//      it.fold(
//          { WrappedNelFault(it).left() },
//          { it.right() })
//    }
//
//    result.fold(
//        { assertEquals("wrapping as either failed", expected, it.nel.all) },
//        { fail("result should not have been a success") })
  }
}