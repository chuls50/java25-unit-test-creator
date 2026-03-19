# Example 2 — Service Layer (Mockito + @Nested)

## Class Type

Service layer — has three constructor-injected dependencies that must be mocked.

## Files

| File                    | Description                       |
| ----------------------- | --------------------------------- |
| `OrderService.java`     | The source class Claude was given |
| `OrderServiceTest.java` | The test file Claude generated    |

## Prompt Used

```
Write unit tests for OrderService.java using Mockito.
Use @Nested to group tests by method and BDD-style given/when/then.
```

## What Claude Did

1. **Classified** the class as a service layer — detected three constructor dependencies
   (`OrderRepository`, `InventoryService`, `NotificationService`) and applied Mockito.
2. **Used `@ExtendWith(MockitoExtension.class)`** with `@Mock` + `@InjectMocks`.
3. **Used BDD-style** `given(...).willReturn(...)` and `then(...).should()` from `BDDMockito`.
4. **Grouped with `@Nested`** — one inner class per public method (`CreateOrder`, `FindById`,
   `CancelOrder`) so the test report reads like a specification.
5. **Used `ArgumentCaptor`** to inspect the exact object passed to `orderRepository.save()`.
6. **Verified negative interactions** — confirmed `notificationService` was never called
   when an exception was thrown or when the order was already cancelled.

## Key Techniques Demonstrated

- `@Nested` + `@DisplayName` for human-readable test reports
- `ArgumentCaptor` for asserting on saved objects
- `then(mock).shouldHaveNoInteractions()` for negative verification
- `willAnswer(i -> i.getArgument(0))` to return the exact argument passed in

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
mvn test -Dtest=OrderServiceTest
# or
./gradlew test --tests "com.example.order.OrderServiceTest"
```
