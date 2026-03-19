# Real-World Test Patterns

Concrete, production-style examples for the most common Java class types.
Pick the pattern that matches the class you are testing.

---

## Pattern 1: Pure Utility / Static Helper Class

**When to use:** Class has no dependencies; all methods are stateless transformations.

**Source:**

```java
public class StringUtils {
    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
```

**Test:**

```java
class StringUtilsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void isBlank_whenNullOrWhitespace_thenReturnsTrue(String input) {
        assertTrue(StringUtils.isBlank(input));
    }

    @ParameterizedTest
    @CsvSource({"hello, Hello", "WORLD, World", "jAVA, Java"})
    void capitalize_givenMixedCase_thenNormalized(String input, String expected) {
        assertEquals(expected, StringUtils.capitalize(input));
    }

    @Test
    void capitalize_whenNull_thenReturnsNull() {
        assertNull(StringUtils.capitalize(null));
    }

    @Test
    void capitalize_whenEmpty_thenReturnsEmpty() {
        assertEquals("", StringUtils.capitalize(""));
    }
}
```

---

## Pattern 2: Service Layer (with Mocked Repository)

**When to use:** Class has injected dependencies that should not be real in a unit test.

**Source:**

```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public User createUser(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        User user = new User(name, email);
        User saved = userRepository.save(user);
        emailService.sendWelcome(email);
        return saved;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }
}
```

**Test:**

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService subject;

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("saves and returns user when email is new")
        void givenNewEmail_thenSavesAndReturnsUser() {
            // Arrange
            String name = "Alice";
            String email = "alice@example.com";
            User saved = new User(name, email);
            saved.setId(1L);

            when(userRepository.existsByEmail(email)).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(saved);

            // Act
            User result = subject.createUser(name, email);

            // Assert
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Alice");
            verify(emailService).sendWelcome(email);
        }

        @Test
        @DisplayName("throws when email is already registered")
        void givenExistingEmail_thenThrows() {
            when(userRepository.existsByEmail("dupe@example.com")).thenReturn(true);

            assertThatThrownBy(() -> subject.createUser("Bob", "dupe@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");

            verify(userRepository, never()).save(any());
            verify(emailService, never()).sendWelcome(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns user when found")
        void givenExistingId_thenReturnsUser() {
            User user = new User("Alice", "alice@example.com");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            User result = subject.findById(1L);

            assertThat(result.getName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("throws UserNotFoundException when not found")
        void givenMissingId_thenThrows() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subject.findById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
        }
    }
}
```

---

## Pattern 3: Value Object / Domain Model

**When to use:** Class encapsulates a concept with validation, equality, and computed properties.

**Source:**

```java
public class Money {
    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Amount must be non-negative");
        if (currency == null)
            throw new IllegalArgumentException("Currency is required");
        this.amount = amount;
        this.currency = currency;
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw new IllegalArgumentException("Cannot add different currencies");
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

**Test:**

```java
class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void constructor_whenValid_thenCreates() {
        Money money = new Money(new BigDecimal("10.00"), USD);
        assertThat(money.getAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void constructor_whenAmountNull_thenThrows() {
        assertThatThrownBy(() -> new Money(null, USD))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_whenAmountNegative_thenThrows() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-1.00"), USD))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_whenCurrencyNull_thenThrows() {
        assertThatThrownBy(() -> new Money(BigDecimal.ONE, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void add_whenSameCurrency_thenReturnsSum() {
        Money a = new Money(new BigDecimal("5.00"), USD);
        Money b = new Money(new BigDecimal("3.50"), USD);

        Money result = a.add(b);

        assertThat(result.getAmount()).isEqualByComparingTo("8.50");
        assertThat(result.getCurrency()).isEqualTo(USD);
    }

    @Test
    void add_whenDifferentCurrencies_thenThrows() {
        Money usd = new Money(BigDecimal.ONE, USD);
        Money eur = new Money(BigDecimal.ONE, EUR);

        assertThatThrownBy(() -> usd.add(eur))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("currencies");
    }
}
```

---

## Pattern 4: Collections-Returning Methods

**When to use:** Method filters, maps, or groups a collection. AssertJ shines here.

**Source:**

```java
public class ProductCatalog {
    private final List<Product> products;

    public ProductCatalog(List<Product> products) {
        this.products = products;
    }

    public List<Product> findByCategory(String category) {
        return products.stream()
            .filter(p -> p.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }
}
```

**Test:**

```java
class ProductCatalogTest {

    private ProductCatalog catalog;

    @BeforeEach
    void setUp() {
        List<Product> products = List.of(
            new Product("Widget",  "Electronics"),
            new Product("Gadget",  "Electronics"),
            new Product("T-Shirt", "Apparel")
        );
        catalog = new ProductCatalog(products);
    }

    @Test
    void findByCategory_whenMatchExists_thenReturnsMatches() {
        List<Product> result = catalog.findByCategory("electronics");

        assertThat(result)
            .hasSize(2)
            .extracting(Product::getName)
            .containsExactlyInAnyOrder("Widget", "Gadget");
    }

    @Test
    void findByCategory_whenNoMatch_thenReturnsEmpty() {
        List<Product> result = catalog.findByCategory("Sports");

        assertThat(result).isEmpty();
    }

    @Test
    void findByCategory_isCaseInsensitive() {
        assertThat(catalog.findByCategory("ELECTRONICS")).hasSize(2);
        assertThat(catalog.findByCategory("Electronics")).hasSize(2);
    }
}
```

---

## Pattern 5: Exception and Error Path Testing

**When to use:** Any method that throws checked or unchecked exceptions.

```java
@Test
void withdraw_whenInsufficientFunds_thenThrowsWithDetails() {
    Account account = new Account(new BigDecimal("50.00"));

    InsufficientFundsException ex = assertThrows(
        InsufficientFundsException.class,
        () -> account.withdraw(new BigDecimal("100.00"))
    );

    assertThat(ex.getShortfall()).isEqualByComparingTo("50.00");
    assertThat(ex.getMessage()).contains("insufficient funds");
}
```

---

## Common Mistakes to Avoid

| Anti-pattern | Better approach |
|---|---|
| One `@Test` asserting multiple unrelated things | Split into separate tests per behavior |
| Tests that rely on each other's side effects | Each test sets up its own state in `@BeforeEach` |
| Mocking the class under test itself | Only mock its dependencies |
| `assertTrue(result != null)` | `assertNotNull(result)` or `assertThat(result).isNotNull()` |
| Catching exceptions with try/catch and `fail()` | `assertThrows(...)` or `assertThatThrownBy(...)` |
| Printing to stdout in tests | Remove all `System.out.println` calls |
| Testing private methods directly | Test through the public API; refactor if needed |