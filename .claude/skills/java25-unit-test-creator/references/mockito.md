# Mockito Reference

Use Mockito to replace injected dependencies with controlled fakes so the class under test
can be isolated from databases, HTTP clients, email services, and other external systems.

---

## Setup

```java
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PaymentGateway paymentGateway;   // dependency of OrderService

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService subject;            // Mockito injects the @Mocks above
}
```

---

## Core Stubbing

### Stubbing return values

```java
when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
when(paymentGateway.charge(any())).thenReturn(new Receipt("ok"));
when(userRepo.existsByEmail("test@test.com")).thenReturn(true);
```

### Stubbing void methods

```java
doNothing().when(emailService).send(anyString());
doThrow(new RuntimeException("timeout")).when(emailService).send("bad@email.com");
```

### Stubbing to throw

```java
when(repo.save(any())).thenThrow(new DataIntegrityViolationException("constraint"));
```

### Stubbing with multiple calls (first, then second, then …)

```java
when(service.getStatus())
    .thenReturn(Status.PENDING)
    .thenReturn(Status.PROCESSING)
    .thenReturn(Status.COMPLETE);
```

---

## BDD-Style Stubbing (Preferred in Given-When-Then tests)

Use `BDDMockito` aliases when your tests follow a Given/When/Then structure.
`given(...)` reads more naturally in the Arrange section.

```java
import static org.mockito.BDDMockito.*;

// Arrange
given(userRepository.findById(1L)).willReturn(Optional.of(user));
given(paymentGateway.charge(any())).willReturn(new Receipt("ok"));
willDoNothing().given(emailService).send(anyString());
willThrow(new RuntimeException()).given(emailService).send("bad@email.com");

// Act
User result = subject.getUser(1L);

// Assert
then(emailService).should().send(user.getEmail());
then(orderRepository).should(never()).save(any());
```

---

## Argument Matchers

Use matchers when the exact value doesn't matter, or to match any instance of a type.

```java
any()                          // any non-null Object
any(User.class)                // any non-null User
anyString()                    // any non-null String
anyInt(), anyLong()            // any primitive
eq("specific value")           // exact match (required alongside other matchers)
isNull()                       // explicit null
isNotNull()                    // explicit non-null
argThat(u -> u.getAge() > 18)  // custom predicate
```

> **Rule:** If you use a matcher for one argument, all arguments in that call must use matchers.

```java
// Wrong — mixing literal and matcher
when(repo.update(user, 1L)).thenReturn(saved);

// Right — wrap literal in eq()
when(repo.update(any(User.class), eq(1L))).thenReturn(saved);
```

---

## Verification

### Basic verify

```java
verify(emailService).send("alice@example.com");    // called exactly once
verify(emailService, times(2)).send(anyString());  // called exactly twice
verify(emailService, never()).send(anyString());   // never called
verify(emailService, atLeastOnce()).send(any());   // called one or more times
verify(emailService, atMost(3)).send(any());       // called at most 3 times
```

### Verify no more interactions

```java
verifyNoMoreInteractions(emailService);
verifyNoInteractions(emailService, smsService); // neither was touched
```

### Argument Captor — inspect what was passed

```java
ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

subject.createUser("Alice", "alice@example.com");

verify(userRepository).save(captor.capture());
User savedUser = captor.getValue();

assertThat(savedUser.getName()).isEqualTo("Alice");
assertThat(savedUser.getEmail()).isEqualTo("alice@example.com");
```

---

## Spying (Partial Mocks)

Use `@Spy` to wrap a real object — all real methods are called unless explicitly stubbed.
Use sparingly; prefer full mocks for clean isolation.

```java
@Spy
private List<String> spyList = new ArrayList<>();

@Test
void spy_example() {
    spyList.add("one");
    spyList.add("two");

    verify(spyList, times(2)).add(anyString());
    assertEquals(2, spyList.size()); // real method was called
}
```

---

## Common Pitfalls

| Problem                                  | Fix                                                                                                    |
| ---------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| `@InjectMocks` doesn't inject            | Check for constructor ambiguity — construct manually in `@BeforeEach` and pass mocks as args           |
| Stubbing unreachable code path           | The real code path must call the stubbed method for the stub to matter                                 |
| `UnnecessaryStubbingException`           | Remove stubs that are never triggered — Mockito strict mode (default in JUnit 5 extension) flags these |
| Verifying interactions on the wrong mock | Double-check which field name the `@InjectMocks` class uses                                            |
| Mocking the class under test             | Never do this — only mock its _dependencies_                                                           |

---

## Quick Cheat Sheet

```java
// Setup
@ExtendWith(MockitoExtension.class)
@Mock Dependency dep;
@InjectMocks SubjectClass subject;

// Stub
when(dep.method()).thenReturn(value);
given(dep.method()).willReturn(value);  // BDD style

// Throw
when(dep.method()).thenThrow(new SomeException());

// Void
doNothing().when(dep).voidMethod();
doThrow(ex).when(dep).voidMethod();

// Verify
verify(dep).method();
verify(dep, never()).method();
verify(dep, times(2)).method();

// Capture
ArgumentCaptor<Type> captor = ArgumentCaptor.forClass(Type.class);
verify(dep).method(captor.capture());
captor.getValue(); // inspect the argument
```
