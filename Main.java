package kebabshop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Starts the program. It creates the ONE shared ShopData object and shows a
 * small launcher window with a button for each of the two GUIs.
 */
public class Main {

    // The staff window is opened only once (a second click brings it to the front).
    private static AssistantGUI staffWindow;

    private static final Color GREY = new Color(92, 86, 80);

    public static void main(String[] args) {
        // All Swing windows must be created on the event-dispatch thread.
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // If the system look-and-feel is not available, the default one is used.
            }
            ShopData shopData = new ShopData();
            createLauncher(shopData);
        });
    }

    private static void createLauncher(ShopData shopData) {
        JFrame frame = new JFrame("Wynyard Kebab Shop Ordering System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(620, 400);
        frame.setMinimumSize(new Dimension(560, 380));
        frame.setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(Theme.CREAM);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        frame.setContentPane(content);

        content.add(Theme.createHeader("WYNYARD KEBAB SHOP",
                "Fresh \u2022 Fast \u2022 Made to order", 30), BorderLayout.NORTH);

        JLabel welcome = new JLabel("Welcome to the ordering system", SwingConstants.CENTER);
        welcome.setFont(Theme.font(Font.BOLD, 17));
        JLabel line1 = new JLabel("Choose a screen below. Both windows share the same live shop data.",
                SwingConstants.CENTER);
        line1.setForeground(GREY);
        JLabel line2 = new JLabel("The menu starts empty so staff can create and manage it.",
                SwingConstants.CENTER);
        line2.setForeground(GREY);

        JPanel text = new JPanel(new GridLayout(3, 1, 0, 7));
        text.setBackground(Theme.CREAM);
        text.add(welcome);
        text.add(line1);
        text.add(line2);
        content.add(text, BorderLayout.CENTER);

        JButton customerButton = Theme.darkButton("Customer Ordering");
        JButton staffButton = Theme.primaryButton("Staff / Assistant Panel");
        customerButton.setFont(Theme.font(Font.BOLD, 15));
        staffButton.setFont(Theme.font(Font.BOLD, 15));

        // Button clicks = events. Each opens a window that uses the same ShopData.
        customerButton.addActionListener(e -> new CustomerGUI(shopData).setVisible(true));
        staffButton.addActionListener(e -> openStaffWindow(shopData));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        buttons.setBackground(Theme.CREAM);
        buttons.add(customerButton);
        buttons.add(staffButton);
        content.add(buttons, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static void openStaffWindow(ShopData shopData) {
        if (staffWindow == null || !staffWindow.isDisplayable()) {
            staffWindow = new AssistantGUI(shopData);
            staffWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    staffWindow = null;
                }
            });
            staffWindow.setVisible(true);
        }
        staffWindow.toFront();
    }
}
