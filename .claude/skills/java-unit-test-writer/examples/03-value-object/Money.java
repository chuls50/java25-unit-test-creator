package com.example.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Immutable value object representing a monetary amount in a specific currency.
 */
public final class Money {

    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null)
            throw new IllegalArgumentException("Amount must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Amount must not be negative");
        if (currency == null)
            throw new IllegalArgumentException("Currency must not be null");
        this.amount  = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    /** Convenience factory using a string amount, e.g. Money.of("9.99", "USD") */
    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public BigDecimal getAmount()   { return amount; }
    public Currency getCurrency()   { return currency; }

    /**
     * Returns a new Money representing the sum of this and other.
     *
     * @throws IllegalArgumentException if currencies differ
     */
    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Returns a new Money with the amount multiplied by the given factor.
     *
     * @throws IllegalArgumentException if factor is negative
     */
    public Money multiply(int factor) {
        if (factor < 0)
            throw new IllegalArgumentException("Factor must not be negative");
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }

    /** Returns true if this amount is greater than other. */
    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        Money other = (Money) o;
        return amount.compareTo(other.amount) == 0
            && currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency.getCurrencyCode();
    }
}
