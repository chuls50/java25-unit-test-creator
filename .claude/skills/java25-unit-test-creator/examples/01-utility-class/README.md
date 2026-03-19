# Example 1 — Utility Class (No Dependencies)

## Class Type

Static utility — no constructor, no injected dependencies, pure logic only.

## Files

| File                   | Description                       |
| ---------------------- | --------------------------------- |
| `StringUtils.java`     | The source class Claude was given |
| `StringUtilsTest.java` | The test file Claude generated    |

## Prompt Used

```
Write unit tests for this class.
```

## What Claude Did

1. **Classified** the class as a static utility — no mocking needed, no `@BeforeEach` setup.
2. **Planned** tests for all four public methods: `isBlank`, `capitalize`, `truncate`, `reverse`.
3. **Used `@ParameterizedTest`** with `@NullAndEmptySource` + `@ValueSource` for `isBlank`
   and `@CsvSource` for input/output pairs in `capitalize` and `reverse`.
4. **Covered edge cases**: null input, empty string, exact boundary length, negative `maxLength`.
5. **Named tests** using the `method_whenCondition_thenResult` convention.

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
                        └── <ClassName>Test.java   ← here

## Run It

```bash
mvn test -Dtest=StringUtilsTest
# or
./gradlew test --tests "com.example.util.StringUtilsTest"
```
