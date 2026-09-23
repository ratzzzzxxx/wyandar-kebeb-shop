package kebabshop;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * The single object that BOTH GUIs share. It holds:
 *
 *  - the menu (starts EMPTY),
 *  - the pending order list (a FIFO queue: first order placed = first served),
 *  - the list of completed orders.
 *
 * Whenever anything changes, every registered listener is told, so both
 * windows update themselves straight away (the "observer" pattern).
 * All calls are made on the Swing event-dispatch thread, so no locking is needed.
 */
public class ShopData {

    private final List<MenuItem> menuItems = new ArrayList<>();
    private final Queue<Order> pendingOrders = new LinkedList<>();
    private final List<Order> completedOrders = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();

    // ---------------------------------------------------------------- menu

    /** Returns a copy, so the GUIs cannot change the real list by accident. */
    public List<MenuItem> getMenuItems() {
        return new ArrayList<>(menuItems);
    }

    /**
     * True if a DIFFERENT menu item already uses this name (ignoring upper/lower case).
     * Pass the item being edited as ignoreItem (or null when adding a new item).
     */
    public boolean hasMenuItemNamed(String name, MenuItem ignoreItem) {
        String wanted = name.trim();
        for (MenuItem item : menuItems) {
            if (item != ignoreItem && item.getName().equalsIgnoreCase(wanted)) {
                return true;
            }
        }
        return false;
    }

    public void addMenuItem(MenuItem item) {
        menuItems.add(item);
        notifyListeners();
    }

    public void updateMenuItem(MenuItem item, String name, String description, double price) {
        if (!menuItems.contains(item)) {
            return;
        }
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        notifyListeners();
    }

    public void deleteMenuItem(MenuItem item) {
        if (menuItems.remove(item)) {
            notifyListeners();
        }
    }

    /** Demo helper: only works while the menu is empty. */
    public void loadWynyardSampleMenu() {
        if (!menuItems.isEmpty()) {
            return;
        }
        menuItems.add(new MenuItem("Chicken Kebab", "Grilled chicken, lettuce, tomato, onion & choice of sauce", 13.90));
        menuItems.add(new MenuItem("Lamb Kebab", "Seasoned lamb, fresh salad & choice of sauce", 14.90));
        menuItems.add(new MenuItem("Mixed Kebab", "Chicken and lamb with salad & choice of sauce", 15.90));
        menuItems.add(new MenuItem("Falafel Kebab", "Falafel, hummus, lettuce, tomato & onion", 12.50));
        menuItems.add(new MenuItem("Snack Pack", "Hot chips topped with meat, cheese & sauce", 17.50));
        menuItems.add(new MenuItem("Hot Chips", "Crispy seasoned chips", 6.00));
        menuItems.add(new MenuItem("Soft Drink", "375 mL can", 3.50));
        notifyListeners();
    }

    // -------------------------------------------------------------- orders

    /** Requirement 3: a finished order is added to the END of the order list. */
    public void addOrder(Order order) {
        pendingOrders.offer(order);
        notifyListeners();
    }

    /** The order at the FRONT of the list (the one to make next), or null if none. */
    public Order getNextOrder() {
        return pendingOrders.peek();
    }

    /** All waiting orders, oldest first. */
    public List<Order> getPendingOrders() {
        return new ArrayList<>(pendingOrders);
    }

    public int getPendingOrderCount() {
        return pendingOrders.size();
    }

    /**
     * Requirement 4: removes the front order from the order list and adds it to
     * the list of processed orders. The next order automatically becomes the front.
     *
     * @return the order that was completed, or null if nothing was waiting
     */
    public Order completeNextOrder() {
        Order order = pendingOrders.poll();
        if (order != null) {
            order.markCompleted();
            completedOrders.add(order);
            notifyListeners();
        }
        return order;
    }

    /** Requirement 5: every order that has been processed, oldest first. */
    public List<Order> getCompletedOrders() {
        return new ArrayList<>(completedOrders);
    }

    // ----------------------------------------------------------- listeners

    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        // Iterate over a copy in case a listener is removed while we loop.
        for (Runnable listener : new ArrayList<>(listeners)) {
            listener.run();
        }
    }
}
