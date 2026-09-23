package kebabshop;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One finished customer order. It records who ordered, what they ordered,
 * any special instructions, when it was placed and (later) when it was
 * completed by a shop assistant.
 */
public class Order {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM, h:mm:ss a", Locale.ENGLISH);

    private static final DateTimeFormatter CLOCK_FORMAT =
            DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH);

    // Every new order gets the next number: #1, #2, #3 ...
    private static int nextOrderNumber = 1;

    private final int orderNumber = nextOrderNumber++;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt; // stays null until staff complete it
    private final List<OrderItem> items = new ArrayList<>();
    private final String customerName;
    private final String notes;

    public Order(List<OrderItem> cartItems, String customerName, String notes) {
        this.customerName = (customerName == null || customerName.trim().isEmpty())
                ? "Walk-in Customer"
                : customerName.trim();
        this.notes = (notes == null) ? "" : notes.trim();

        // Copy every line so later changes to the customer's cart (or to menu
        // prices) can never change an order that has already been placed.
        for (OrderItem line : cartItems) {
            items.add(new OrderItem(line.getMenuItem(), line.getQuantity()));
        }
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void markCompleted() {
        this.completedAt = LocalDateTime.now();
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    /** Total number of things ordered, e.g. 2 kebabs + 1 drink = 3. */
    public int getTotalQuantity() {
        int count = 0;
        for (OrderItem line : items) {
            count += line.getQuantity();
        }
        return count;
    }

    public double getTotal() {
        double total = 0.0;
        for (OrderItem line : items) {
            total += line.getLineTotal();
        }
        return total;
    }

    public String getCreatedTimeText() {
        return createdAt.format(TIME_FORMAT);
    }

    public String getCompletedTimeText() {
        return (completedAt == null) ? "Not completed" : completedAt.format(TIME_FORMAT);
    }

    /** Short time-only text (e.g. "11:37:24 AM") for the narrow table columns. */
    public String getCreatedClockText() {
        return createdAt.format(CLOCK_FORMAT);
    }

    public String getCompletedClockText() {
        return (completedAt == null) ? "-" : completedAt.format(CLOCK_FORMAT);
    }

    /** A plain-text "docket" for the order, shown in the staff GUI. */
    public String toReceiptText() {
        String divider = "-".repeat(42) + "\n";
        StringBuilder text = new StringBuilder();

        text.append("ORDER #").append(orderNumber).append("\n");
        text.append("Customer:  ").append(customerName).append("\n");
        text.append("Placed:    ").append(getCreatedTimeText()).append("\n");
        if (completedAt != null) {
            text.append("Completed: ").append(getCompletedTimeText()).append("\n");
        }
        text.append(divider);
        for (OrderItem line : items) {
            text.append(String.format(Locale.ENGLISH, "%2d x %-22s %10s\n",
                    line.getQuantity(), line.getItemName(), Money.format(line.getLineTotal())));
        }
        text.append(divider);
        text.append(String.format(Locale.ENGLISH, "TOTAL: %s\n", Money.format(getTotal())));

        if (!notes.isEmpty()) {
            text.append("\nSpecial instructions:\n").append(notes).append("\n");
        }
        return text.toString();
    }
}
