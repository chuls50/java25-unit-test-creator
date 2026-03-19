# JUnit 5 Annotations Reference

A complete reference for JUnit 5 annotations. The most commonly used are marked **★**.

---

## Test Methods

| Annotation           | Purpose                                                                                          |
| -------------------- | ------------------------------------------------------------------------------------------------ |
| **★ `@Test`**        | Marks a method as a test. Return type must be `void`.                                            |
| `@RepeatedTest(n)`   | Runs the test `n` times. Inject `RepetitionInfo` to vary behavior.                               |
| `@ParameterizedTest` | Runs the test multiple times with different arguments. See [parameterized.md](parameterized.md). |
| `@TestFactory`       | Declares a factory for dynamic tests (returns `Stream<DynamicTest>`).                            |
| `@TestTemplate`      | Marks a method as a template for test cases — advanced extension use.                            |

---

## Lifecycle Hooks

| Annotation          | Runs                               | Notes                                                |
| ------------------- | ---------------------------------- | ---------------------------------------------------- |
| **★ `@BeforeEach`** | Before every `@Test`               | Instance setup (create subject, init mocks)          |
| **★ `@AfterEach`**  | After every `@Test`                | Cleanup after each test                              |
| `@BeforeAll`        | Once before all tests in the class | Must be `static` (unless `@TestInstance(PER_CLASS)`) |
| `@AfterAll`         | Once after all tests in the class  | Must be `static` (unless `@TestInstance(PER_CLASS)`) |

**Typical setup:**

```java
class MyServiceTest {
    private MyService subject;

    @BeforeEach
    void setUp() {
        subject = new MyService();
    }

    @AfterEach
    void tearDown() {
        // optional: clear state, close resources
    }
}
```

---

## Organization and Grouping

### @Nested — Hierarchical Test Structure ★

Use `@Nested` to group tests that share setup or describe behavior by scenario.
Especially useful for "when X ... then Y" structures.

```java
class OrderServiceTest {

    @Nested
    @DisplayName("when order is empty")
    class WhenOrderIsEmpty {

        @Test
        @DisplayName("returns zero total")
        void total_isZero() {
            assertEquals(BigDecimal.ZERO, new Order().total());
        }
    }

    @Nested
    @DisplayName("when order has items")
    class WhenOrderHasItems {

        private Order order;

        @BeforeEach
        void setUp() {
            order = new Order();
            order.add(new Item("Widget", new BigDecimal("9.99")));
        }

        @Test
        @DisplayName("returns correct total")
        void total_isCorrect() {
            assertEquals(new BigDecimal("9.99"), order.total());
        }

        @Test
        @DisplayName("returns correct item count")
        void itemCount_isOne() {
            assertEquals(1, order.itemCount());
        }
    }
}
```

**Rules:**

- `@Nested` classes must be non-static inner classes.
- `@BeforeAll` / `@AfterAll` require `@TestInstance(Lifecycle.PER_CLASS)` inside a nested class.
- Nesting can be multiple levels deep.

---

### @DisplayName ★

Provides a human-readable label shown in IDE test runners and reports.
Use spaces, `when/then` language, special characters — anything that reads clearly.

```java
@Test
@DisplayName("should throw when order total is negative")
void total_negative_throws() { ... }

@Nested
@DisplayName("Given a valid user")
class GivenAValidUser { ... }
```

---

### @Tag — Filter and Categorize Tests

Use tags to group tests by type, component, or speed — then run subsets in CI.

```java
@Tag("fast")
@Tag("unit")
@Test
void someQuickTest() { ... }

@Tag("slow")
@Tag("integration")
@Test
void someSlowTest() { ... }
```

**Running a tag subset with Maven:**

```bash
mvn test -Dgroups="fast"
mvn test -DexcludedGroups="slow"
```

**In `pom.xml` (Surefire):**

```xml
<configuration>
    <groups>fast</groups>
    <excludedGroups>slow,integration</excludedGroups>
</configuration>
```

---

## Conditional Execution

### @Disabled ★

Skips the test with an optional reason. Always add a reason explaining why.

```java
@Test
@Disabled("Temporarily disabled — see JIRA-1234")
void featureUnderDevelopment() { ... }
```

### Conditional Annotations

```java
@EnabledOnOs(OS.LINUX)             // only on Linux
@DisabledOnOs(OS.WINDOWS)          // skip on Windows
@EnabledOnJre(JRE.JAVA_17)         // only on Java 17
@EnabledForJreRange(min = JRE.JAVA_11, max = JRE.JAVA_17)
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
@EnabledIfSystemProperty(named = "feature.flag", matches = "enabled")
```

---

## Timeout

Fail a test if it exceeds a time limit. Useful for detecting infinite loops or unexpected blocking.

```java
@Test
@Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
void shouldCompleteWithinHalfASecond() {
    subject.performOperation();
}

// Applies to all tests in the class
@Timeout(5)
class PerformanceSensitiveTest {
    // All @Test methods must complete within 5 seconds
}
```

Using `assertTimeoutPreemptively` from JUnit Assertions:

```java
@Test
void asyncOperation_completesInTime() {
    assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
        service.runAsync();
    });
}
```

---

## Test Ordering

By default, JUnit 5 runs tests in a deterministic but unspecified order. To enforce order:

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderedTest {

    @Test
    @Order(1)
    void firstStep() { ... }

    @Test
    @Order(2)
    void secondStep() { ... }
}
```

Other orderers: `MethodOrderer.MethodName`, `MethodOrderer.DisplayName`,
`MethodOrderer.Random`.

> **Warning:** Tests should not depend on each other. Use ordering only for
> readability, not to pass state between tests.

---

## Test Instance Lifecycle

By default, JUnit creates a new test class instance before each test method.
Use `@TestInstance(PER_CLASS)` to share state across tests in the same class —
useful for expensive setup like parsing a large file.

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SharedStateTest {

    private final Database db = new Database(); // created once

    @BeforeAll
    void initDb() {             // does NOT need to be static
        db.connect();
    }

    @AfterAll
    void cleanUp() {
        db.disconnect();
    }
}
```

---

## Extensions

Register extensions that hook into the test lifecycle — for example, Mockito uses this.

```java
// Declarative
@ExtendWith(MockitoExtension.class)
class MyTest { ... }

// Multiple extensions
@ExtendWith({MockitoExtension.class, SpringExtension.class})
class MyTest { ... }
```

---

## Assumptions

Tests are skipped (not failed) when an assumption is not met.
Useful for environment-specific tests.

```java
import static org.junit.jupiter.api.Assumptions.*;

@Test
void onlyRunsInCi() {
    assumeTrue("true".equals(System.getenv("CI")), "Skipping: not running in CI");
    // test only runs if CI=true
}
```
