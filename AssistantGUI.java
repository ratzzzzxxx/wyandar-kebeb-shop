package kebabshop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * The SHOP ASSISTANT window (requirements 1, 4 and 5).
 *
 *  - Left side : add, update (including the price) and delete menu items.
 *  - Right side: "Current Order" tab - the order at the front of the list, the
 *                orders waiting behind it, and the "Order Completed" button.
 *                "Previous Orders" tab - every order that has been processed.
 */
public class AssistantGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_STATUS =
            "Tip: click a row in the menu table to edit its name, description or price.";

    private final ShopData shopData;

    // Menu management
    private final DefaultTableModel menuModel = Theme.readOnlyModel("Item", "Description", "Price");
    private final JTable menuTable = Theme.createTable(menuModel, 32);
    private final JTextField nameField = new JTextField();
    private final JTextField descriptionField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JButton sampleButton = Theme.lightButton("Load Wynyard Sample Menu");
    private List<MenuItem> displayedMenu = new ArrayList<>();
    private boolean refreshingMenu = false; // true while we redraw the menu table ourselves

    // Order processing
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final JLabel pendingCountLabel = new JLabel("Orders waiting: 0");
    private final JTextArea currentOrderArea = new JTextArea();
    private final DefaultTableModel queueModel = Theme.readOnlyModel("Order", "Customer", "Placed", "Qty", "Total");
    private final JButton completeButton = Theme.primaryButton("Order Completed");

    // Previous orders
    private final JLabel completedCountLabel = new JLabel("Orders completed: 0");
    private final DefaultTableModel historyModel =
            Theme.readOnlyModel("Order", "Customer", "Placed", "Completed", "Qty", "Total");
    private final JTable historyTable = Theme.createTable(historyModel, 32);
    private final JTextArea historyDetailArea = new JTextArea();
    private List<Order> displayedCompleted = new ArrayList<>();

    private final JLabel statusLabel = new JLabel(DEFAULT_STATUS);

    // Called by ShopData whenever the shop changes (e.g. a customer places an order).
    private final Runnable shopListener = () -> SwingUtilities.invokeLater(() -> {
        refreshMenu();
        refreshOrders();
    });

    public AssistantGUI(ShopData shopData) {
        this.shopData = shopData;

        setTitle("Wynyard Kebab Shop - Staff / Assistant Panel");
        setSize(1240, 740);
        setMinimumSize(new Dimension(980, 620));
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(Theme.CREAM);
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(content);

        content.add(Theme.createHeader("WYNYARD KEBAB SHOP - STAFF PANEL",
                "Menu management \u2022 Live order queue \u2022 Previous orders", 24), BorderLayout.NORTH);

        // Menu on the left (44% of the width), order processing on the right (56%).
        JPanel middle = new JPanel(new GridBagLayout());
        middle.setBackground(Theme.CREAM);
        GridBagConstraints layout = new GridBagConstraints();
        layout.fill = GridBagConstraints.BOTH;
        layout.weighty = 1.0;
        layout.gridx = 0;
        layout.weightx = 0.44;
        layout.insets = new Insets(0, 0, 0, 6);
        middle.add(buildMenuPanel(), layout);
        layout.gridx = 1;
        layout.weightx = 0.56;
        layout.insets = new Insets(0, 6, 0, 0);
        middle.add(buildOrderPanel(), layout);
        content.add(middle, BorderLayout.CENTER);

        statusLabel.setForeground(Theme.BROWN);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 4));
        content.add(statusLabel, BorderLayout.SOUTH);

        registerEventListeners();

        refreshMenu();
        refreshOrders();
    }

    // ------------------------------------------------------- building the GUI

    /** Left side: menu table, edit form and the Add / Update / Delete buttons (requirement 1). */
    private JPanel buildMenuPanel() {
        Theme.styleTextField(nameField);
        Theme.styleTextField(descriptionField);
        Theme.styleTextField(priceField);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.alignRight(menuTable, 2);
        menuTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        menuTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        menuTable.getColumnModel().getColumn(2).setPreferredWidth(70);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBackground(Theme.CREAM);
        form.add(new JLabel("Item name:"));
        form.add(nameField);
        form.add(new JLabel("Description:"));
        form.add(descriptionField);
        form.add(new JLabel("Price ($):"));
        form.add(priceField);

        JButton addButton = Theme.primaryButton("Add New Item");
        JButton updateButton = Theme.darkButton("Update Selected");
        JButton deleteButton = Theme.dangerButton("Delete Selected");
        JButton clearButton = Theme.lightButton("Clear Form");

        addButton.addActionListener(e -> addMenuItem());
        updateButton.addActionListener(e -> updateMenuItem());
        deleteButton.addActionListener(e -> deleteMenuItem());
        clearButton.addActionListener(e -> clearForm());
        sampleButton.addActionListener(e -> loadSampleMenu());

        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonGrid.setBackground(Theme.CREAM);
        buttonGrid.add(addButton);
        buttonGrid.add(updateButton);
        buttonGrid.add(deleteButton);
        buttonGrid.add(clearButton);

        JPanel buttons = new JPanel(new BorderLayout(0, 8));
        buttons.setBackground(Theme.CREAM);
        buttons.add(buttonGrid, BorderLayout.CENTER);
        buttons.add(sampleButton, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.CREAM);
        panel.setBorder(Theme.section("Menu Management"));
        panel.add(form, BorderLayout.NORTH);
        panel.add(Theme.scroll(menuTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    /** Right side: tabs for the live order queue and the previous orders. */
    private JPanel buildOrderPanel() {
        tabbedPane.setFont(Theme.font(Font.BOLD, 13));
        tabbedPane.addTab("Current Order", buildCurrentOrderTab());
        tabbedPane.addTab("Previous Orders", buildPreviousOrdersTab());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.CREAM);
        panel.setBorder(Theme.section("Order Processing"));
        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    /** Requirement 4: shows the front order, the queue behind it and the "Order Completed" button. */
    private JPanel buildCurrentOrderTab() {
        pendingCountLabel.setFont(Theme.font(Font.BOLD, 15));

        currentOrderArea.setEditable(false);
        currentOrderArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        currentOrderArea.setLineWrap(true);
        currentOrderArea.setWrapStyleWord(true);
        currentOrderArea.setMargin(new Insets(8, 8, 8, 8));

        JTable queueTable = Theme.createTable(queueModel, 28);
        Theme.alignRight(queueTable, 3);
        Theme.alignRight(queueTable, 4);
        queueTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        queueTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        queueTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        queueTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        queueTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        queueTable.setRowSelectionAllowed(false);

        JPanel nowServing = new JPanel(new BorderLayout(0, 4));
        nowServing.setBackground(Theme.CREAM);
        nowServing.add(boldLabel("Now serving (oldest order first)"), BorderLayout.NORTH);
        nowServing.add(Theme.scroll(currentOrderArea), BorderLayout.CENTER);

        JPanel upNext = new JPanel(new BorderLayout(0, 4));
        upNext.setBackground(Theme.CREAM);
        upNext.add(boldLabel("Up next"), BorderLayout.NORTH);
        upNext.add(Theme.scroll(queueTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, nowServing, upNext);
        split.setResizeWeight(0.62);
        split.setBorder(null);
        split.setBackground(Theme.CREAM);

        completeButton.setFont(Theme.font(Font.BOLD, 15));

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.CREAM);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(pendingCountLabel, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        panel.add(completeButton, BorderLayout.SOUTH);
        return panel;
    }

    /** Requirement 5: a table of every processed order, with the details of the selected one below. */
    private JPanel buildPreviousOrdersTab() {
        completedCountLabel.setFont(Theme.font(Font.BOLD, 15));

        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.alignRight(historyTable, 4);
        Theme.alignRight(historyTable, 5);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(95);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(75);

        historyDetailArea.setEditable(false);
        historyDetailArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        historyDetailArea.setMargin(new Insets(8, 8, 8, 8));

        JPanel details = new JPanel(new BorderLayout(0, 4));
        details.setBackground(Theme.CREAM);
        details.add(boldLabel("Order details (click a row above)"), BorderLayout.NORTH);
        details.add(Theme.scroll(historyDetailArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, Theme.scroll(historyTable), details);
        split.setResizeWeight(0.5);
        split.setBorder(null);
        split.setBackground(Theme.CREAM);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.CREAM);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(completedCountLabel, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JLabel boldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(Font.BOLD, 13));
        label.setForeground(Theme.BROWN);
        return label;
    }

    // ------------------------------------------------------------- events

    private void registerEventListeners() {
        completeButton.addActionListener(e -> completeCurrentOrder());

        // Clicking a row in the menu table copies that item into the edit form.
        menuTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !refreshingMenu) {
                loadSelectedMenuItemIntoFields();
            }
        });

        // Clicking a row in the history table shows that order's details.
        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedHistoryOrder();
            }
        });

        // Listen for changes made by customers (new orders) while this window is open ...
        shopData.addChangeListener(shopListener);

        // ... and stop listening when the window is closed.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                shopData.removeChangeListener(shopListener);
            }
        });
    }

    // ------------------------------------------------- menu management (req 1)

    private void addMenuItem() {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an item name.");
            return;
        }
        if (shopData.hasMenuItemNamed(name, null)) {
            JOptionPane.showMessageDialog(this, "That item is already on the menu.");
            return;
        }
        Double price = readPrice();
        if (price == null) {
            return;
        }

        shopData.addMenuItem(new MenuItem(name, description, price));
        setStatus("Added \"" + name + "\" at " + Money.format(price) + ".");
        clearForm();
    }

    private void updateMenuItem() {
        MenuItem selected = getSelectedMenuItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a menu item to update.");
            return;
        }

        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an item name.");
            return;
        }
        if (shopData.hasMenuItemNamed(name, selected)) {
            JOptionPane.showMessageDialog(this, "Another menu item already uses that name.");
            return;
        }
        Double price = readPrice();
        if (price == null) {
            return;
        }

        shopData.updateMenuItem(selected, name, description, price);
        setStatus("Updated \"" + name + "\" - now " + Money.format(price) + ".");
        clearForm();
    }

    private void deleteMenuItem() {
        MenuItem selected = getSelectedMenuItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a menu item to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete \"" + selected.getName() + "\" from the menu?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            shopData.deleteMenuItem(selected);
            setStatus("Deleted \"" + selected.getName() + "\".");
            clearForm();
        }
    }

    private void loadSampleMenu() {
        if (!shopData.getMenuItems().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "The menu already contains items.\nThe sample menu can only be loaded when the menu is empty.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Load a sample Wynyard kebab menu for the demonstration?",
                "Load sample menu", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            shopData.loadWynyardSampleMenu();
            setStatus("Sample menu loaded.");
        }
    }

    /**
     * Reads the price field. Accepts values such as 13, 13.9, 13.90 or $13.90.
     * Shows a message and returns null if the text is not a valid price.
     */
    private Double readPrice() {
        String text = priceField.getText().trim();
        if (text.startsWith("$")) {
            text = text.substring(1).trim();
        }

        // Digits with an optional decimal part of at most 2 digits (rules out NaN, 1e5, -3 ...).
        if (!text.matches("\\d{1,4}(\\.\\d{1,2})?")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid price, for example 13.90\n(numbers only, up to 2 decimal places).");
            return null;
        }

        double price = Double.parseDouble(text);
        if (price <= 0) {
            JOptionPane.showMessageDialog(this, "Price must be greater than 0.");
            return null;
        }
        return price;
    }

    private MenuItem getSelectedMenuItem() {
        int row = menuTable.getSelectedRow();
        if (row < 0 || row >= displayedMenu.size()) {
            return null;
        }
        return displayedMenu.get(row);
    }

    private void loadSelectedMenuItemIntoFields() {
        MenuItem item = getSelectedMenuItem();
        if (item == null) {
            return;
        }
        nameField.setText(item.getName());
        descriptionField.setText(item.getDescription());
        priceField.setText(String.format(Locale.ENGLISH, "%.2f", item.getPrice()));
    }

    private void clearForm() {
        nameField.setText("");
        descriptionField.setText("");
        priceField.setText("");
        menuTable.clearSelection();
        nameField.requestFocusInWindow();
    }

    /** Reloads the menu table from the shared data (keeps the selected row if possible). */
    private void refreshMenu() {
        MenuItem previouslySelected = getSelectedMenuItem();

        // While we rebuild the table the selection changes by itself - do not treat that as a click.
        refreshingMenu = true;
        displayedMenu = shopData.getMenuItems();
        menuModel.setRowCount(0);
        for (MenuItem item : displayedMenu) {
            menuModel.addRow(new Object[] { item.getName(), item.getDescription(), Money.format(item.getPrice()) });
        }
        int row = displayedMenu.indexOf(previouslySelected);
        if (row >= 0) {
            menuTable.setRowSelectionInterval(row, row);
        }
        refreshingMenu = false;

        // The sample menu is only allowed while the menu is empty.
        sampleButton.setEnabled(displayedMenu.isEmpty());
        sampleButton.setToolTipText(displayedMenu.isEmpty()
                ? "Fill the menu with example items for the demonstration"
                : "Only available while the menu is empty");
    }

    // --------------------------------------------- order processing (req 4, 5)

    /** Redraws the current order, the waiting queue and the previous orders. */
    private void refreshOrders() {
        // ---- current order + queue
        List<Order> pending = shopData.getPendingOrders();
        pendingCountLabel.setText("Orders waiting: " + pending.size());
        tabbedPane.setTitleAt(0, "Current Order (" + pending.size() + ")");
        completeButton.setEnabled(!pending.isEmpty());

        if (pending.isEmpty()) {
            currentOrderArea.setText("No orders waiting.\n\nNew customer orders will appear here automatically.");
        } else {
            currentOrderArea.setText(pending.get(0).toReceiptText());
        }
        currentOrderArea.setCaretPosition(0);

        queueModel.setRowCount(0);
        for (int i = 1; i < pending.size(); i++) { // index 0 is already shown as "Now serving"
            Order order = pending.get(i);
            queueModel.addRow(new Object[] {
                    "#" + order.getOrderNumber(),
                    order.getCustomerName(),
                    order.getCreatedClockText(),
                    order.getTotalQuantity(),
                    Money.format(order.getTotal()) });
        }

        // ---- previous orders
        Order previouslySelected = getSelectedCompletedOrder();
        displayedCompleted = shopData.getCompletedOrders();
        completedCountLabel.setText("Orders completed: " + displayedCompleted.size());
        tabbedPane.setTitleAt(1, "Previous Orders (" + displayedCompleted.size() + ")");

        historyModel.setRowCount(0);
        for (Order order : displayedCompleted) {
            historyModel.addRow(new Object[] {
                    "#" + order.getOrderNumber(),
                    order.getCustomerName(),
                    order.getCreatedClockText(),
                    order.getCompletedClockText(),
                    order.getTotalQuantity(),
                    Money.format(order.getTotal()) });
        }
        int row = displayedCompleted.indexOf(previouslySelected);
        if (row >= 0) {
            historyTable.setRowSelectionInterval(row, row);
        }
        showSelectedHistoryOrder();
    }

    private Order getSelectedCompletedOrder() {
        int row = historyTable.getSelectedRow();
        if (row < 0 || row >= displayedCompleted.size()) {
            return null;
        }
        return displayedCompleted.get(row);
    }

    private void showSelectedHistoryOrder() {
        Order order = getSelectedCompletedOrder();
        if (order == null) {
            historyDetailArea.setText(displayedCompleted.isEmpty()
                    ? "No orders have been completed yet."
                    : "Select an order in the table to see what was ordered.");
        } else {
            historyDetailArea.setText(order.toReceiptText());
        }
        historyDetailArea.setCaretPosition(0);
    }

    /**
     * Requirement 4: the front order is removed from the order list and moved to the
     * processed list. The next order then becomes the front order automatically.
     */
    private void completeCurrentOrder() {
        Order completed = shopData.completeNextOrder();
        if (completed == null) {
            JOptionPane.showMessageDialog(this, "There is no pending order to complete.");
            return;
        }

        Order next = shopData.getNextOrder();
        setStatus("Order #" + completed.getOrderNumber() + " (" + completed.getCustomerName()
                + ") completed and moved to Previous Orders. "
                + (next == null ? "No more orders waiting." : "Now showing order #" + next.getOrderNumber() + "."));
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }
}
