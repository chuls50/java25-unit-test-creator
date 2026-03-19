package com.example.order;

import java.util.List;

/**
 * Business logic for creating and managing orders.
 */
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository,
                        InventoryService inventoryService,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
    }

    /**
     * Creates a new order after validating stock. Notifies the customer on success.
     *
     * @throws IllegalArgumentException if customerId is blank or items list is empty
     * @throws OutOfStockException if any item is not available in the requested quantity
     */
    public Order createOrder(String customerId, List<OrderItem> items) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        for (OrderItem item : items) {
            if (!inventoryService.isAvailable(item.getProductId(), item.getQuantity())) {
                throw new OutOfStockException(item.getProductId());
            }
        }

        Order order = new Order(customerId, items);
        Order saved = orderRepository.save(order);
        notificationService.sendOrderConfirmation(customerId, saved.getId());
        return saved;
    }

    /**
     * Looks up an order by ID.
     *
     * @throws OrderNotFoundException if no order exists with the given ID
     */
    public Order findById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
    }

    /**
     * Cancels an order. Does NOT notify if the order was already cancelled.
     *
     * @throws OrderNotFoundException if no order exists with the given ID
     */
    public void cancelOrder(Long id) {
        Order order = findById(id);
        if (order.isCancelled()) return;
        order.cancel();
        orderRepository.save(order);
        notificationService.sendCancellationNotice(order.getCustomerId());
    }
}
