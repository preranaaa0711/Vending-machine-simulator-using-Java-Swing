import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Main application class containing the Vending Machine Simulator.
 * * **OOP Concepts Used:**
 * 1.  **Inheritance:** `VendingMachineApp` extends `JFrame` to inherit all GUI window properties.
 * 2.  **Encapsulation:** Data fields like `inventory`, `cart`, and `customerBalance` are private within the `VendingMachineController` class, accessed only through public getter/setter methods.
 * 3.  **Abstraction:** The inner classes (`Product`, `VendingMachineController`, `GradientPanel`, `ButtonRenderer`, `ButtonEditor`) hide complex implementation details (like CSV I/O or custom painting) from the main GUI logic.
 * 4.  **Composition/Aggregation:** `VendingMachineApp` *has a* `VendingMachineController` instance, delegating all business logic to it (a key component of the Model-View-Controller pattern).
 * * **GUI Overview:**
 * The application uses a central `CardLayout` to switch between two main modes:
 * -   **Customer View:** Displays product cards, a cart summary, total cost, current balance, and transaction buttons (select, insert money, checkout).
 * -   **Admin View:** Displays a dashboard with a table showing product stock, sales, and a refill button for inventory management.
 * The design uses custom components (`GradientPanel`) and color schemes (Teal, Cyan, Dark Blue) for a modern look.
 */
public class vendingmachine extends JFrame {

    // --- Static Constants ---
    private static final String APP_TITLE = "The Snack Shack";
    private static final Font VERDANA_FONT = new Font("Verdana", Font.PLAIN, 14);
    private static final Font VERDANA_BOLD = new Font("Verdana", Font.BOLD, 16);
    private static final Color TEAL_COLOR = new Color(0, 150, 136); // Specific Teal
    private static final Color CYAN_COLOR = new Color(0, 188, 212); // Specific Cyan
    private static final Color DARK_BLUE_HEADER = new Color(13, 71, 161); // Dark Blue

    // --- Instance Fields (GUI Components) ---
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel customerPanel;
    private JPanel adminPanel;
    private JPanel productsContainerPanel; // New field to hold the product grid

    private JTextArea cartDisplayArea;
    private JLabel totalCostLabel;
    private JLabel balanceLabel;

    private JTable adminTable;
    private DefaultTableModel adminTableModel;

    // --- Controller Instance ---
    private final VendingMachineController controller;

    /**
     * Constructor sets up the application frame and all components.
     */
    public vendingmachine() {
        // Initialize the controller first
        controller = new VendingMachineController();

        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));

        // Use CardLayout to switch between Customer and Admin views
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Build the two main panels
        customerPanel = createCustomerPanel();
        adminPanel = createAdminPanel();

        mainPanel.add(customerPanel, "CUSTOMER");
        mainPanel.add(adminPanel, "ADMIN");
        cardLayout.show(mainPanel, "CUSTOMER"); // Start with the customer view

        // Add the main header and content panel
        setLayout(new BorderLayout());
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Center the frame on the screen
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // =========================================================================
    // 1. GUI Component Factory Methods (Frontend/View)
    // =========================================================================

    /**
     * Creates the main header panel with the application title.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(Color.WHITE); // White box/background for the text
        headerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel titleLabel = new JLabel(APP_TITLE);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 30));
        titleLabel.setForeground(DARK_BLUE_HEADER);
        headerPanel.add(titleLabel);
        return headerPanel;
    }

    /**
     * Creates the main Customer Panel view.
     */
    private JPanel createCustomerPanel() {
        // Use the custom gradient panel as the background
        GradientPanel customerMainPanel = new GradientPanel(CYAN_COLOR, TEAL_COLOR);
        customerMainPanel.setLayout(new BorderLayout(10, 10));
        customerMainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // A welcome message at the bottom
        JLabel welcomeLabel = new JLabel("Welcome. Please select your items.");
        welcomeLabel.setFont(VERDANA_FONT.deriveFont(Font.ITALIC, 16f));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Center panel for products and cart
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false); // See through to the gradient background

        // 1. Products Panel (Left) - This is the container that will be refreshed
        productsContainerPanel = createProductsPanel();
        centerPanel.add(productsContainerPanel);

        // 2. Cart and Transaction Panel (Right)
        centerPanel.add(createCartPanel());

        customerMainPanel.add(centerPanel, BorderLayout.CENTER);
        customerMainPanel.add(welcomeLabel, BorderLayout.SOUTH);

        return customerMainPanel;
    }

    /**
     * Creates the panel containing all clickable product boxes.
     */
    private JPanel createProductsPanel() {
        JPanel productsPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        productsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                "Vending Machine Selections",
                0, 0, VERDANA_BOLD, Color.WHITE));
        productsPanel.setOpaque(false);

        // Dynamically create a card for each product
        for (Product product : controller.getInventory().values()) {
            productsPanel.add(createProductCard(product));
        }

        return productsPanel;
    }

    /**
     * Creates a single product display card with specific styling.
     */
    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(DARK_BLUE_HEADER, 2, true));

        // --- 1. Header Section (Dark Blue) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_BLUE_HEADER);
        header.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel nameLabel = new JLabel(product.getId() + " - " + product.getName());
        nameLabel.setFont(VERDANA_FONT.deriveFont(Font.BOLD, 14f));
        nameLabel.setForeground(Color.WHITE);
        header.add(nameLabel, BorderLayout.CENTER);
        card.add(header, BorderLayout.NORTH);

        // --- 2. Body Section (Details) ---
        JPanel body = new JPanel(new GridLayout(3, 1, 5, 5));
        body.setBorder(new EmptyBorder(5, 5, 5, 5));
        body.setBackground(Color.WHITE);

        // Price Label (Teal Color)
        JLabel priceLabel = new JLabel("AED " + product.getFormattedPrice());
        priceLabel.setFont(VERDANA_BOLD);
        priceLabel.setForeground(TEAL_COLOR);
        priceLabel.setHorizontalAlignment(SwingConstants.LEFT);
        body.add(priceLabel);

        // Stock Label
        JLabel stockLabel = new JLabel("In Stock (" + product.getStock() + ")");
        stockLabel.setFont(VERDANA_FONT.deriveFont(12f));

        // Out of Stock Logic: Display warning and disable the button
        if (product.getStock() <= 0) {
            stockLabel.setText("OUT OF STOCK");
            stockLabel.setForeground(Color.RED.darker());
            stockLabel.setFont(VERDANA_BOLD.deriveFont(12f));
        } else if (product.getStock() <= 5) {
            // Nearing zero stock warning
            stockLabel.setForeground(Color.ORANGE.darker());
        }

        body.add(stockLabel);

        // --- 3. Select Button (Black Box, White Text) ---
        JButton selectButton = createStyledButton("SELECT", DARK_BLUE_HEADER.darker()); // Darker blue for contrast
        selectButton.addActionListener(e -> handleSelectProduct(product.getId()));

        if (product.getStock() <= 0) {
            selectButton.setEnabled(false);
            selectButton.setText("UNAVAILABLE");
        }

        body.add(selectButton);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    /**
     * Creates the cart display, transaction area, and control buttons.
     */
    private JPanel createCartPanel() {
        JPanel cartPanel = new JPanel(new BorderLayout(10, 10));
        cartPanel.setOpaque(false);

        // --- 1. Cart Display Area ---
        cartDisplayArea = new JTextArea("Your Cart:\n");
        cartDisplayArea.setEditable(false);
        cartDisplayArea.setFont(VERDANA_FONT);
        JScrollPane scrollPane = new JScrollPane(cartDisplayArea);
        scrollPane.setPreferredSize(new Dimension(300, 300));
        cartPanel.add(scrollPane, BorderLayout.NORTH);

        // --- 2. Transaction Summary and Controls ---
        JPanel summaryPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Total Cost Display
        totalCostLabel = new JLabel("AED 0.00");
        summaryPanel.add(createDisplayField("TOTAL COST:", totalCostLabel));

        // Balance Display
        balanceLabel = new JLabel("AED 0.00");
        summaryPanel.add(createDisplayField("YOUR BALANCE:", balanceLabel));

        // Action Buttons Row 1 (Money / Remove)
        JPanel buttonRow1 = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonRow1.setOpaque(false);
        JButton insertMoneyButton = createStyledButton("INSERT MONEY", Color.BLACK);
        insertMoneyButton.addActionListener(e -> showMoneyInsertionDialog());
        JButton removeButton = createStyledButton("REMOVE SELECTED", Color.BLACK);
        removeButton.addActionListener(e -> handleRemoveSelected());
        buttonRow1.add(insertMoneyButton);
        buttonRow1.add(removeButton);
        summaryPanel.add(buttonRow1);

        // Action Buttons Row 2 (Checkout / Clear Cart / Admin)
        JPanel buttonRow2 = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonRow2.setOpaque(false);
        JButton checkoutButton = createStyledButton("CHECKOUT", Color.BLACK);
        checkoutButton.addActionListener(e -> handleCheckout());
        JButton clearCartButton = createStyledButton("CLEAR CART", Color.BLACK);
        clearCartButton.addActionListener(e -> handleClearCart());
        JButton adminLoginButton = createStyledButton("ADMIN LOGIN", Color.GRAY.darker());
        adminLoginButton.addActionListener(e -> showAdminLoginDialog());
        buttonRow2.add(checkoutButton);
        buttonRow2.add(clearCartButton);
        buttonRow2.add(adminLoginButton);
        summaryPanel.add(buttonRow2);

        cartPanel.add(summaryPanel, BorderLayout.CENTER);
        return cartPanel;
    }

    /**
     * Helper to create a display field (label + value box).
     */
    private JPanel createDisplayField(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // --- START OF REQUIRED CHANGE ---
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE); // Set the background to white
        // --- END OF REQUIRED CHANGE ---

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL_COLOR.darker(), 1),
                new EmptyBorder(5, 5, 5, 5)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(VERDANA_FONT.deriveFont(Font.BOLD));
        titleLabel.setForeground(TEAL_COLOR.darker());

        valueLabel.setFont(VERDANA_BOLD.deriveFont(20f));
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valueLabel.setForeground(DARK_BLUE_HEADER);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.EAST);
        return panel;
    }

    /**
     * Creates the Admin Panel view (Dashboard and controls).
     */
    private JPanel createAdminPanel() {
        // Use the custom gradient panel as the background
        GradientPanel adminMainPanel = new GradientPanel(CYAN_COLOR, TEAL_COLOR);
        adminMainPanel.setLayout(new BorderLayout(10, 10));
        adminMainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Admin Header
        JLabel headerLabel = new JLabel("Admin Dashboard");
        headerLabel.setFont(new Font("Verdana", Font.BOLD, 30));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        adminMainPanel.add(headerLabel, BorderLayout.NORTH);

        // Table Model Setup
        String[] columnNames = {"ID", "Name", "Price (AED)", "Stock", "Sales", "Action"};
        adminTableModel = new DefaultTableModel(columnNames, 0);
        adminTable = new JTable(adminTableModel);
        adminTable.setFont(VERDANA_FONT);
        adminTable.setRowHeight(25);
        adminTable.getTableHeader().setFont(VERDANA_BOLD.deriveFont(14f));

        // Custom renderer for the Stock column to highlight warnings
        adminTable.getColumnModel().getColumn(3).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel(value.toString());
            label.setOpaque(true);
            label.setFont(VERDANA_FONT);
            
            // Check if value is a number (it should be, but defensive check)
            int stock;
            try {
                stock = Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                stock = -1; // Default or error state
            }


            if (stock <= 0) {
                label.setBackground(new Color(255, 199, 199)); // Light Red
                label.setForeground(Color.RED.darker());
                label.setText(value.toString() + " (OUT)");
            } else if (stock <= 5) {
                label.setBackground(new Color(255, 245, 199)); // Light Yellow
                label.setForeground(Color.ORANGE.darker());
                label.setText(value.toString() + " (LOW)");
            } else {
                label.setBackground(Color.WHITE);
            }
            return label;
        });

        // Custom renderer/editor for the Refill Button column
        adminTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Refill"));
        // PASS THE JTABLE INSTANCE TO THE EDITOR
        adminTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(adminTable, (row) -> {
            // The row index received here is the model index
            String productId = (String) adminTableModel.getValueAt(row, 0);
            controller.refillProduct(productId);
            // Update table after refill
            populateAdminTable();
            JOptionPane.showMessageDialog(adminPanel, productId + " refilled successfully!", "Refill Success", JOptionPane.INFORMATION_MESSAGE);
        }));

        JScrollPane tableScrollPane = new JScrollPane(adminTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        adminMainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Control Panel (Save and Logout)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        controlPanel.setOpaque(false);
        JButton saveButton = createStyledButton("SAVE CHANGES", Color.BLACK);
        saveButton.addActionListener(e -> handleSaveAdminChanges());
        JButton logoutButton = createStyledButton("LOGOUT", Color.GRAY.darker());
        logoutButton.addActionListener(e -> handleLogout());
        controlPanel.add(saveButton);
        controlPanel.add(logoutButton);
        adminMainPanel.add(controlPanel, BorderLayout.SOUTH);

        // Initially populate the table
        populateAdminTable();

        return adminMainPanel;
    }

    /**
     * Populates the Admin Table with the current inventory data.
     */
    private void populateAdminTable() {
        adminTableModel.setRowCount(0); // Clear existing data
        for (Product p : controller.getInventory().values()) {
            adminTableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getFormattedPrice(),
                    p.getStock(),
                    p.getSales(),
                    "Refill" // Button placeholder text
            });
        }
    }

    // --- Utility Methods for Styling ---

    /**
     * Creates a JButton with the black box/white text style.
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(VERDANA_BOLD.deriveFont(12f));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                new EmptyBorder(10, 15, 10, 15)));
        return button;
    }

    // =========================================================================
    // 2. Customer Panel Logic (Event Handlers)
    // =========================================================================

    /**
     * Handles adding a product to the cart.
     */
    private void handleSelectProduct(String id) {
        String message = controller.addToCart(id);
        JOptionPane.showMessageDialog(this, message, "Selection Status", JOptionPane.INFORMATION_MESSAGE);
        updateCustomerView();
    }

    /**
     * Shows a dialog for the customer to insert money.
     */
    private void showMoneyInsertionDialog() {
        String[] options = {"1 AED", "5 AED", "10 AED", "20 AED", "50 AED", "100 AED"};
        String choice = (String) JOptionPane.showInputDialog(
                this,
                "Insert money:",
                "Insert Money",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice != null) {
            try {
                // Extract the numerical value (e.g., "1" from "1 AED")
                String valueStr = choice.split(" ")[0];
                int value = Integer.parseInt(valueStr);
                controller.insertMoney(new BigDecimal(value));
                updateCustomerView();
            } catch (NumberFormatException ex) {
                // Should not happen with defined options
            }
        }
    }

    /**
     * Handles removing a selected item from the cart (simplistic: removes the last added).
     */
    private void handleRemoveSelected() {
        String message = controller.removeFromCart();
        JOptionPane.showMessageDialog(this, message, "Cart Update", JOptionPane.INFORMATION_MESSAGE);
        updateCustomerView();
    }

    /**
     * Handles the final checkout process.
     */
    private void handleCheckout() {
        VendingMachineController.CheckoutResult result = controller.checkout();

        JOptionPane.showMessageDialog(this,
                result.message(),
                "Checkout Result",
                JOptionPane.INFORMATION_MESSAGE);

        // Update the GUI state based on the outcome
        if (result.success()) {
            // 1. Re-render product cards to show new stock levels
            refreshProductsPanel();
        }
        // 2. Update cart and balance display (happens for success or failure)
        updateCustomerView();
    }

    /**
     * Clears all items from the cart.
     */
    private void handleClearCart() {
        controller.clearCart();
        JOptionPane.showMessageDialog(this, "Cart cleared.", "Cart Update", JOptionPane.INFORMATION_MESSAGE);
        updateCustomerView();
    }

    /**
     * Updates the cart display, total cost, and customer balance labels.
     */
    private void updateCustomerView() {
        cartDisplayArea.setText(controller.getCartDisplay());
        totalCostLabel.setText("AED " + controller.getCartTotalFormatted());
        balanceLabel.setText("AED " + controller.getBalanceFormatted());
    }
    
    /**
     * Replaces the old products grid with a new one reflecting current stock levels.
     */
    private void refreshProductsPanel() {
        if (productsContainerPanel != null) {
            productsContainerPanel.removeAll();
            // Create and add the new products grid
            for (Product product : controller.getInventory().values()) {
                productsContainerPanel.add(createProductCard(product));
            }
            // Re-render
            productsContainerPanel.revalidate();
            productsContainerPanel.repaint();
        }
    }

    // =========================================================================
    // 3. Admin Panel Logic (Event Handlers)
    // =========================================================================

    /**
     * Shows the two-step admin login dialog.
     */
    private void showAdminLoginDialog() {
        String password = JOptionPane.showInputDialog(this, "Enter Admin Password:", "Admin Login - Step 1", JOptionPane.QUESTION_MESSAGE);
        if (password == null) return; // User cancelled

        if (controller.checkPassword(password)) {
            String pin = JOptionPane.showInputDialog(this, "Enter Admin PIN:", "Admin Login - Step 2", JOptionPane.QUESTION_MESSAGE);
            if (pin != null && controller.checkPin(pin)) {
                // Login successful
                cardLayout.show(mainPanel, "ADMIN");
                populateAdminTable();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid PIN.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles saving changes from the Admin Dashboard and updates customer view.
     */
    private void handleSaveAdminChanges() {
        try {
            // (No direct stock edits here, relying on refill logic)

            controller.saveProductsToCsv();
            JOptionPane.showMessageDialog(adminPanel, "Inventory saved to CSV successfully.", "Save Success", JOptionPane.INFORMATION_MESSAGE);

            // Update the customer's product view with any stock changes (e.g., after a refill)
            refreshProductsPanel(); 

        } catch (Exception e) {
            JOptionPane.showMessageDialog(adminPanel, "Error saving inventory: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles logging out of the Admin Dashboard.
     */
    private void handleLogout() {
        cardLayout.show(mainPanel, "CUSTOMER");
        JOptionPane.showMessageDialog(this, "Logged out of Admin Dashboard.", "Logout", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // 4. Inner Class: Product Model (OOP Concept)
    // =========================================================================

    /**
     * Model class for a Vending Machine Product.
     * Uses BigDecimal for precise currency representation (AED).
     */
    public class Product {
        private String id;
        private String name;
        private BigDecimal price;
        private int stock;
        private int sales;

        private static final DecimalFormat AED_FORMAT = new DecimalFormat("0.00");

        public Product(String id, String name, BigDecimal price, int stock, int sales) {
            this.id = id;
            this.name = name;
            this.price = price.setScale(2, RoundingMode.HALF_UP);
            this.stock = stock;
            this.sales = sales;
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
        public int getStock() { return stock; }
        public int getSales() { return sales; }

        // Setters / Modifiers
        public void setStock(int stock) { this.stock = stock; }

        public void dispense() {
            if (stock > 0) {
                stock--;
                sales++;
            }
        }

        public void refill() {
            // Refill to a fixed amount, e.g., 50
            this.stock = 50;
        }

        // Utility Methods
        public String getFormattedPrice() {
            return AED_FORMAT.format(price);
        }

        public String toCsvLine() {
            // ID,Name,Price,Stock,Sales
            return String.join(",",
                    id,
                    name,
                    price.toPlainString(),
                    String.valueOf(stock),
                    String.valueOf(sales)
            );
        }
    }

    // =========================================================================
    // 5. Inner Class: VendingMachineController (Backend/Controller) (OOP Concept)
    // =========================================================================

    /**
     * The Controller class managing all Vending Machine logic.
     */
    public class VendingMachineController {
        private static final String CSV_FILE = "vending_inventory.csv";
        private final Map<String, Product> inventory = new LinkedHashMap<>();
        private final List<Product> cart = new ArrayList<>();
        private BigDecimal customerBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // Admin Credentials
        private static final String ADMIN_PASSWORD = "manager123";
        private static final String ADMIN_PIN = "1234";

        public record CheckoutResult(boolean success, String message, BigDecimal change) {}

        public VendingMachineController() {
            loadProductsFromCsv();
        }

        // --- CSV I/O and Persistence ---

        private void loadProductsFromCsv() {
            File file = new File(CSV_FILE);
            if (file.exists() && file.length() > 0) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    br.readLine(); // Skip header
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",", -1);
                        if (data.length == 5) {
                            String id = data[0].trim();
                            String name = data[1].trim();
                            BigDecimal price = new BigDecimal(data[2].trim()).setScale(2, RoundingMode.HALF_UP);
                            int stock = Integer.parseInt(data[3].trim());
                            int sales = Integer.parseInt(data[4].trim());
                            inventory.put(id, new Product(id, name, price, stock, sales));
                        }
                    }
                } catch (IOException | NumberFormatException e) {
                    System.err.println("Error reading inventory CSV, creating default: " + e.getMessage());
                    createDefaultInventory();
                }
            } else {
                createDefaultInventory();
            }
        }

        public void saveProductsToCsv() {
            try (FileWriter fw = new FileWriter(CSV_FILE)) {
                // Write header
                fw.write("ID,Name,Price,Stock,Sales\n");
                // Write data
                for (Product p : inventory.values()) {
                    fw.write(p.toCsvLine() + "\n");
                }
            } catch (IOException e) {
                System.err.println("Error saving inventory CSV: " + e.getMessage());
            }
        }

        private void createDefaultInventory() {
            inventory.clear();
            // Initial stock is 20 for all, initial sales is 0.
            inventory.put("A1", new Product("A1", "Maxafi Water (500ml)", new BigDecimal("1.00"), 20, 0));
            inventory.put("A2", new Product("A2", "Pepsi Max (Can)", new BigDecimal("3.50"), 20, 0));
            inventory.put("B1", new Product("B1", "Laban Up (Small)", new BigDecimal("4.00"), 20, 0));
            inventory.put("B2", new Product("B2", "Areej Juice (Orange)", new BigDecimal("3.00"), 20, 0));
            inventory.put("C1", new Product("C1", "Galaxy Chocolate", new BigDecimal("5.50"), 20, 0));
            inventory.put("C2", new Product("C2", "Oman Chips", new BigDecimal("1.50"), 20, 0));
            inventory.put("D1", new Product("D1", "Hot Coffee (Cappuccino)", new BigDecimal("8.00"), 20, 0));
            inventory.put("D2", new Product("D2", "Red Bull (Can)", new BigDecimal("10.00"), 20, 0));
            inventory.put("E1", new Product("E1", "Almarai Milk (200ml)", new BigDecimal("2.50"), 20, 0));
            inventory.put("E2", new Product("E2", "Snickers Bar", new BigDecimal("4.50"), 20, 0));
            inventory.put("F1", new Product("F1", "Pringles (Small)", new BigDecimal("6.00"), 20, 0));
            inventory.put("F2", new Product("F2", "KitKat (4 Finger)", new BigDecimal("4.00"), 20, 0));
            saveProductsToCsv(); // Save the default inventory
        }

        // --- Customer Logic ---

        /**
         * Adds a product to the cart if in stock.
         */
        public String addToCart(String id) {
            Product p = inventory.get(id);
            if (p == null) {
                return "Error: Invalid product ID.";
            }

            // Check if the actual inventory stock is > 0
            if (p.getStock() <= 0) {
                return "Sorry, " + p.getName() + " is currently out of stock.";
            }

            // Check if there is enough stock for one more in the cart
            long currentInCart = cart.stream().filter(item -> item.getId().equals(id)).count();
            if (currentInCart >= p.getStock()) {
                return "Sorry, you have selected the entire available stock of " + p.getName() + ".";
            }

            cart.add(p);
            return p.getName() + " added to cart.";
        }

        /**
         * Removes the last added item from the cart (simplistic removal).
         */
        public String removeFromCart() {
            if (cart.isEmpty()) {
                return "Your cart is empty.";
            }
            Product removed = cart.remove(cart.size() - 1);
            return removed.getName() + " removed from cart.";
        }

        /**
         * Inserts money into the machine.
         */
        public void insertMoney(BigDecimal amount) {
            customerBalance = customerBalance.add(amount).setScale(2, RoundingMode.HALF_UP);
        }

        /**
         * Finalizes the purchase transaction.
         */
        public CheckoutResult checkout() {
            BigDecimal totalCost = getCartTotal();
            BigDecimal change = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            if (cart.isEmpty()) {
                return new CheckoutResult(false, "Cart is empty. Please select products.", BigDecimal.ZERO);
            }

            if (customerBalance.compareTo(totalCost) < 0) {
                BigDecimal needed = totalCost.subtract(customerBalance);
                return new CheckoutResult(false, String.format("Insufficient funds. Need AED %.2f more.", needed), BigDecimal.ZERO);
            }

            // 1. Process Sale: Dispense all items and update inventory/sales
            Map<String, Integer> itemsToDispense = new HashMap<>();
            for (Product item : cart) {
                itemsToDispense.put(item.getId(), itemsToDispense.getOrDefault(item.getId(), 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : itemsToDispense.entrySet()) {
                Product p = inventory.get(entry.getKey());
                for (int i = 0; i < entry.getValue(); i++) {
                    p.dispense(); // Decrements stock, increments sales
                }
            }

            // 2. Calculate and Return Change
            change = customerBalance.subtract(totalCost).setScale(2, RoundingMode.HALF_UP);
            customerBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            cart.clear();

            // 3. Save changes immediately
            saveProductsToCsv();

            if (change.compareTo(BigDecimal.ZERO) > 0) {
                return new CheckoutResult(true, String.format("Purchase successful. Dispensing items and change: AED %.2f", change), change);
            } else {
                return new CheckoutResult(true, "Purchase successful. Dispensing items. Exact change.", change);
            }
        }

        /**
         * Clears the customer's cart.
         */
        public void clearCart() {
            cart.clear();
        }

        // --- Admin Logic ---

        public boolean checkPassword(String password) {
            return ADMIN_PASSWORD.equals(password);
        }

        public boolean checkPin(String pin) {
            return ADMIN_PIN.equals(pin);
        }

        public void refillProduct(String id) {
            Product p = inventory.get(id);
            if (p != null) {
                p.refill();
            }
        }

        // --- Getters for View/Display ---

        public Map<String, Product> getInventory() {
            return inventory;
        }

        public BigDecimal getCartTotal() {
            return cart.stream()
                    .map(Product::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        public String getCartTotalFormatted() {
            return new DecimalFormat("0.00").format(getCartTotal());
        }

        public String getBalanceFormatted() {
            return new DecimalFormat("0.00").format(customerBalance);
        }

        public String getCartDisplay() {
            if (cart.isEmpty()) return "Your Cart is Empty.";

            StringBuilder sb = new StringBuilder("Your Cart:\n");
            // Group by product name and count
            Map<String, Long> counts = cart.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Product::getName, java.util.stream.Collectors.counting()));

            for (Map.Entry<String, Long> entry : counts.entrySet()) {
                // Find the price for display
                Product p = inventory.values().stream()
                        .filter(item -> item.getName().equals(entry.getKey()))
                        .findFirst().orElse(null);

                if (p != null) {
                    BigDecimal itemCost = p.getPrice().multiply(new BigDecimal(entry.getValue()));
                    sb.append(String.format(" - %s (x%d) @ AED %.2f\n", entry.getKey(), entry.getValue(), itemCost));
                }
            }
            return sb.toString();
        }
    }

    // =========================================================================
    // 6. Inner Class: Custom Components (OOP Concept)
    // =========================================================================

    /**
     * Custom JPanel implementation to draw the cyan/teal gradient background.
     */
    class GradientPanel extends JPanel {
        private final Color color1;
        private final Color color2;

        public GradientPanel(Color color1, Color color2) {
            this.color1 = color1;
            this.color2 = color2;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth();
            int h = getHeight();

            // Diagonal gradient from top-left (cyan) to bottom-right (teal)
            GradientPaint gp = new GradientPaint(
                    0, 0, color1,
                    w, h, color2);

            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
        }
    }

    /**
     * Custom Button Renderer for the Admin Table's Refill button column.
     */
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer(String text) {
            setText(text);
            setFont(VERDANA_BOLD.deriveFont(12f));
            setBackground(Color.BLACK);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setRolloverEnabled(false);
            setBorder(new EmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    /**
     * Custom Button Editor for the Admin Table's Refill button column,
     * allowing it to trigger an action when clicked.
     */
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private final ActionListener actionListener;
        private final JTable table;
        
        // Change: Pass the JTable instance here.
        public ButtonEditor(JTable table, java.util.function.Consumer<Integer> action) {
            super(new JTextField()); // Pass an arbitrary component, but we won't use it
            setClickCountToStart(1); 
            this.table = table;

            button = new JButton();
            button.setFont(VERDANA_BOLD.deriveFont(12f));
            button.setBackground(Color.BLACK);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(5, 10, 5, 10));
            
            this.actionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // FIX: Capture the row index BEFORE firing editing stopped.
                    // table.getEditingRow() gives the view row currently being edited.
                    int viewRow = table.getEditingRow();
                    if (viewRow != -1) {
                         // Convert the view row to the model row (important if the table is sorted)
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        // 1. Perform the action
                        action.accept(modelRow);
                    }
                    // 2. Stop the editing process
                    fireEditingStopped();
                }
            };
            button.addActionListener(actionListener);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            // This method is primarily used to return the value that should be put into the model cell.
            // Since this is a button action, we just return the label.
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    // =========================================================================
    // 7. Main Method
    // =========================================================================

    public static void main(String[] args) {
        // Ensure GUI runs on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new vendingmachine());
    }
}