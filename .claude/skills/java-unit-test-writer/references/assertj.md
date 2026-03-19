# AssertJ Reference

AssertJ provides a fluent, strongly-typed assertion API that produces far more readable error
messages than JUnit's built-in `assertEquals`. Prefer it for anything beyond single-value
equality checks.

## Import

```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
```

---

## Strings

```java
assertThat("Hello World")
    .isNotNull()
    .isNotBlank()
    .startsWith("Hello")
    .endsWith("World")
    .contains("lo W")
    .containsIgnoringCase("hello")
    .hasSize(11)
    .doesNotContain("Goodbye");
```

---

## Numbers

```java
assertThat(42).isEqualTo(42);
assertThat(price).isGreaterThan(0).isLessThanOrEqualTo(100);
assertThat(0.1 + 0.2).isCloseTo(0.3, within(0.001)); // floating point safe
```

---

## Booleans

```java
assertThat(result).isTrue();
assertThat(flag).isFalse();
```

---

## Nulls

```java
assertThat(obj).isNotNull();
assertThat(nullableResult).isNull();
```

---

## Collections (List, Set, array)

```java
List<String> names = List.of("Alice", "Bob", "Charlie");

// Size
assertThat(names).hasSize(3);
assertThat(names).isNotEmpty();
assertThat(emptyList).isEmpty();

// Containment
assertThat(names).contains("Alice", "Bob");          // order doesn't matter
assertThat(names).containsExactly("Alice", "Bob", "Charlie"); // exact order
assertThat(names).containsExactlyInAnyOrder("Charlie", "Alice", "Bob");
assertThat(names).containsOnly("Alice", "Bob", "Charlie"); // no extras
assertThat(names).doesNotContain("Dave");

// Element conditions
assertThat(names).allMatch(n -> n.length() > 2);
assertThat(names).anyMatch(n -> n.startsWith("A"));
assertThat(names).noneMatch(n -> n.isBlank());

// Extracting fields from a list of objects
List<User> users = List.of(new User("Alice", 30), new User("Bob", 25));
assertThat(users)
    .extracting("name")
    .containsExactly("Alice", "Bob");

assertThat(users)
    .extracting(User::getName, User::getAge)
    .containsExactly(
        tuple("Alice", 30),
        tuple("Bob", 25)
    );
```

---

## Maps

```java
Map<String, Integer> scores = Map.of("Alice", 95, "Bob", 82);

assertThat(scores)
    .hasSize(2)
    .containsKey("Alice")
    .containsEntry("Alice", 95)
    .doesNotContainKey("Charlie");
```

---

## Optionals

```java
Optional<String> present = Optional.of("hello");
Optional<String> empty   = Optional.empty();

assertThat(present).isPresent().contains("hello");
assertThat(empty).isEmpty();
```

---

## Objects and Field-by-Field Comparison

Use `usingRecursiveComparison()` when `equals()` is not implemented or you want to ignore fields:

```java
User expected = new User("Alice", 30);
User actual   = service.findByName("Alice");

// Exact field match (ignores equals() implementation)
assertThat(actual)
    .usingRecursiveComparison()
    .isEqualTo(expected);

// Ignore specific fields (e.g. auto-generated IDs or timestamps)
assertThat(actual)
    .usingRecursiveComparison()
    .ignoringFields("id", "createdAt")
    .isEqualTo(expected);
```

---

## Exceptions

**Preferred style** — captures and chains assertions on the exception:

```java
assertThatThrownBy(() -> service.process(null))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("Input must not be null")
    .hasMessageContaining("null");
```

**For verifying NO exception is thrown:**

```java
assertThatCode(() -> service.process(validInput))
    .doesNotThrowAnyException();
```

---

## Soft Assertions (collect all failures at once)

Use when you want to run all assertions and see every failure, not just the first:

```java
import org.assertj.core.api.SoftAssertions;

@Test
void validateUser() {
    User user = service.getUser(1L);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(user.getName()).isEqualTo("Alice");
    softly.assertThat(user.getAge()).isGreaterThan(0);
    softly.assertThat(user.getEmail()).contains("@");
    softly.assertAll(); // throws if any assertion failed
}
```

Or with try-with-resources (auto-closes):

```java
try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
    softly.assertThat(user.getName()).isEqualTo("Alice");
    softly.assertThat(user.getAge()).isGreaterThan(0);
}
```

---

## Custom Assertion Messages

Add `.as("description")` before the assertion to label failures clearly:

```java
assertThat(result)
    .as("discount should be applied when order total exceeds $100")
    .isLessThan(originalPrice);
```

---

## Dependency

```xml
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.25.3</version>
    <scope>test</scope>
</dependency>
```

> Spring Boot's `spring-boot-starter-test` includes AssertJ automatically.

---

## Tips

- `assertThat(x).isEqualTo(y)` is always preferred over `assertTrue(x.equals(y))` — the
  failure message is far more helpful.
- Chain multiple assertions on the same subject instead of writing separate `assertThat` calls.
- Use `usingRecursiveComparison()` as the default for domain objects instead of relying on
  `equals()`, unless `equals()` is explicitly tested.
- Use `extracting()` to assert on a field of a list of objects rather than looping manually.
