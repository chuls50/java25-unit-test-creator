# Example 3 — Value Object (Immutable Domain Model)

## Class Type

Immutable value object — no dependencies to mock, but rich validation, arithmetic,
and equality contract that all need testing.

## Files

| File             | Description                       |
| ---------------- | --------------------------------- |
| `Money.java`     | The source class Claude was given |
| `MoneyTest.java` | The test file Claude generated    |

## Prompt Used

```
Write comprehensive unit tests for Money.java.
Test construction validation, arithmetic, equality, and edge cases.
```

## What Claude Did

1. **Classified** the class as an immutable value object — no mocking needed, direct
   instantiation in each test.
2. **Used `@Nested`** to group tests by concept: Construction, add, multiply, isGreaterThan,
   equals/hashCode, and toString — mirrors the class's responsibility areas.
3. **Used `@CsvSource` parameterized tests** for arithmetic operations (add, multiply)
   where many input/output pairs share the same logic.
4. **Used `isEqualByComparingTo`** from AssertJ (not `assertEquals`) for `BigDecimal` —
   correctly handles `10.0` vs `10.00` without failing on scale differences.
5. **Tested the equality contract** — equal values, trailing zeros, currency mismatch,
   amount mismatch, null comparison, and `hashCode` consistency.
6. **Tested the factory method** (`Money.of(...)`) separately from the constructor.

## Key Techniques Demonstrated

- `isEqualByComparingTo()` for BigDecimal (never use `assertEquals` for BigDecimal)
- Testing both `equals()` AND `hashCode()` consistency
- `@Nested` for value objects with multiple logical concerns
- Parameterized arithmetic tests with `@CsvSource`
- Immutability verification — `add()` and `multiply()` return new instances

## Where the File Lands

Claude writes the test file to:
outputs/<ClassName>Test.java

From there, move it into your Java project:
your-java-project/
└── src/
└── test/
└── java/
└── com/
└── example/
└── <package>/
└── <ClassName>Test.java ← here

## Run It

```bash
mvn test -Dtest=MoneyTest
# or
./gradlew test --tests "com.example.domain.MoneyTest"
```
