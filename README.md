# Java Unit Test Writer — Claude Skill

A Claude skill that generates clean, idiomatic **JUnit 5** unit tests for Java classes.
Upload a `.java` file (or paste code), and Claude will analyze it, plan test cases, and
produce a ready-to-run test file following industry best practices.

Supports: utility classes, service layers with Mockito mocks, value objects, collections,
parameterized tests, AssertJ assertions, and JaCoCo coverage setup.

---

## What This Skill Does

Given a Java class like this:

```java
public class OrderService {
    private final OrderRepository repo;
    public Order createOrder(String customerId, List<Item> items) { ... }
    public Order findById(Long id) { ... }
}
```

Claude will produce a complete, runnable test file:

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository repo;
    @InjectMocks private OrderService subject;

    @Test
    void createOrder_whenValidInput_thenSavesAndReturns() { ... }

    @Test
    void findById_whenNotFound_thenThrowsOrderNotFoundException() { ... }
}
```

See the [`examples/`](examples/) folder for complete input → output walkthroughs.

---

## Quick Setup

### For VS Code Users

1. **Install Claude for VS Code extension** from the VS Code marketplace
2. **Clone and install this skill:**

```bash
# Clone the repository
git clone https://github.com/chuls50/java25-unit-test-creator.git
cd java25-unit-test-creator

# Install Claude CLI (if not already installed)
npm install -g @anthropic-ai/claude-cli

# Add the skill to Claude Code
claude skills add .

# Verify installation
claude skills list
```

3. **Open your Java project in VS Code**
4. **Start using:** Upload a `.java` file to Claude chat and ask for unit tests

### For IntelliJ IDEA Users

1. **Install Claude plugin** from IntelliJ marketplace (or use Claude CLI)
2. **Clone this repository:**

```bash
git clone https://github.com/chuls50/java25-unit-test-creator.git
```

3. **Install the skill (if using Claude CLI):**

```bash
cd java25-unit-test-creator
claude skills add .
```

4. **Setup your workflow:**
   - Open this `java25-unit-test-creator` project in IntelliJ alongside your main Java project
   - Generated tests will appear in the `outputs/` folder
   - Drag tests from `outputs/` to your main project's `src/test/java/` directory

### Verification

The skill activates automatically when you:
- Mention "unit tests", "JUnit", or "write tests" for Java code
- Upload a `.java` file and ask about testing
- Request test generation for specific methods or classes

---

## How to Use It

### Basic Usage — Upload a File

1. Open Claude in your IDE (VS Code or IntelliJ with Claude plugin)
2. Attach your `.java` source file
3. Type any of these prompts:

```
"Write unit tests for this class"
"Generate JUnit 5 tests for UserService.java"
"Add tests for the calculateDiscount method"
"Write tests — focus on edge cases and null handling"
```

### Paste Code Inline

```
Write unit tests for this:

public class StringUtils {
    public static boolean isBlank(String s) { ... }
}
```

### Be More Specific

```
"Write tests for OrderService.java using Mockito for the repository.
 Focus on the createOrder and cancel methods. Include parameterized
 tests for the discount tiers."
```

---

## Where Do the Generated Tests Go?

When the skill runs inside your IDE via Claude, it writes the generated test
file directly to the `outputs/` folder at the root of this repo:

```
java25-unit-test-creator/
└── outputs/
    └── OrderServiceTest.java    ← Claude writes it here
```

Once it appears there, move it into your Java project following standard
Maven/Gradle structure:

```
your-java-project/
└── src/
    └── test/
        └── java/
            └── com/
                └── example/
                    └── OrderServiceTest.java   ← move it here
```

**In IntelliJ:**

1. Open the `outputs/` folder in the Project panel
2. Right-click `OrderServiceTest.java` → Copy
3. Navigate to `src/test/java/<your-package>/`
4. Paste — IntelliJ will compile it automatically
5. Click the green ▶ next to the class name to run it

**In VS Code:**

1. Open the `outputs/` folder in the Explorer panel
2. Copy the generated `*Test.java` file
3. Navigate to your project's `src/test/java/<your-package>/` folder
4. Paste the file
5. Use the Test Explorer or run `mvn test` / `./gradlew test` in terminal

> The `outputs/` folder is intentionally empty in the repo. It is held in git
> by a `.gitkeep` file and is where every generated test lands before you move it.

---

## Java Project Dependencies

Add these to your `pom.xml` (or `build.gradle`) **before running the generated tests**.
If you use Spring Boot, `spring-boot-starter-test` already includes all three — skip this.

### Maven (`pom.xml`)

```xml
<dependencies>
    <!-- JUnit 5 — test runner -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.2</version>
        <scope>test</scope>
    </dependency>

    <!-- AssertJ — fluent, readable assertions -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.25.3</version>
        <scope>test</scope>
    </dependency>

    <!-- Mockito — mocking dependencies (add only if your class has injected deps) -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <version>5.11.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<!-- Required to run JUnit 5 with mvn test -->
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.5</version>
        </plugin>
    </plugins>
</build>
```

### Gradle (`build.gradle`)

```groovy
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
    testImplementation 'org.assertj:assertj-core:3.25.3'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.11.0'
}

test {
    useJUnitPlatform()
}
```

---

## Project Structure

```
java25-unit-test-creator/
├── README.md                        ← you are here
├── SKILL.md                         ← Claude's main instructions
├── references/
│   ├── annotations.md               ← JUnit 5 annotation reference (@Nested, @Tag, etc.)
│   ├── assertj.md                   ← AssertJ fluent assertion patterns
│   ├── coverage.md                  ← JaCoCo setup for Maven and Gradle
│   ├── mockito.md                   ← Mockito mocking patterns + BDD style
│   ├── parameterized.md             ← All @ParameterizedTest source types
│   └── patterns.md                  ← Real-world patterns (service, POJO, utility, etc.)
└── examples/
    ├── 01-utility-class/            ← StringUtils input + expected test output
    ├── 02-service-layer/            ← OrderService (with Mockito) input + output
    └── 03-value-object/             ← Money value object input + output
```

---

## Sharing with Your Team

Send your team this repo link and tell them:

1. **Install the skill** using the instructions above (Claude Code CLI or claude.ai Settings)
2. **Add the Maven/Gradle dependencies** to their project's build file (one-time setup)
3. **Upload a `.java` file** and type `"Write unit tests for this class"`
4. **Download the output** and drop it into `src/test/java/<their-package>/`
5. **Run** `mvn test` or `./gradlew test` — tests should pass immediately on a correct class

The `examples/` folder shows exactly what input → output looks like so they know what to expect.

---

## Tips for Best Results

- **Be specific** in your prompts. Mention Mockito, edge cases, parameterized tests, or specific methods you care about.
- **Dependency injection** is detected automatically. If your class has `@Autowired` or constructor-injected dependencies, Claude will use Mockito without being asked.
- **Large classes** (10+ methods): Ask Claude to focus on one section at a time:
  `"Write tests for just the validation methods in UserService.java"`
- **Ask for explanations**: `"Explain why you used @ParameterizedTest here instead of separate @Test methods"`
- **Java 25 features**: The skill leverages modern Java patterns including records, sealed classes, pattern matching, and switch expressions.

---

## Test Quality Standards

Generated tests follow industry best practices:

- **Descriptive Names** — `methodName_whenCondition_thenExpectedResult` pattern
- **AAA Structure** — Arrange, Act, Assert organization
- **Comprehensive Coverage** — Happy path, edge cases, and error scenarios
- **Proper Mocking** — Minimal, focused mocks with verification
- **Readable Assertions** — Fluent AssertJ assertions for clarity
- **Modern Java** — Uses Java 25 features when appropriate

---

## Reference Documentation

All the detailed patterns and options Claude uses internally are in readable reference files
in the `references/` folder. Browse them to understand what's available or to learn
JUnit 5 / Mockito / AssertJ best practices.

---

## Contributing

Contributions are welcome! This project uses a skill creation framework for extending
test generation capabilities. See the skill creation guidelines for details.

## License

This project is open source under the MIT License.
