# Parameterized Tests Reference

Use `@ParameterizedTest` to run the same test logic with multiple inputs. This eliminates
copy-paste duplication and makes coverage of boundary conditions explicit.

## Dependency

```xml
<!-- Included in junit-jupiter, but can also be added explicitly -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>
```

---

## Source Types — Decision Guide

| Scenario                            | Use                                                    |
| ----------------------------------- | ------------------------------------------------------ |
| Single primitive or string argument | `@ValueSource`                                         |
| Single null or empty value          | `@NullSource` / `@EmptySource` / `@NullAndEmptySource` |
| Enum values                         | `@EnumSource`                                          |
| Multiple arguments inline           | `@CsvSource`                                           |
| Multiple arguments from a file      | `@CsvFileSource`                                       |
| Complex objects or reusable data    | `@MethodSource`                                        |
| Fully custom provider class         | `@ArgumentsSource`                                     |

---

## @ValueSource — Single Argument

Good for: testing the same method against a list of valid or invalid primitives/strings.

```java
@ParameterizedTest
@ValueSource(strings = {"", "   ", "\t", "\n"})
void isBlank_whenWhitespace_thenReturnsTrue(String input) {
    assertTrue(subject.isBlank(input));
}

@ParameterizedTest
@ValueSource(ints = {1, 2, 3, 100, Integer.MAX_VALUE})
void isPositive_whenPositiveInt_thenReturnsTrue(int number) {
    assertTrue(subject.isPositive(number));
}
```

Supported types: `strings`, `ints`, `longs`, `doubles`, `booleans`, `chars`, `bytes`, `shorts`,
`floats`, `classes`.

---

## @NullSource / @EmptySource / @NullAndEmptySource

Good for: ensuring null/empty inputs are handled correctly.

```java
@ParameterizedTest
@NullAndEmptySource
@ValueSource(strings = {"  ", "\t"})
void isBlankOrNull_thenReturnsTrue(String input) {
    assertTrue(subject.isBlank(input));
}
```

- `@NullSource` — passes `null` once
- `@EmptySource` — passes `""` (or empty collection/array for those types)
- `@NullAndEmptySource` — both, combined

---

## @EnumSource — Enum Values

Good for: running a test against a subset (or all) of an enum's constants.

```java
enum Status { ACTIVE, INACTIVE, PENDING, BANNED }

// All values
@ParameterizedTest
@EnumSource(Status.class)
void hasStatus_whenAnyStatus_thenNotNull(Status status) {
    assertNotNull(subject.getLabel(status));
}

// Specific include list
@ParameterizedTest
@EnumSource(value = Status.class, names = {"ACTIVE", "PENDING"})
void isEligible_whenActiveOrPending_thenTrue(Status status) {
    assertTrue(subject.isEligible(status));
}

// Exclude list
@ParameterizedTest
@EnumSource(value = Status.class, mode = EnumSource.Mode.EXCLUDE, names = {"BANNED"})
void canLogin_whenNotBanned_thenTrue(Status status) {
    assertTrue(subject.canLogin(status));
}
```

---

## @CsvSource — Multiple Arguments Inline

Good for: input-output pairs, or any test needing 2–4 columns of simple data.

```java
@ParameterizedTest
@CsvSource({
    "alice, 25, true",
    "bob,   17, false",
    "carol, 0,  false"
})
void isAdult_givenAgeAndName(String name, int age, boolean expected) {
    assertEquals(expected, subject.isAdult(age));
}
```

**Handling nulls in CSV:**

```java
@ParameterizedTest
@CsvSource(value = {
    "alice, 25",
    "     , 30",   // blank string
    "NULL, 0"      // default: string "NULL" — use nullValues attribute to treat as null
}, nullValues = "NULL")
void create_withVariousInputs(String name, int age) {
    // name will be null when input is "NULL"
}
```

**Custom display name:**

```java
@ParameterizedTest(name = "age={1} → isAdult={2}")
@CsvSource({"alice, 25, true", "bob, 17, false"})
void isAdult(...) { ... }
```

---

## @CsvFileSource — Arguments from a File

Good for: large data sets or data managed by non-developers.

Place file in `src/test/resources/test-data.csv`:

```
input,expected
"hello","HELLO"
"world","WORLD"
"",""
```

```java
@ParameterizedTest
@CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
void toUpperCase_withCsvFile(String input, String expected) {
    assertEquals(expected, input.toUpperCase());
}
```

---

## @MethodSource — Complex Arguments

Good for: complex objects, reusable providers, or when data needs computation.

```java
@ParameterizedTest
@MethodSource("provideOrderScenarios")
void calculateDiscount_givenTotal(BigDecimal total, int expectedDiscountPercent) {
    assertEquals(expectedDiscountPercent, subject.discountPercent(total));
}

// The provider — must be static unless @TestInstance(PER_CLASS) is used
private static Stream<Arguments> provideOrderScenarios() {
    return Stream.of(
        Arguments.of(new BigDecimal("50.00"),  0),
        Arguments.of(new BigDecimal("100.00"), 5),
        Arguments.of(new BigDecimal("200.00"), 10),
        Arguments.of(new BigDecimal("0.00"),   0)
    );
}
```

**Sharing a provider across test classes:**

```java
@ParameterizedTest
@MethodSource("com.example.TestData#orderScenarios")
void calculateDiscount_withSharedProvider(BigDecimal total, int expected) { ... }

// In TestData.java:
public class TestData {
    public static Stream<Arguments> orderScenarios() { ... }
}
```

---

## @ArgumentsSource — Reusable Provider Class

Good for: when the same data set is shared by many test classes.

```java
// Provider class
public class UserArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext ctx) {
        return Stream.of(
            Arguments.of(new User("Alice", 30)),
            Arguments.of(new User("Bob", 17))
        );
    }
}

// Usage
@ParameterizedTest
@ArgumentsSource(UserArgumentsProvider.class)
void validateUser(User user) {
    assertNotNull(user.getName());
}
```

---

## Best Practices

- Use `@CsvSource` for simple input-output pairs — it keeps data next to the test.
- Use `@MethodSource` for complex objects or when you need to reuse the data set.
- Always add a `name` attribute to `@ParameterizedTest` for readable test reports:
  `@ParameterizedTest(name = "input={0} → expected={1}")`
- Keep provider methods (`@MethodSource`) close to the test method in the same class, or in a
  dedicated `TestData` utility class for shared data.
- Avoid using `@MethodSource` for trivial single-argument cases — `@ValueSource` is cleaner.
