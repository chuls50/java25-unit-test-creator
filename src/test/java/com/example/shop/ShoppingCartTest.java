package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShoppingCart")
class ShoppingCartTest {

    @Mock
    private ShoppingCart.DiscountService discountService;

    private ShoppingCart cart;
    private final String customerId = "customer123";

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart(customerId, discountService);
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {

        @Test
        void constructor_whenValidCustomerId_thenCreatesCart() {
            // Arrange & Act
            ShoppingCart newCart = new ShoppingCart("valid-id", discountService);

            // Assert
            assertThat(newCart.getCustomerId()).isEqualTo("valid-id");
            assertThat(newCart.isEmpty()).isTrue();
            assertThat(newCart.getAppliedDiscountCode()).isNull();
        }

        @Test
        void constructor_whenCustomerIdIsNull_thenThrowsIllegalArgumentException() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new ShoppingCart(null, discountService))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("customerId must not be blank");
        }

        @Test
        void constructor_whenCustomerIdIsBlank_thenThrowsIllegalArgumentException() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new ShoppingCart("   ", discountService))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("customerId must not be blank");
        }

        @Test
        void constructor_whenCustomerIdIsEmpty_thenThrowsIllegalArgumentException() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new ShoppingCart("", discountService))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("customerId must not be blank");
        }
    }

    @Nested
    @DisplayName("Adding Items")
    class AddItemTest {

        @Test
        void addItem_whenNewProduct_thenAddsToCart() {
            // Arrange
            String productId = "P001";
            String productName = "Laptop";
            BigDecimal price = new BigDecimal("999.99");
            int quantity = 2;

            // Act
            cart.addItem(productId, productName, price, quantity);

            // Assert
            List<ShoppingCart.CartItem> items = cart.getItems();
            assertThat(items).hasSize(1);
            
            ShoppingCart.CartItem item = items.get(0);
            assertThat(item.getProductId()).isEqualTo(productId);
            assertThat(item.getProductName()).isEqualTo(productName);
            assertThat(item.getPrice()).isEqualByComparingTo(price);
            assertThat(item.getQuantity()).isEqualTo(quantity);
        }

        @Test
        void addItem_whenProductAlreadyExists_thenIncrementsQuantity() {
            // Arrange
            String productId = "P001";
            BigDecimal price = new BigDecimal("50.00");
            cart.addItem(productId, "Mouse", price, 3);

            // Act
            cart.addItem(productId, "Mouse", price, 2);

            // Assert
            List<ShoppingCart.CartItem> items = cart.getItems();
            assertThat(items).hasSize(1);
            assertThat(items.get(0).getQuantity()).isEqualTo(5);
            assertThat(items.get(0).getLineTotal()).isEqualByComparingTo(new BigDecimal("250.00"));
        }

        @Test
        void addItem_whenQuantityIsZero_thenThrowsIllegalArgumentException() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> 
                    cart.addItem("P001", "Product", new BigDecimal("10.00"), 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity must be at least 1");
        }

        @Test
        void addItem_whenQuantityIsNegative_thenThrowsIllegalArgumentException() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> 
                    cart.addItem("P001", "Product", new BigDecimal("10.00"), -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity must be at least 1");
        }

        @Test
        void addItem_whenPriceIsNull_thenThrowsIllegalArgumentException() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> 
                    cart.addItem("P001", "Product", null, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Price must be non-negative");
        }

        @Test
        void addItem_whenPriceIsNegative_thenThrowsIllegalArgumentException() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> 
                    cart.addItem("P001", "Product", new BigDecimal("-5.00"), 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Price must be non-negative");
        }

        @Test
        void addItem_whenExceedsMaxQuantity_thenThrowsCartException() {
            // Arrange
            String productId = "P001";
            cart.addItem(productId, "Product", new BigDecimal("10.00"), 50);

            // Act & Assert
            assertThatThrownBy(() -> 
                    cart.addItem(productId, "Product", new BigDecimal("10.00"), 50))
                    .isInstanceOf(ShoppingCart.CartException.class)
                    .hasMessage("Cannot exceed 99 units of product: P001");
        }

        @Test
        void addItem_whenExactlyMaxQuantity_thenSucceeds() {
            // Arrange
            String productId = "P001";
            BigDecimal price = new BigDecimal("10.00");

            // Act
            cart.addItem(productId, "Product", price, 99);

            // Assert
            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(99);
        }

        @Test
        void addItem_whenPriceIsZero_thenSucceeds() {
            // Arrange
            BigDecimal zeroPrice = BigDecimal.ZERO;

            // Act
            cart.addItem("FREE001", "Free Sample", zeroPrice, 1);

            // Assert
            List<ShoppingCart.CartItem> items = cart.getItems();
            assertThat(items).hasSize(1);
            assertThat(items.get(0).getPrice()).isEqualByComparingTo(zeroPrice);
            assertThat(items.get(0).getLineTotal()).isEqualByComparingTo(zeroPrice);
        }
    }

    @Nested
    @DisplayName("Removing Items")
    class RemoveItemTest {

        @Test
        void removeItem_whenProductExists_thenRemovesFromCart() {
            // Arrange
            cart.addItem("P001", "Product1", new BigDecimal("10.00"), 1);
            cart.addItem("P002", "Product2", new BigDecimal("20.00"), 2);

            // Act
            cart.removeItem("P001");

            // Assert
            List<ShoppingCart.CartItem> items = cart.getItems();
            assertThat(items).hasSize(1);
            assertThat(items.get(0).getProductId()).isEqualTo("P002");
        }

        @Test
        void removeItem_whenProductNotFound_thenThrowsCartException() {
            // Arrange
            cart.addItem("P001", "Product1", new BigDecimal("10.00"), 1);

            // Act & Assert
            assertThatThrownBy(() -> cart.removeItem("P999"))
                    .isInstanceOf(ShoppingCart.CartException.class)
                    .hasMessage("Product not found in cart: P999");
        }

        @Test
        void removeItem_whenCartIsEmpty_thenThrowsCartException() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> cart.removeItem("P001"))
                    .isInstanceOf(ShoppingCart.CartException.class)
                    .hasMessage("Product not found in cart: P001");
        }
    }

    @Nested
    @DisplayName("Discount Code Management")
    class DiscountCodeTest {

        @Test
        void applyDiscountCode_whenValidCode_thenAppliesDiscount() {
            // Arrange
            String discountCode = "SAVE10";
            given(discountService.validate(discountCode)).willReturn(10);

            // Act
            cart.applyDiscountCode(discountCode);

            // Assert
            assertThat(cart.getAppliedDiscountCode()).isEqualTo(discountCode);
        }

        @Test
        void applyDiscountCode_whenInvalidCode_thenThrowsInvalidDiscountException() {
            // Arrange
            String invalidCode = "INVALID";
            given(discountService.validate(invalidCode)).willReturn(0);

            // Act & Assert
            assertThatThrownBy(() -> cart.applyDiscountCode(invalidCode))
                    .isInstanceOf(ShoppingCart.InvalidDiscountException.class)
                    .hasMessage("Invalid or expired discount code: INVALID");
        }

        @Test
        void applyDiscountCode_whenNegativeReturn_thenThrowsInvalidDiscountException() {
            // Arrange
            String expiredCode = "EXPIRED";
            given(discountService.validate(expiredCode)).willReturn(-1);

            // Act & Assert
            assertThatThrownBy(() -> cart.applyDiscountCode(expiredCode))
                    .isInstanceOf(ShoppingCart.InvalidDiscountException.class)
                    .hasMessage("Invalid or expired discount code: EXPIRED");
        }

        @Test
        void applyDiscountCode_whenDiscountAlreadyApplied_thenThrowsCartException() {
            // Arrange
            String firstCode = "SAVE10";
            String secondCode = "SAVE20";
            given(discountService.validate(firstCode)).willReturn(10);
            cart.applyDiscountCode(firstCode);

            // Act & Assert
            assertThatThrownBy(() -> cart.applyDiscountCode(secondCode))
                    .isInstanceOf(ShoppingCart.CartException.class)
                    .hasMessage("A discount code is already applied: SAVE10");
        }

        @Test
        void removeDiscountCode_whenDiscountApplied_thenRemovesDiscount() {
            // Arrange
            String discountCode = "SAVE15";
            given(discountService.validate(discountCode)).willReturn(15);
            cart.applyDiscountCode(discountCode);

            // Act
            cart.removeDiscountCode();

            // Assert
            assertThat(cart.getAppliedDiscountCode()).isNull();
        }

        @Test
        void removeDiscountCode_whenNoDiscountApplied_thenNoException() {
            // Arrange & Act
            cart.removeDiscountCode();

            // Assert
            assertThat(cart.getAppliedDiscountCode()).isNull();
        }
    }

    @Nested
    @DisplayName("Calculations")
    class CalculationsTest {

        @Test
        void getSubtotal_whenEmptyCart_thenReturnsZero() {
            // Arrange & Act
            BigDecimal subtotal = cart.getSubtotal();

            // Assert
            assertThat(subtotal).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
        }

        @Test
        void getSubtotal_whenSingleItem_thenReturnsItemTotal() {
            // Arrange
            cart.addItem("P001", "Product", new BigDecimal("25.99"), 3);

            // Act
            BigDecimal subtotal = cart.getSubtotal();

            // Assert
            assertThat(subtotal).isEqualByComparingTo(new BigDecimal("77.97"));
        }

        @Test
        void getSubtotal_whenMultipleItems_thenReturnsSumOfAllItems() {
            // Arrange
            cart.addItem("P001", "Product1", new BigDecimal("10.50"), 2);
            cart.addItem("P002", "Product2", new BigDecimal("15.99"), 1);
            cart.addItem("P003", "Product3", new BigDecimal("5.00"), 4);

            // Act
            BigDecimal subtotal = cart.getSubtotal();

            // Assert
            // 10.50 * 2 + 15.99 * 1 + 5.00 * 4 = 21.00 + 15.99 + 20.00 = 56.99
            assertThat(subtotal).isEqualByComparingTo(new BigDecimal("56.99"));
        }

        @Test
        void getDiscountAmount_whenNoDiscountApplied_thenReturnsZero() {
            // Arrange
            cart.addItem("P001", "Product", new BigDecimal("100.00"), 1);

            // Act
            BigDecimal discountAmount = cart.getDiscountAmount();

            // Assert
            assertThat(discountAmount).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void getDiscountAmount_whenValidDiscountApplied_thenCalculatesCorrectDiscount() {
            // Arrange
            cart.addItem("P001", "Product", new BigDecimal("100.00"), 1);
            String discountCode = "SAVE20";
            given(discountService.validate(discountCode)).willReturn(20);
            cart.applyDiscountCode(discountCode);

            // Act
            BigDecimal discountAmount = cart.getDiscountAmount();

            // Assert
            assertThat(discountAmount).isEqualByComparingTo(new BigDecimal("20.00"));
        }

        @Test
        void getDiscountAmount_whenDiscountAppliedToMultipleItems_thenCalculatesOnSubtotal() {
            // Arrange
            cart.addItem("P001", "Product1", new BigDecimal("50.00"), 2);
            cart.addItem("P002", "Product2", new BigDecimal("25.00"), 1);
            String discountCode = "SAVE10";
            given(discountService.validate(discountCode)).willReturn(10);
            cart.applyDiscountCode(discountCode);

            // Act
            BigDecimal discountAmount = cart.getDiscountAmount();

            // Assert
            // Subtotal: 50.00 * 2 + 25.00 = 125.00
            // Discount: 125.00 * 0.10 = 12.50
            assertThat(discountAmount).isEqualByComparingTo(new BigDecimal("12.50"));
        }

        @Test
        void getTotal_whenEmptyCart_thenReturnsZero() {
            // Arrange & Act
            BigDecimal total = cart.getTotal();

            // Assert
            assertThat(total).isEqualByComparingTo(new BigDecimal("0.00"));
        }

        @Test
        void getTotal_whenNoDiscount_thenReturnsSubtotalPlusTax() {
            // Arrange
            cart.addItem("P001", "Product", new BigDecimal("100.00"), 1);

            // Act
            BigDecimal total = cart.getTotal();

            // Assert
            // Subtotal: 100.00
            // Tax (8%): 8.00
            // Total: 108.00
            assertThat(total).isEqualByComparingTo(new BigDecimal("108.00"));
        }

        @Test
        void getTotal_whenDiscountApplied_thenCalculatesWithDiscountAndTax() {
            // Arrange
            cart.addItem("P001", "Product", new BigDecimal("100.00"), 1);
            String discountCode = "SAVE20";
            given(discountService.validate(discountCode)).willReturn(20);
            cart.applyDiscountCode(discountCode);

            // Act
            BigDecimal total = cart.getTotal();

            // Assert
            // Subtotal: 100.00
            // Discount (20%): 20.00
            // Discounted total: 80.00
            // Tax (8% on 80.00): 6.40
            // Final total: 86.40
            assertThat(total).isEqualByComparingTo(new BigDecimal("86.40"));
        }

        @Test
        void getTotal_whenComplexScenario_thenCalculatesCorrectly() {
            // Arrange
            cart.addItem("P001", "Laptop", new BigDecimal("999.99"), 1);
            cart.addItem("P002", "Mouse", new BigDecimal("29.99"), 2);
            cart.addItem("P003", "Keyboard", new BigDecimal("79.99"), 1);
            String discountCode = "SAVE15";
            given(discountService.validate(discountCode)).willReturn(15);
            cart.applyDiscountCode(discountCode);

            // Act
            BigDecimal total = cart.getTotal();

            // Assert
            // Subtotal: 999.99 + (29.99 * 2) + 79.99 = 1139.96
            // Discount (15%): 170.99
            // Discounted total: 968.97
            // Tax (8%): 77.52
            // Final total: 1046.49
            assertThat(total).isEqualByComparingTo(new BigDecimal("1046.49"));
        }
    }

    @Nested
    @DisplayName("Cart State Queries")
    class CartStateTest {

        @Test
        void isEmpty_whenNewCart_thenReturnsTrue() {
            // Arrange & Act
            boolean isEmpty = cart.isEmpty();

            // Assert
            assertThat(isEmpty).isTrue();
        }

        @Test
        void isEmpty_whenItemsAdded_thenReturnsFalse() {
            // Arrange
            cart.addItem("P001", "Product", new BigDecimal("10.00"), 1);

            // Act
            boolean isEmpty = cart.isEmpty();

            // Assert
            assertThat(isEmpty).isFalse();
        }

        @Test
        void isEmpty_whenItemsAddedThenRemoved_thenReturnsTrue() {
            // Arrange
            cart.addItem("P001", "Product", new BigDecimal("10.00"), 1);
            cart.removeItem("P001");

            // Act
            boolean isEmpty = cart.isEmpty();

            // Assert
            assertThat(isEmpty).isTrue();
        }

        @Test
        void getTotalItemCount_whenEmptyCart_thenReturnsZero() {
            // Arrange & Act
            int count = cart.getTotalItemCount();

            // Assert
            assertThat(count).isEqualTo(0);
        }

        @Test
        void getTotalItemCount_whenSingleItem_thenReturnsQuantity() {
            // Arrange
            cart.addItem("P001", "Product", new BigDecimal("10.00"), 5);

            // Act
            int count = cart.getTotalItemCount();

            // Assert
            assertThat(count).isEqualTo(5);
        }

        @Test
        void getTotalItemCount_whenMultipleItems_thenReturnsTotalQuantity() {
            // Arrange
            cart.addItem("P001", "Product1", new BigDecimal("10.00"), 3);
            cart.addItem("P002", "Product2", new BigDecimal("20.00"), 2);
            cart.addItem("P003", "Product3", new BigDecimal("15.00"), 4);

            // Act
            int count = cart.getTotalItemCount();

            // Assert
            assertThat(count).isEqualTo(9); // 3 + 2 + 4
        }

        @Test
        void getCustomerId_thenReturnsOriginalCustomerId() {
            // Arrange & Act
            String returnedId = cart.getCustomerId();

            // Assert
            assertThat(returnedId).isEqualTo(customerId);
        }

        @Test
        void getAppliedDiscountCode_whenNoDiscountApplied_thenReturnsNull() {
            // Arrange & Act
            String code = cart.getAppliedDiscountCode();

            // Assert
            assertThat(code).isNull();
        }

        @Test
        void getAppliedDiscountCode_whenDiscountApplied_thenReturnsCode() {
            // Arrange
            String discountCode = "SAVE25";
            given(discountService.validate(discountCode)).willReturn(25);
            cart.applyDiscountCode(discountCode);

            // Act
            String code = cart.getAppliedDiscountCode();

            // Assert
            assertThat(code).isEqualTo(discountCode);
        }

        @Test
        void getItems_whenEmptyCart_thenReturnsEmptyList() {
            // Arrange & Act
            List<ShoppingCart.CartItem> items = cart.getItems();

            // Assert
            assertThat(items).isEmpty();
        }

        @Test
        void getItems_whenItemsAdded_thenReturnsUnmodifiableList() {
            // Arrange
            cart.addItem("P001", "Product", new BigDecimal("10.00"), 1);

            // Act
            List<ShoppingCart.CartItem> items = cart.getItems();

            // Assert
            assertThat(items).hasSize(1);
            assertThatThrownBy(() -> items.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void getItems_whenMultipleItems_thenReturnsAllItems() {
            // Arrange
            cart.addItem("P001", "Laptop", new BigDecimal("1000.00"), 1);
            cart.addItem("P002", "Mouse", new BigDecimal("50.00"), 2);

            // Act
            List<ShoppingCart.CartItem> items = cart.getItems();

            // Assert
            assertThat(items)
                    .hasSize(2)
                    .extracting("productId")
                    .containsExactly("P001", "P002");
        }
    }

    @Nested
    @DisplayName("CartItem")
    class CartItemTest {

        @Test
        void cartItem_whenCreated_thenHasCorrectProperties() {
            // Arrange
            String productId = "P001";
            String productName = "Test Product";
            BigDecimal price = new BigDecimal("25.99");
            int quantity = 3;

            // Act
            ShoppingCart.CartItem item = new ShoppingCart.CartItem(productId, productName, price, quantity);

            // Assert
            assertAll(
                    () -> assertThat(item.getProductId()).isEqualTo(productId),
                    () -> assertThat(item.getProductName()).isEqualTo(productName),
                    () -> assertThat(item.getPrice()).isEqualByComparingTo(price),
                    () -> assertThat(item.getQuantity()).isEqualTo(quantity)
            );
        }

        @Test
        void getLineTotal_whenCalled_thenReturnsPriceTimesQuantity() {
            // Arrange
            BigDecimal price = new BigDecimal("15.50");
            int quantity = 4;
            ShoppingCart.CartItem item = new ShoppingCart.CartItem("P001", "Product", price, quantity);

            // Act
            BigDecimal lineTotal = item.getLineTotal();

            // Assert
            assertThat(lineTotal).isEqualByComparingTo(new BigDecimal("62.00"));
        }

        @Test
        void setQuantity_whenCalled_thenUpdatesQuantity() {
            // Arrange
            ShoppingCart.CartItem item = new ShoppingCart.CartItem("P001", "Product", new BigDecimal("10.00"), 2);

            // Act
            item.setQuantity(5);

            // Assert
            assertThat(item.getQuantity()).isEqualTo(5);
            assertThat(item.getLineTotal()).isEqualByComparingTo(new BigDecimal("50.00"));
        }
    }
}