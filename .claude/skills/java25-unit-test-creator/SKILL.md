---
name: java-unit-test-writer
description: >
  Use this skill whenever the user wants to generate, write, improve, or review Java unit tests.
  Triggers include: any mention of JUnit, unit tests for Java classes, test coverage, mocking
  with Mockito, writing test cases for Java methods, or requests like "write tests for this class",
  "add unit tests", "generate JUnit tests", "improve my tests", or "test this Java code". Also
  trigger when the user shares a Java file and asks about testing, coverage, or quality — even
  if they don't use the word "test". Trigger when the user asks about AssertJ, Mockito, JaCoCo,
  parameterized tests, or test organization in Java. Do NOT use for integration tests,
  end-to-end tests, or non-Java languages.
---

# Java Unit Test Writer

Generates clean, idiomatic JUnit 5 unit tests for Java classes — from simple utilities to
service layers with mocked dependencies.

---

## Quick Reference

| Task                      | Go to                                                      |
| ------------------------- | ---------------------------------------------------------- |
| Full annotation reference | [references/annotations.md](references/annotations.md)     |
| Mockito mocking patterns  | [references/mockito.md](references/mockito.md)             |
| AssertJ fluent assertions | [references/assertj.md](references/assertj.md)             |
| Parameterized tests       | [references/parameterized.md](references/parameterized.md) |
| Real-world test patterns  | [references/patterns.md](references/patterns.md)           |
| Coverage with JaCoCo      | [references/coverage.md](references/coverage.md)           |

---

## Workflow

### Step 1 — Read the Source

If a Java file is uploaded, read it from `/mnt/user-data/uploads/`. If no file is provided, ask
the user to paste the class or upload it before proceeding.

```bash
cat /mnt/user-data/uploads/MyClass.java
```

### Step 2 — Classify the Class

Identify what kind of class you are testing. This determines the approach:

| Class Type               | Characteristics                             | Approach                                                          |
| ------------------------ | ------------------------------------------- | ----------------------------------------------------------------- |
| **Pure logic / utility** | No injected dependencies                    | Instantiate directly in `@BeforeEach`                             |
| **Service layer**        | Has `@Autowired` / constructor dependencies | Use Mockito — read [references/mockito.md](references/mockito.md) |
| **Value object / POJO**  | Getters, setters, equals, hashCode          | Test construction, equality, edge values                          |
| **Static utility**       | All static methods                          | Call directly; no setup needed                                    |
| **Collections-heavy**    | Returns/accepts `List`, `Map`, `Set`        | Use AssertJ — read [references/assertj.md](references/assertj.md) |

### Step 3 — Plan the Tests

Before writing, outline which methods you'll test and what cases matter:

- **Happy path** — expected behavior with valid input
- **Edge cases** — nulls, empty collections, zero, boundary values, negative numbers
- **Error cases** — exceptions that should be thrown

Keep this outline short. Confirm with the user if the scope is large or ambiguous.

### Step 4 — Write the Tests

Follow the conventions below and generate the test file. Determine the class name
from the source file and write the output to the `outputs/` folder at the root of
this repo using the naming convention `<SourceClassName>Test.java`.

```bash
# Example output path
outputs/OrderServiceTest.java
outputs/StringUtilsTest.java
```

---

## Core Conventions

### File Structure

```java
package com.example; // match the source package exactly

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
// OR: import static org.assertj.core.api.Assertions.assertThat;

class MyClassTest {

    private MyClass subject;

    @BeforeEach
    void setUp() {
        subject = new MyClass();
    }

    @Test
    void methodName_whenCondition_thenExpectedResult() {
        // Arrange
        // Act
        // Assert
    }
}
```

### Test Naming Convention

Use the pattern: `methodName_whenCondition_thenExpectedResult`

```java
// Good
calculateTotal_whenItemsAreEmpty_thenReturnsZero()
findUser_whenIdNotFound_thenThrowsNotFoundException()
processOrder_whenPaymentDeclined_thenRollsBackAndThrows()

// Avoid
testMethod1()
test_calculate()
checkIfWorks()
```

### Assertions — Choose the Right Tool

**Use JUnit 5 built-ins** for simple, single-value checks:

```java
assertEquals(42, result);
assertNotNull(result);
assertTrue(list.isEmpty());
assertThrows(IllegalArgumentException.class, () -> subject.method(null));
assertAll(
    () -> assertEquals("Alice", user.getName()),
    () -> assertEquals(30, user.getAge())
);
```

**Use AssertJ** for collections, strings, complex objects, and chained assertions.
See [references/assertj.md](references/assertj.md) for a full reference.

```java
// Collection example
assertThat(result)
    .hasSize(3)
    .contains("apple")
    .doesNotContain("kiwi");

// Object fields example
assertThat(user)
    .extracting("name", "age")
    .containsExactly("Alice", 30);
```

**Prefer AssertJ over JUnit built-ins** when:

- Asserting on a collection, map, or optional
- Chaining multiple assertions on the same object
- You want a more readable failure message

### Test Structure — AAA Pattern

Every test must follow Arrange / Act / Assert:

```java
@Test
void add_whenBothPositive_thenReturnsSum() {
    // Arrange
    int a = 3;
    int b = 4;

    // Act
    int result = subject.add(a, b);

    // Assert
    assertEquals(7, result);
}
```

### Exception Testing

```java
@Test
void divide_whenDivisorIsZero_thenThrowsArithmeticException() {
    assertThrows(ArithmeticException.class, () -> subject.divide(10, 0));
}

// Also verify the message if it matters:
@Test
void process_whenInputNull_thenThrowsWithMessage() {
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> subject.process(null)
    );
    assertEquals("Input must not be null", ex.getMessage());
}
```

---

## When to Use Which Reference File

| Situation                                                   | Read                                            |
| ----------------------------------------------------------- | ----------------------------------------------- |
| Class has `@Autowired` or constructor-injected dependencies | [mockito.md](references/mockito.md)             |
| Testing collections, strings, or object fields              | [assertj.md](references/assertj.md)             |
| Same test logic with many different inputs                  | [parameterized.md](references/parameterized.md) |
| Need `@Nested`, `@Tag`, `@Disabled`, `@Timeout`             | [annotations.md](references/annotations.md)     |
| Real-world examples (service, CRUD, util)                   | [patterns.md](references/patterns.md)           |
| User asks about coverage or JaCoCo                          | [coverage.md](references/coverage.md)           |

---

## Dependencies (pom.xml)

```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>

<!-- AssertJ (recommended for expressive assertions) -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.25.3</version>
    <scope>test</scope>
</dependency>

<!-- Mockito (add if mocking is needed) -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.11.0</version>
    <scope>test</scope>
</dependency>
```

> **Note:** If using Spring Boot, `spring-boot-starter-test` includes JUnit 5, AssertJ,
> and Mockito automatically — no individual entries needed.

---

## Output Checklist

Before presenting the test file, verify:

- [ ] Package declaration matches the source class
- [ ] Test class is named `<SourceClass>Test`
- [ ] `@BeforeEach` sets up the subject under test (or `@ExtendWith` if Mockito is used)
- [ ] Every public method has at least one test
- [ ] At least one edge case per non-trivial method
- [ ] No test depends on another test (fully independent)
- [ ] Assertions are specific — not just `assertTrue(x != null)` but `assertNotNull(x)`
- [ ] File saved to `outputs/<SourceClassName>Test.java` at the repo root
