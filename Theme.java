package kebabshop;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

/**
 * Colours, fonts and small factory methods shared by every screen, so the
 * Wynyard Kebab Shop charcoal / gold / cream theme is defined in ONE place.
 *
 * Buttons and table headers are painted by us (instead of relying on the
 * operating system's look-and-feel), so the colours look the same on
 * Windows, macOS and Linux.
 */
public final class Theme {

    public static final Color CHARCOAL = new Color(38, 36, 34);
    public static final Color GOLD = new Color(232, 157, 54);
    public static final Color GOLD_HOVER = new Color(243, 176, 82);
    public static final Color GOLD_PRESSED = new Color(201, 133, 36);
    public static final Color CREAM = new Color(250, 247, 241);
    public static final Color SAND = new Color(238, 226, 205);
    public static final Color SAND_DARK = new Color(214, 198, 170);
    public static final Color BROWN = new Color(105, 85, 60);
    public static final Color ROW_ALT = new Color(247, 242, 232);
    public static final Color ROW_SELECTED = new Color(250, 222, 170);
    public static final Color RED = new Color(176, 58, 46);

    private static final String FONT = "SansSerif";

    private Theme() {
        // Utility class - no instances needed.
    }

    public static Font font(int style, int size) {
        return new Font(FONT, style, size);
    }

    // ------------------------------------------------------------- header

    /** The dark banner with a gold underline shown at the top of every window. */
    public static JPanel createHeader(String title, String subtitle, int titleSize) {
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setBackground(CHARCOAL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, GOLD),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(font(Font.BOLD, titleSize));

        JLabel subtitleLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subtitleLabel.setForeground(new Color(235, 220, 196));
        subtitleLabel.setFont(font(Font.PLAIN, 13));

        header.add(titleLabel);
        header.add(subtitleLabel);
        return header;
    }

    // ------------------------------------------------------------ borders

    /** A rounded, titled box used to group related controls. */
    public static Border section(String title) {
        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SAND_DARK, 1, true),
                " " + title + " ",
                TitledBorder.LEFT, TitledBorder.TOP,
                font(Font.BOLD, 14), CHARCOAL);
        return BorderFactory.createCompoundBorder(titled, BorderFactory.createEmptyBorder(4, 8, 8, 8));
    }

    public static void styleTextField(JTextComponent field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SAND_DARK),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
    }

    /** A scroll pane with a thin sand-coloured outline. */
    public static JScrollPane scroll(Component view) {
        JScrollPane pane = new JScrollPane(view);
        pane.setBorder(BorderFactory.createLineBorder(SAND_DARK));
        pane.getViewport().setBackground(Color.WHITE);
        return pane;
    }

    // ------------------------------------------------------------ buttons

    /** Gold button - the main action on a screen (Finish Order, Order Completed...). */
    public static JButton primaryButton(String text) {
        return new ThemedButton(text, GOLD, GOLD_HOVER, GOLD_PRESSED, CHARCOAL);
    }

    /** Charcoal button - normal actions. */
    public static JButton darkButton(String text) {
        return new ThemedButton(text, CHARCOAL, new Color(72, 68, 63), new Color(20, 19, 18), Color.WHITE);
    }

    /** Sand button - low-key actions (Clear, Load sample menu...). */
    public static JButton lightButton(String text) {
        return new ThemedButton(text, SAND, SAND_DARK, new Color(196, 178, 146), CHARCOAL);
    }

    /** Red button - destructive actions such as Delete. */
    public static JButton dangerButton(String text) {
        return new ThemedButton(text, RED, new Color(196, 78, 66), new Color(140, 42, 33), Color.WHITE);
    }

    /** A JButton that paints its own rounded background in any look-and-feel. */
    private static class ThemedButton extends JButton {
        private static final long serialVersionUID = 1L;

        private final Color normal;
        private final Color hover;
        private final Color pressed;
        private boolean mouseOver = false;

        ThemedButton(String text, Color normal, Color hover, Color pressed, Color textColor) {
            super(text);
            this.normal = normal;
            this.hover = hover;
            this.pressed = pressed;

            setForeground(textColor);
            setFont(font(Font.BOLD, 13));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Mouse listener = event-driven hover effect.
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    mouseOver = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    mouseOver = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill;
            if (!isEnabled()) {
                fill = new Color(222, 217, 208);
            } else if (getModel().isPressed()) {
                fill = pressed;
            } else if (mouseOver) {
                fill = hover;
            } else {
                fill = normal;
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();

            super.paintComponent(g); // draws the text on top
        }
    }

    // ------------------------------------------------------------- tables

    /** A table model whose cells cannot be edited by typing in them. */
    public static DefaultTableModel readOnlyModel(String... columnNames) {
        return new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** A table with a charcoal header, striped rows and a gold selection colour. */
    public static JTable createTable(DefaultTableModel model, int rowHeight) {
        JTable table = new JTable(model) {
            private static final long serialVersionUID = 1L;

            // Hovering a cell shows its full text (useful when a long description is cut off).
            @Override
            public String getToolTipText(MouseEvent event) {
                int row = rowAtPoint(event.getPoint());
                int column = columnAtPoint(event.getPoint());
                if (row < 0 || column < 0) {
                    return null;
                }
                Object value = getValueAt(row, column);
                return (value == null) ? null : value.toString();
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component cell = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    cell.setBackground(ROW_SELECTED);
                } else {
                    cell.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                }
                cell.setForeground(CHARCOAL);
                return cell;
            }
        };
        table.setRowHeight(rowHeight);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setFont(font(Font.PLAIN, 13));
        table.setDefaultRenderer(Object.class, new PaddedCellRenderer(SwingConstants.LEFT));

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(100, 32));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                    boolean focused, int row, int column) {
                super.getTableCellRendererComponent(t, value, false, false, row, column);
                setOpaque(true);
                setBackground(CHARCOAL);
                setForeground(Color.WHITE);
                setFont(font(Font.BOLD, 13));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                // Line the heading up with its column (right-aligned columns get right-aligned headings).
                TableCellRenderer columnRenderer = t.getColumnModel().getColumn(column).getCellRenderer();
                if (columnRenderer instanceof JLabel) {
                    setHorizontalAlignment(((JLabel) columnRenderer).getHorizontalAlignment());
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return this;
            }
        });
        return table;
    }

    /** Right-aligns one column (used for prices and quantities). */
    public static void alignRight(JTable table, int column) {
        table.getColumnModel().getColumn(column).setCellRenderer(new PaddedCellRenderer(SwingConstants.RIGHT));
    }

    /** Cell renderer with left/right padding so text does not touch the cell edge. */
    private static class PaddedCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        PaddedCellRenderer(int alignment) {
            setHorizontalAlignment(alignment);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, false, row, column);
            // The parent class resets the border on every call, so set our padding afterwards.
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return this;
        }
    }
}
