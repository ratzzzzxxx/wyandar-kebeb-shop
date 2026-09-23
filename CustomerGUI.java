package kebabshop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * The CUSTOMER window (requirements 2 and 3).
 *
 * A customer picks items from the menu, chooses a quantity for each,
 * optionally types a name and special instructions, and clicks "Finish Order".
 * The finished order is added to the end of the shared order list.
 */
public class CustomerGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int MAX_QUANTITY_PER_ITEM = 99;

    private final ShopData shopData;

    // The customer's "shopping cart" - lines not yet sent to the kitchen.
    private final List<OrderItem> cart = new ArrayList<>();

    // The menu items currently shown in the menu table (row i of the table = item i here).
    private List<MenuItem> displayedMenu = new ArrayList<>();

    private final DefaultTableModel menuModel = Theme.readOnlyModel("Item", "Description", "Price");
    private final DefaultTableModel cartModel = Theme.readOnlyModel("Item", "Qty", "Unit Price", "Subtotal");
    private final JTable menuTable = Theme.createTable(menuModel, 34);
    private final JTable cartTable = Theme.createTable(cartModel, 34);

    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private final JLabel menuMessageLabel = new JLabel();
    private final JLabel cartNoticeLabel = new JLabel(" ");
    private final JLabel totalLabel = new JLabel("Total: $0.00", SwingConstants.CENTER);
    private final JTextField customerNameField = new JTextField(16);
    private final JTextArea notesArea = new JTextArea(2, 28);

    // Called by ShopData whenever the shop changes (e.g. staff edit the menu).
    private final Runnable shopListener = () -> SwingUtilities.invokeLater(this::refreshMenu);

    public CustomerGUI(ShopData shopData) {
        this.shopData = shopData;

        setTitle("Wynyard Kebab Shop - Customer Ordering");
        setSize(1020, 740);
        setMinimumSize(new Dimension(880, 640));
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(Theme.CREAM);
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(content);

        content.add(Theme.createHeader("WYNYARD KEBAB SHOP",
                "Fresh \u2022 Fast \u2022 Made to order", 26), BorderLayout.NORTH);

        JPanel middle = new JPanel(new GridLayout(1, 2, 12, 0));
        middle.setBackground(Theme.CREAM);
        middle.add(buildMenuPanel());
        middle.add(buildCartPanel());
        content.add(middle, BorderLayout.CENTER);

        content.add(buildDetailsPanel(), BorderLayout.SOUTH);

        registerEventListeners();

        refreshMenu();
        refreshCart();
    }

    // ------------------------------------------------------- building the GUI

    /** Left side: the menu table plus quantity and "Add to Order". */
    private JPanel buildMenuPanel() {
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.alignRight(menuTable, 2);
        menuTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        menuTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        menuTable.getColumnModel().getColumn(2).setPreferredWidth(70);

        menuMessageLabel.setText("No menu yet \u2014 please ask a staff member to add the shop items.");
        menuMessageLabel.setForeground(Theme.BROWN);

        quantitySpinner.setPreferredSize(new Dimension(64, 32));

        JButton addButton = Theme.primaryButton("Add to Order");
        addButton.addActionListener(e -> addSelectedItemToCart());

        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        addRow.setBackground(Theme.CREAM);
        addRow.add(new JLabel("Quantity:"));
        addRow.add(quantitySpinner);
        addRow.add(addButton);
        JLabel hint = new JLabel("or double-click an item");
        hint.setForeground(Theme.BROWN);
        addRow.add(hint);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Theme.CREAM);
        bottom.add(menuMessageLabel, BorderLayout.NORTH);
        bottom.add(addRow, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.CREAM);
        panel.setBorder(Theme.section("1. Pick something from the menu"));
        panel.add(Theme.scroll(menuTable), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    /** Right side: the customer's current order with Remove / Clear buttons. */
    private JPanel buildCartPanel() {
        Theme.alignRight(cartTable, 1);
        Theme.alignRight(cartTable, 2);
        Theme.alignRight(cartTable, 3);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(40);

        cartNoticeLabel.setForeground(Theme.RED);

        JButton removeButton = Theme.darkButton("Remove Selected");
        removeButton.addActionListener(e -> removeSelectedCartItem());
        JButton clearButton = Theme.lightButton("Clear Order");
        clearButton.addActionListener(e -> clearCart());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        buttonRow.setBackground(Theme.CREAM);
        buttonRow.add(removeButton);
        buttonRow.add(clearButton);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Theme.CREAM);
        bottom.add(cartNoticeLabel, BorderLayout.NORTH);
        bottom.add(buttonRow, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.CREAM);
        panel.setBorder(Theme.section("2. Your order"));
        panel.add(Theme.scroll(cartTable), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    /** Bottom: name, special instructions, running total and the Finish button. */
    private JPanel buildDetailsPanel() {
        Theme.styleTextField(customerNameField);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(Theme.font(Font.PLAIN, 13));
        notesArea.setMargin(new Insets(4, 6, 4, 6));

        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        nameRow.setBackground(Theme.CREAM);
        nameRow.add(new JLabel("Customer name (optional):"));
        nameRow.add(customerNameField);

        JPanel notesRow = new JPanel(new BorderLayout(8, 4));
        notesRow.setBackground(Theme.CREAM);
        notesRow.add(new JLabel("Special instructions:"), BorderLayout.WEST);
        notesRow.add(Theme.scroll(notesArea), BorderLayout.CENTER);

        JPanel form = new JPanel(new BorderLayout(6, 6));
        form.setBackground(Theme.CREAM);
        form.add(nameRow, BorderLayout.NORTH);
        form.add(notesRow, BorderLayout.CENTER);

        totalLabel.setFont(Theme.font(Font.BOLD, 20));
        JButton finishButton = Theme.primaryButton("Finish Order");
        finishButton.setFont(Theme.font(Font.BOLD, 16));
        finishButton.addActionListener(e -> finishOrder());

        JPanel finishPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        finishPanel.setBackground(Theme.CREAM);
        finishPanel.setPreferredSize(new Dimension(220, 90));
        finishPanel.add(totalLabel);
        finishPanel.add(finishButton);

        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(Theme.CREAM);
        panel.setBorder(Theme.section("3. Order details"));
        panel.add(form, BorderLayout.CENTER);
        panel.add(finishPanel, BorderLayout.EAST);
        return panel;
    }

    // ------------------------------------------------------------- events

    private void registerEventListeners() {
        // Double-clicking a menu row is a quick way to add one to the order.
        menuTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSelectedItemToCart();
                }
            }
        });

        // Listen for changes made by staff (menu edits) while this window is open ...
        shopData.addChangeListener(shopListener);

        // ... and stop listening when the window is closed.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                shopData.removeChangeListener(shopListener);
            }
        });
    }

    // ------------------------------------------------------- menu and cart

    /** Reloads the menu table from the shared data (keeps the selected row if possible). */
    private void refreshMenu() {
        MenuItem previouslySelected = getSelectedMenuItem();

        displayedMenu = shopData.getMenuItems();
        menuModel.setRowCount(0);
        for (MenuItem item : displayedMenu) {
            menuModel.addRow(new Object[] { item.getName(), item.getDescription(), Money.format(item.getPrice()) });
        }
        menuMessageLabel.setVisible(displayedMenu.isEmpty());

        int row = displayedMenu.indexOf(previouslySelected);
        if (row >= 0) {
            menuTable.setRowSelectionInterval(row, row);
        }

        syncCartWithMenu();
    }

    private MenuItem getSelectedMenuItem() {
        int row = menuTable.getSelectedRow();
        if (row < 0 || row >= displayedMenu.size()) {
            return null;
        }
        return displayedMenu.get(row);
    }

    /**
     * If staff delete an item or change a price while the customer is choosing,
     * update the cart so the customer never orders something that no longer exists
     * and always sees the current price.
     */
    private void syncCartWithMenu() {
        List<String> removedNames = new ArrayList<>();
        List<String> repricedNames = new ArrayList<>();

        Iterator<OrderItem> iterator = cart.iterator();
        while (iterator.hasNext()) {
            OrderItem line = iterator.next();
            if (!displayedMenu.contains(line.getMenuItem())) {
                removedNames.add(line.getItemName());
                iterator.remove();
            } else {
                if (line.getUnitPrice() != line.getMenuItem().getPrice()) {
                    repricedNames.add(line.getMenuItem().getName());
                }
                line.syncWithMenu();
            }
        }

        if (!removedNames.isEmpty()) {
            showCartNotice("No longer on the menu, removed from your order: " + String.join(", ", removedNames));
        } else if (!repricedNames.isEmpty()) {
            showCartNotice("Price updated by staff: " + String.join(", ", repricedNames));
        }
        refreshCart();
    }

    /** Requirement 2: add the selected menu item, in the chosen quantity, to the order. */
    private void addSelectedItemToCart() {
        MenuItem item = getSelectedMenuItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please choose a menu item first.",
                    "Choose an item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int quantity = (Integer) quantitySpinner.getValue();

        // If this item is already in the order, just increase its quantity.
        OrderItem existingLine = findCartLine(item);
        if (existingLine != null) {
            int newQuantity = existingLine.getQuantity() + quantity;
            if (newQuantity > MAX_QUANTITY_PER_ITEM) {
                newQuantity = MAX_QUANTITY_PER_ITEM;
                JOptionPane.showMessageDialog(this,
                        "The maximum quantity for one item is " + MAX_QUANTITY_PER_ITEM + ".");
            }
            existingLine.setQuantity(newQuantity);
        } else {
            cart.add(new OrderItem(item, quantity));
        }

        showCartNotice(" ");
        quantitySpinner.setValue(1);
        refreshCart();
    }

    private OrderItem findCartLine(MenuItem item) {
        for (OrderItem line : cart) {
            if (line.getMenuItem() == item) {
                return line;
            }
        }
        return null;
    }

    private void removeSelectedCartItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cart.size()) {
            JOptionPane.showMessageDialog(this, "Select something from your order first.");
            return;
        }
        cart.remove(row);
        showCartNotice(" ");
        refreshCart();
    }

    private void clearCart() {
        cart.clear();
        showCartNotice(" ");
        refreshCart();
    }

    private void showCartNotice(String text) {
        cartNoticeLabel.setText(text.isEmpty() ? " " : text);
    }

    /** Redraws the cart table and the running total. */
    private void refreshCart() {
        cartModel.setRowCount(0);
        double total = 0.0;
        for (OrderItem line : cart) {
            cartModel.addRow(new Object[] {
                    line.getItemName(),
                    line.getQuantity(),
                    Money.format(line.getUnitPrice()),
                    Money.format(line.getLineTotal()) });
            total += line.getLineTotal();
        }
        totalLabel.setText("Total: " + Money.format(total));
    }

    // ------------------------------------------------------- finishing

    /** Requirement 3: "Finish" completes the order and adds it to the END of the order list. */
    private void finishOrder() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Your order is empty. Add at least one item before finishing.");
            return;
        }

        Order order = new Order(cart, customerNameField.getText(), notesArea.getText());
        shopData.addOrder(order);

        // Reset the form for the next customer.
        cart.clear();
        customerNameField.setText("");
        notesArea.setText("");
        showCartNotice(" ");
        refreshCart();

        int ordersAhead = shopData.getPendingOrderCount() - 1;
        JOptionPane.showMessageDialog(this,
                "Thanks, " + order.getCustomerName() + "!\n"
                        + "Your order number is #" + order.getOrderNumber() + ".\n"
                        + "Total: " + Money.format(order.getTotal()) + "\n"
                        + "Orders ahead of you: " + ordersAhead + "\n\n"
                        + "It has been sent to the kitchen.",
                "Order placed", JOptionPane.INFORMATION_MESSAGE);
    }
}
