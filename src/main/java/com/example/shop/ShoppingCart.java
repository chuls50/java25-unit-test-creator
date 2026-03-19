package com.example.shop;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a shopping cart for a single customer session.
 *
 * Responsibilities:
 * - Add and remove items
 * - Apply a single percentage-based discount code
 * - Calculate subtotal, discount amount, and final total
 * - Enforce business rules (quantity limits, valid discount codes, etc.)
 */
public class ShoppingCart {

    private static final int MAX_QUANTITY_PER_ITEM = 99;
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8%

    private final String customerId;
    private final DiscountService discountService;
    private final List<CartItem> items = new ArrayList<>();

    private String appliedDiscountCode = null;

    public ShoppingCart(String customerId, DiscountService discountService) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        this.customerId = customerId;
        this.discountService = discountService;
    }

    /**
     * Adds a product to the cart. If the product already exists, increments
     * quantity.
     *
     * @throws IllegalArgumentException if quantity < 1
     * @throws CartException            if adding would exceed MAX_QUANTITY_PER_ITEM
     */
    public void addItem(String productId, String productName, BigDecimal price, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }

        Optional<CartItem> existing = findItem(productId);

        if (existing.isPresent()) {
            int newQty = existing.get().getQuantity() + quantity;
            if (newQty > MAX_QUANTITY_PER_ITEM) {
                throw new CartException("Cannot exceed " + MAX_QUANTITY_PER_ITEM
                        + " units of product: " + productId);
            }
            existing.get().setQuantity(newQty);
        } else {
            items.add(new CartItem(productId, productName, price, quantity));
        }
    }

    /**
     * Removes a product from the cart entirely.
     *
     * @throws CartException if the product is not in the cart
     */
    public void removeItem(String productId) {
        boolean removed = items.removeIf(i -> i.getProductId().equals(productId));
        if (!removed) {
            throw new CartException("Product not found in cart: " + productId);
        }
    }

    /**
     * Applies a discount code to the cart. Only one code can be applied at a time.
     * Validates the code via DiscountService.
     *
     * @throws CartException            if a discount code is already applied
     * @throws InvalidDiscountException if the code is not valid
     */
    public void applyDiscountCode(String code) {
        if (appliedDiscountCode != null) {
            throw new CartException("A discount code is already applied: " + appliedDiscountCode);
        }
        int discountPercent = discountService.validate(code);
        if (discountPercent <= 0) {
            throw new InvalidDiscountException("Invalid or expired discount code: " + code);
        }
        this.appliedDiscountCode = code;
    }

    /**
     * Removes any applied discount code.
     */
    public void removeDiscountCode() {
        this.appliedDiscountCode = null;
    }

    /**
     * Returns the subtotal (sum of all item prices * quantities), before discount
     * and tax.
     */
    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Returns the discount amount in currency. Returns ZERO if no code is applied.
     */
    public BigDecimal getDiscountAmount() {
        if (appliedDiscountCode == null)
            return BigDecimal.ZERO;
        int percent = discountService.validate(appliedDiscountCode);
        return getSubtotal()
                .multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Returns the final total: (subtotal - discount) + tax.
     * Tax is applied after discount.
     */
    public BigDecimal getTotal() {
        BigDecimal discounted = getSubtotal().subtract(getDiscountAmount());
        BigDecimal tax = discounted.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        return discounted.add(tax).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Returns true if the cart has no items.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Returns the total number of individual units across all items.
     */
    public int getTotalItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getAppliedDiscountCode() {
        return appliedDiscountCode;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    private Optional<CartItem> findItem(String productId) {
        return items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();
    }

    // -------------------------------------------------------------------------
    // Inner classes
    // -------------------------------------------------------------------------

    public static class CartItem {
        private final String productId;
        private final String productName;
        private final BigDecimal price;
        private int quantity;

        public CartItem(String productId, String productName, BigDecimal price, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
        }

        public BigDecimal getLineTotal() {
            return price.multiply(BigDecimal.valueOf(quantity));
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int q) {
            this.quantity = q;
        }
    }

    // Checked exceptions used by this class
    public static class CartException extends RuntimeException {
        public CartException(String message) {
            super(message);
        }
    }

    public static class InvalidDiscountException extends RuntimeException {
        public InvalidDiscountException(String message) {
            super(message);
        }
    }

    // Dependency interface — will be mocked in tests
    public interface DiscountService {
        /**
         * Returns the discount percentage (e.g. 10 for 10%) for a given code.
         * Returns 0 or negative if the code is invalid or expired.
         */
        int validate(String code);
    }
}
