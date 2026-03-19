# Coverage with JaCoCo

JaCoCo measures how much of your production code is exercised by your tests.
It instruments bytecode at runtime and generates HTML, XML, and CSV reports.

---

## Coverage Metrics

| Metric     | What it measures                            |
| ---------- | ------------------------------------------- |
| **Line**   | Lines of code executed during tests         |
| **Branch** | Both sides of every `if`/`switch` condition |
| **Method** | Methods that were called at least once      |
| **Class**  | Classes that were instantiated or accessed  |

**Aim for:** 80%+ line and branch coverage on business logic. 100% is rarely the goal —
focus on testing behavior, not inflating numbers.

---

## Maven Setup

### 1. Add the plugin to `pom.xml`

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <!-- Instrument classes before tests run -->
                <execution>
                    <id>prepare-agent</id>
                    <goals><goal>prepare-agent</goal></goals>
                </execution>
                <!-- Generate report after tests -->
                <execution>
                    <id>report</id>
                    <phase>verify</phase>
                    <goals><goal>report</goal></goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 2. Run and view report

```bash
mvn verify
# Report: target/site/jacoco/index.html
```

### 3. Enforce a minimum threshold (optional)

Fail the build if coverage drops below a threshold:

```xml
<execution>
    <id>check</id>
    <goals><goal>check</goal></goals>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum> <!-- 80% line coverage required -->
                    </limit>
                    <limit>
                        <counter>BRANCH</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.75</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

---

## Gradle Setup

### 1. Apply the plugin in `build.gradle`

```groovy
plugins {
    id 'java'
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.11"
}

test {
    useJUnitPlatform()
    finalizedBy jacocoTestReport    // always generate report after tests
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true         // needed for SonarQube / CI
        html.required = true        // human-readable
        csv.required = false
    }
}
```

### 2. Run and view report

```bash
./gradlew test jacocoTestReport
# Report: build/reports/jacoco/test/html/index.html
```

### 3. Enforce minimum coverage

```groovy
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = 'LINE'
                value = 'COVEREDRATIO'
                minimum = 0.80
            }
        }
        rule {
            limit {
                counter = 'BRANCH'
                value = 'COVEREDRATIO'
                minimum = 0.75
            }
        }
    }
}

check.dependsOn jacocoTestCoverageVerification
```

---

## Excluding Classes from Coverage

Some classes shouldn't count toward coverage — generated code, config, DTOs, `main()`.

### Maven

```xml
<configuration>
    <excludes>
        <exclude>**/generated/**</exclude>
        <exclude>**/*Config.class</exclude>
        <exclude>**/*Application.class</exclude>
        <exclude>**/dto/**</exclude>
    </excludes>
</configuration>
```

### Gradle

```groovy
jacocoTestReport {
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/generated/**',
                '**/*Config.class',
                '**/*Application.class',
                '**/dto/**'
            ])
        }))
    }
}
```

### Using `@Generated` annotation

For Lombok-generated code, add a `lombok.config` in your project root:

```
lombok.addLombokGeneratedAnnotation = true
```

JaCoCo will automatically skip methods annotated with `@Generated`.

---

## Reading the HTML Report

Open `index.html` in a browser. Color coding:

| Color     | Meaning                           |
| --------- | --------------------------------- |
| 🟢 Green  | Fully covered                     |
| 🟡 Yellow | Partially covered (branch missed) |
| 🔴 Red    | Not covered at all                |

Click through packages → classes → methods to see exactly which lines and branches are missing.

---

## CI/CD Integration Tips

- Commit the `pom.xml` / `build.gradle` with thresholds so coverage is enforced on every PR.
- For GitHub Actions, use the `jacoco-report` action to post coverage as a PR comment.
- For SonarQube, point it at `target/site/jacoco/jacoco.xml` (Maven) or
  `build/reports/jacoco/test/jacocoTestReport.xml` (Gradle).
- Treat coverage drops as failing the build, not just a warning.

---

## Coverage Anti-Patterns to Avoid

| Anti-pattern                                | Problem                                                      |
| ------------------------------------------- | ------------------------------------------------------------ |
| Writing tests just to hit a percentage      | Creates meaningless tests with no assertions                 |
| 100% coverage as the goal                   | Wastes time on boilerplate; branch coverage is more valuable |
| Including generated/Lombok code in coverage | Inflates denominator — exclude it                            |
| Skipping branch coverage                    | Line coverage misses `if` paths that could have bugs         |
