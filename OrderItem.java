package kebabshop;

/**
 * One line of an order: a menu item plus how many the customer wants
 * (for example "2 x Chicken Kebab").
 *
 * The name and unit price are COPIED from the menu item when the line is
 * created. This means that if staff later change the price on the menu,
 * an order that was already placed keeps the price the customer agreed to.
 *
 * While a customer is still building their cart, the GUI calls
 * {@link #syncWithMenu()} so the cart follows menu changes. Once the order
 * is finished, the copies made by {@link Order} are never synced again.
 */
public class OrderItem {

    private final MenuItem menuItem; // link back to the menu (used by the cart)
    private String itemName;         // copy of the name
    private double unitPrice;        // copy of the price
    private int quantity;

    public OrderItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        syncWithMenu();
    }

    /** Refreshes the copied name and price from the menu item. */
    public void syncWithMenu() {
        this.itemName = menuItem.getName();
        this.unitPrice = menuItem.getPrice();
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public String getItemName() {
        return itemName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getLineTotal() {
        return unitPrice * quantity;
    }
}
