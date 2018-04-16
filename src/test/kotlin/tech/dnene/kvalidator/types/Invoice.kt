package tech.dnene.kvalidator.types

import tech.dnene.kvalidator.validators.*

import java.math.BigDecimal

@SequentialInvoiceItems(message="Invoice item numbers are not sequential")
data class Invoice(
        @Pattern(pattern="^[0-9a-zA-Z\\/-]{1,16}$")
        val number: String,
        @Size(min=10, max=32, message="counterparty name too short or too long")
        val counterparty: String,
        @Recurse
        val items: List<InvoiceItem>)

data class InvoiceItem(
        @Min(min=0, message="Must be a positive value")
        val num: Int,
        val amount: BigDecimal)

