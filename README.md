The Snack Shack: A Java Swing Vending Machine Simulator

This project is a desktop application simulating a modern vending machine, built using Java and the Swing framework. It demonstrates strong principles of Object-Oriented Programming (OOP) and a clear separation of concerns, providing distinct interfaces for both customers and administrators.

✨ Key Features

Dual Interface: Separate, visually distinct views for Customer Transactions and Admin Management.

Inventory Management: Tracks product stock, prices, and sales figures in real-time.

Transaction Logic: Handles complex transactions, including item selection, money insertion (using BigDecimal for precision), total cost calculation, and change dispensing.

Data Persistence: Inventory and sales data are persisted to a local CSV file (vending_inventory.csv) on every transaction or admin save, ensuring data survives application restarts.

Intuitive GUI: Uses custom rendering and visual feedback (e.g., color-coded stock warnings in the Admin view, gradient background) for an engaging user experience.

Admin Dashboard: Secure, password/PIN-protected dashboard allowing administrators to view stock levels, refill products, and monitor sales performance.

💻 Technology & Architecture
Language- Java
Framework- Swing(For desktop GUI)
Persistance- Csv file (for local storage)
Currency Management-  java.math.BigDecimal for accurate currency calculations (avoiding floating-point errors).


🧠 Object-Oriented Programming (OOP) Concepts

The simulator is designed around core OOP principles to ensure scalability and maintainability:

Encapsulation: Data fields (e.g., inventory, customerBalance, Product details) are kept private and accessed only through public methods (getters/setters).

Inheritance: The main application class (VendingMachineApp) inherits functionality from JFrame. Custom GUI components (GradientPanel, ButtonRenderer) inherit from Swing classes like JPanel and JButton.

Abstraction: The Product class provides a simple, high-level interface (dispense(), refill()) while hiding the internal logic of stock and sales manipulation. The CheckoutResult record abstracts the complex transaction outcome.

Composition/Aggregation: The VendingMachineApp (View) contains an instance of the VendingMachineController, delegating all business logic 

NOTE: This project is a single, self-contained Java file.

Prerequisites

Java Development Kit (JDK) 8 or newer.

Admin Requirements (for testing purposes)

Username: manager123
pin: 1234
