WYNYARD KEBAB SHOP - JAVA SWING ORDERING SYSTEM
================================================

Assessment 4: Java GUI application (AWT / Swing, event-driven programming)

The application has two separate GUIs that share the same live shop data:

  1. Customer Ordering GUI   (CustomerGUI.java)
  2. Staff / Assistant GUI   (AssistantGUI.java)

The menu is EMPTY when the program starts. Staff add items manually. For
presentation convenience there is also a "Load Wynyard Sample Menu" button,
which is only enabled while the menu is empty.


HOW TO RUN
----------
Option A - Eclipse
  1. Extract the ZIP file.
  2. In Eclipse: File > Import > General > Existing Projects into Workspace.
  3. Select the extracted WynyardKebabShop folder and click Finish.
  4. Open src > kebabshop > Main.java.
  5. Right-click Main.java > Run As > Java Application.

Option B - VS Code
  1. Install Java (JDK 17 or newer) and the "Extension Pack for Java" in VS Code.
  2. File > Open Folder... and choose the WynyardKebabShop folder.
  3. Open src > kebabshop > Main.java.
  4. Click the "Run" link shown above "public static void main" (or press F5).

Option C - double-click / command line (needs Java 17 or newer)
  java -jar WynyardKebabShop.jar


ASSIGNMENT REQUIREMENTS -> WHERE THEY ARE IMPLEMENTED
-----------------------------------------------------
1. Two GUIs. Customers order; shop assistants add items, update prices,
   delete items and process orders in creation-time order. The item list
   is initially empty.
     - CustomerGUI.java / AssistantGUI.java (the two windows)
     - AssistantGUI: addMenuItem(), updateMenuItem() (includes the price),
       deleteMenuItem()
     - ShopData.menuItems starts as an empty ArrayList

2. A customer selects any items and specifies the quantity of each
   (e.g. two Chicken Kebabs).
     - CustomerGUI.addSelectedItemToCart() using the Quantity spinner
     - Adding the same item again increases its quantity

3. Clicking "Finish Order" completes the order and adds it to the END of
   the order list (payment is ignored, as instructed).
     - CustomerGUI.finishOrder() -> ShopData.addOrder()
     - ShopData.pendingOrders is a Queue (LinkedList): first in, first out

4. Assistants process the orders in the order list. Clicking "Order
   Completed" removes the order from the list, adds it to the list of
   processed orders, and the next order is displayed.
     - AssistantGUI.completeCurrentOrder() -> ShopData.completeNextOrder()
       (poll() from the pending queue, add to completedOrders)
     - AssistantGUI.refreshOrders() shows the new front order
       ("Now serving") and the remaining orders ("Up next")

5. Assistants can view all previous orders.
     - "Previous Orders" tab: table of every processed order; click a row
       to see exactly what was ordered, the notes and the times

6. User-friendly, good-looking GUIs.
     - Theme.java: charcoal / gold / cream colours, rounded buttons with a
       hover effect, striped tables, consistent layout on every screen
     - Live updates in both windows, input validation with clear messages,
       disabled buttons when an action is not possible, status bar


EXTRA FEATURES
--------------
- Shop name and Australian dollar prices
- Customer name (optional) and special instructions on every order
- Live customer-to-staff updates using a change listener (observer pattern)
- Double-click a menu item to add it quickly
- "Up next" queue so staff can see every waiting order, not only the first
- Duplicate menu-item checking (not case sensitive)
- Strict price validation (numbers only, up to 2 decimal places)
- A placed order keeps the price the customer agreed to, even if staff
  change the menu price afterwards
- If staff delete an item or change a price while a customer is choosing,
  the customer's cart updates and shows a notice
- Order numbers, placed/completed times and totals on every order


SAMPLE MENU (only loads when the menu is empty)
-----------------------------------------------
Chicken Kebab  $13.90
Lamb Kebab     $14.90
Mixed Kebab    $15.90
Falafel Kebab  $12.50
Snack Pack     $17.50
Hot Chips      $6.00
Soft Drink     $3.50


MAIN CLASSES
------------
Main.java          Starts the program: creates the shared ShopData and the launcher window.
Theme.java         Colours, fonts, styled buttons/tables shared by all windows.
Money.java         Formats prices as dollars, e.g. $13.90.
MenuItem.java      One item on the menu (name, description, price).
OrderItem.java     One line of an order: an item plus its quantity.
Order.java         One customer order: name, notes, items, times, total.
ShopData.java      Shared menu, pending-order queue and completed-order list;
                   tells the GUIs when something changes.
CustomerGUI.java   Customer-facing ordering window.
AssistantGUI.java  Staff-facing menu and order-management window.

Event listeners worth pointing out in the video:
  - ActionListener on every button (lambda expressions)
  - MouseListener on the menu table (double-click to add)
  - ListSelectionListener on the menu table (fills the edit form) and on the
    previous-orders table (shows order details)
  - WindowListener (removes a window's listener from ShopData when closed)
  - ShopData.addChangeListener(...) - keeps both windows in sync


SUGGESTED VIDEO DEMO (about 10 minutes)
---------------------------------------
1. Introduce yourself (you must appear in the video) and the project.
2. Start the program; explain the launcher and that the menu begins empty.
3. Open the Customer window: show the "no menu yet" message.
4. Open the Staff / Assistant Panel. Add one item manually, update its price,
   then delete it. Then click "Load Wynyard Sample Menu".
5. Back in the Customer window: the menu appeared automatically (live update).
6. Enter a customer name, add several items with different quantities,
   remove one, add a special instruction, click Finish Order.
7. Place a second order from another customer window.
8. In the staff window: show the current order, the "Up next" queue and the
   order-of-creation (first order first).
9. Click Order Completed: the next order is displayed, and the finished order
   appears under Previous Orders (click it to show the details).
10. Show the code in Eclipse while explaining: ShopData (queue + listeners),
    CustomerGUI.finishOrder(), AssistantGUI.completeCurrentOrder(), and a
    few event listeners. The code must be visible while you explain it.
11. If the video is larger than 20 MB, upload it to your KOI Google Drive,
    share it with your tutor and submit a file containing the link.


NOTES
-----
Payment is not included because the assessment says to ignore the payment process.
The sample menu is only for demonstration; any items and prices can be entered.
