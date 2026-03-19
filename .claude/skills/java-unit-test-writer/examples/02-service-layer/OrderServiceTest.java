package com.example.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Unit tests for OrderService.
 *
 * Class type: Service layer — uses Mockito to mock all three dependencies.
 * Prompt used: "Write unit tests for OrderService.java using Mockito.
 *               Use @Nested to group by method and BDD-style given/when/then."
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private InventoryService inventoryService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private OrderService subject;

    // Shared test data
    private OrderItem widget;
    private List<OrderItem> items;

    @BeforeEach
    void setUp() {
        widget = new OrderItem("WIDGET-01", 2);
        items  = List.of(widget);
    }

    // =========================================================================
    // createOrder
    // =========================================================================

    @Nested
    @DisplayName("createOrder")
    class CreateOrder {

        @Test
        @DisplayName("saves order and sends confirmation when input is valid and stock available")
        void givenValidInput_thenSavesAndNotifies() {
            // Arrange
            Order saved = new Order("CUST-1", items);
            saved.setId(100L);

            given(inventoryService.isAvailable("WIDGET-01", 2)).willReturn(true);
            given(orderRepository.save(any(Order.class))).willReturn(saved);

            // Act
            Order result = subject.createOrder("CUST-1", items);

            // Assert
            assertThat(result.getId()).isEqualTo(100L);
            then(orderRepository).should().save(any(Order.class));
            then(notificationService).should().sendOrderConfirmation("CUST-1", 100L);
        }

        @Test
        @DisplayName("captures the saved order and verifies customer ID is set correctly")
        void givenValidInput_thenOrderHasCorrectCustomerId() {
            // Arrange
            given(inventoryService.isAvailable(anyString(), anyLong())).willReturn(true);
            given(orderRepository.save(any(Order.class))).willAnswer(i -> i.getArgument(0));

            // Act
            subject.createOrder("CUST-99", items);

            // Assert — capture what was actually saved
            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            then(orderRepository).should().save(captor.capture());
            assertThat(captor.getValue().getCustomerId()).isEqualTo("CUST-99");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when customerId is blank")
        void givenBlankCustomerId_thenThrows() {
            assertThatThrownBy(() -> subject.createOrder("  ", items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customerId");

            then(orderRepository).shouldHaveNoInteractions();
            then(notificationService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("throws IllegalArgumentException when items list is empty")
        void givenEmptyItems_thenThrows() {
            assertThatThrownBy(() -> subject.createOrder("CUST-1", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one item");
        }

        @Test
        @DisplayName("throws OutOfStockException when an item is unavailable")
        void givenItemOutOfStock_thenThrowsAndDoesNotSave() {
            // Arrange
            given(inventoryService.isAvailable("WIDGET-01", 2)).willReturn(false);

            // Act + Assert
            assertThatThrownBy(() -> subject.createOrder("CUST-1", items))
                .isInstanceOf(OutOfStockException.class);

            then(orderRepository).should(never()).save(any());
            then(notificationService).shouldHaveNoInteractions();
        }
    }

    // =========================================================================
    // findById
    // =========================================================================

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns order when it exists")
        void givenExistingId_thenReturnsOrder() {
            Order order = new Order("CUST-1", items);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            Order result = subject.findById(1L);

            assertThat(result.getCustomerId()).isEqualTo("CUST-1");
        }

        @Test
        @DisplayName("throws OrderNotFoundException when order does not exist")
        void givenMissingId_thenThrows() {
            given(orderRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> subject.findById(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
        }
    }

    // =========================================================================
    // cancelOrder
    // =========================================================================

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {

        @Test
        @DisplayName("cancels order and sends notice when order is active")
        void givenActiveOrder_thenCancelsAndNotifies() {
            // Arrange
            Order order = new Order("CUST-1", items);  // not cancelled
            given(orderRepository.findById(5L)).willReturn(Optional.of(order));

            // Act
            subject.cancelOrder(5L);

            // Assert
            assertThat(order.isCancelled()).isTrue();
            then(orderRepository).should().save(order);
            then(notificationService).should().sendCancellationNotice("CUST-1");
        }

        @Test
        @DisplayName("does nothing when order is already cancelled")
        void givenAlreadyCancelledOrder_thenSkipsNotification() {
            // Arrange
            Order order = new Order("CUST-1", items);
            order.cancel(); // pre-cancel it
            given(orderRepository.findById(5L)).willReturn(Optional.of(order));

            // Act
            subject.cancelOrder(5L);

            // Assert — save and notification should NOT be called again
            then(orderRepository).should(never()).save(any());
            then(notificationService).should(never()).sendCancellationNotice(anyString());
        }

        @Test
        @DisplayName("throws OrderNotFoundException when order does not exist")
        void givenMissingId_thenThrows() {
            given(orderRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> subject.cancelOrder(99L))
                .isInstanceOf(OrderNotFoundException.class);
        }
    }
}
