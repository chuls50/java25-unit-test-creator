package com.example.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Money value object.
 *
 * Class type: Immutable value object — no mocking needed.
 * Prompt used: "Write comprehensive unit tests for Money.java.
 *               Test construction validation, arithmetic, equality, and edge cases."
 */
class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    // =========================================================================
    // Construction
    // =========================================================================

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("creates Money with valid amount and currency")
        void givenValidInput_thenCreatesInstance() {
            Money money = new Money(new BigDecimal("10.50"), USD);

            assertThat(money.getAmount()).isEqualByComparingTo("10.50");
            assertThat(money.getCurrency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("rounds amount to 2 decimal places on construction")
        void givenMoreThanTwoDecimals_thenRoundsToScale() {
            Money money = new Money(new BigDecimal("9.999"), USD);

            assertThat(money.getAmount()).isEqualByComparingTo("10.00");
        }

        @Test
        @DisplayName("accepts zero amount")
        void givenZeroAmount_thenCreatesInstance() {
            assertDoesNotThrow(() -> new Money(BigDecimal.ZERO, USD));
        }

        @Test
        @DisplayName("throws when amount is null")
        void givenNullAmount_thenThrows() {
            assertThatThrownBy(() -> new Money(null, USD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount");
        }

        @Test
        @DisplayName("throws when amount is negative")
        void givenNegativeAmount_thenThrows() {
            assertThatThrownBy(() -> new Money(new BigDecimal("-0.01"), USD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
        }

        @Test
        @DisplayName("throws when currency is null")
        void givenNullCurrency_thenThrows() {
            assertThatThrownBy(() -> new Money(BigDecimal.ONE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency");
        }

        @Test
        @DisplayName("factory method Money.of() creates correctly")
        void factoryOf_givenValidStrings_thenCreatesInstance() {
            Money money = Money.of("19.99", "USD");

            assertThat(money.getAmount()).isEqualByComparingTo("19.99");
            assertThat(money.getCurrency()).isEqualTo(USD);
        }
    }

    // =========================================================================
    // add
    // =========================================================================

    @Nested
    @DisplayName("add")
    class Add {

        @ParameterizedTest(name = "{0} + {1} = {2} USD")
        @CsvSource({
            "5.00,  3.00,  8.00",
            "0.00,  1.50,  1.50",
            "9.99,  0.01, 10.00",
            "0.00,  0.00,  0.00"
        })
        @DisplayName("returns correct sum for same-currency amounts")
        void givenSameCurrency_thenReturnsCorrectSum(String a, String b, String expected) {
            Money moneyA = Money.of(a, "USD");
            Money moneyB = Money.of(b, "USD");

            Money result = moneyA.add(moneyB);

            assertThat(result.getAmount()).isEqualByComparingTo(expected);
            assertThat(result.getCurrency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("throws when currencies differ")
        void givenDifferentCurrencies_thenThrows() {
            Money usd = Money.of("10.00", "USD");
            Money eur = Money.of("10.00", "EUR");

            assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mismatch");
        }
    }

    // =========================================================================
    // multiply
    // =========================================================================

    @Nested
    @DisplayName("multiply")
    class Multiply {

        @ParameterizedTest(name = "10.00 * {0} = {1}")
        @CsvSource({"0, 0.00", "1, 10.00", "3, 30.00", "10, 100.00"})
        @DisplayName("scales amount correctly")
        void givenPositiveFactor_thenScalesAmount(int factor, String expected) {
            Money money = Money.of("10.00", "USD");

            Money result = money.multiply(factor);

            assertThat(result.getAmount()).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("throws when factor is negative")
        void givenNegativeFactor_thenThrows() {
            Money money = Money.of("10.00", "USD");

            assertThatThrownBy(() -> money.multiply(-1))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // =========================================================================
    // isGreaterThan
    // =========================================================================

    @Nested
    @DisplayName("isGreaterThan")
    class IsGreaterThan {

        @Test
        @DisplayName("returns true when this amount is larger")
        void givenLargerAmount_thenReturnsTrue() {
            assertTrue(Money.of("10.00", "USD").isGreaterThan(Money.of("9.99", "USD")));
        }

        @Test
        @DisplayName("returns false when amounts are equal")
        void givenEqualAmounts_thenReturnsFalse() {
            assertFalse(Money.of("10.00", "USD").isGreaterThan(Money.of("10.00", "USD")));
        }

        @Test
        @DisplayName("returns false when this amount is smaller")
        void givenSmallerAmount_thenReturnsFalse() {
            assertFalse(Money.of("5.00", "USD").isGreaterThan(Money.of("10.00", "USD")));
        }

        @Test
        @DisplayName("throws when currencies differ")
        void givenDifferentCurrencies_thenThrows() {
            assertThatThrownBy(
                () -> Money.of("10.00", "USD").isGreaterThan(Money.of("5.00", "EUR")))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // =========================================================================
    // equals and hashCode
    // =========================================================================

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("equal when amount and currency match")
        void givenSameAmountAndCurrency_thenEqual() {
            Money a = Money.of("10.00", "USD");
            Money b = Money.of("10.00", "USD");

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("equal when amounts differ only in trailing zeros")
        void givenTrailingZeroDifference_thenStillEqual() {
            Money a = new Money(new BigDecimal("10.0"), USD);
            Money b = new Money(new BigDecimal("10.00"), USD);

            assertEquals(a, b);
        }

        @Test
        @DisplayName("not equal when currencies differ")
        void givenDifferentCurrencies_thenNotEqual() {
            assertNotEquals(Money.of("10.00", "USD"), Money.of("10.00", "EUR"));
        }

        @Test
        @DisplayName("not equal when amounts differ")
        void givenDifferentAmounts_thenNotEqual() {
            assertNotEquals(Money.of("10.00", "USD"), Money.of("10.01", "USD"));
        }

        @Test
        @DisplayName("not equal to null")
        void givenNull_thenNotEqual() {
            assertNotEquals(null, Money.of("10.00", "USD"));
        }
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    @DisplayName("toString returns amount and currency code")
    void toString_returnsReadableRepresentation() {
        assertThat(Money.of("9.99", "USD").toString()).isEqualTo("9.99 USD");
    }
}
