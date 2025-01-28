import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class VehicleManagementSystem extends JFrame {
    private Connection connection;

    public VehicleManagementSystem() {
        setTitle("Vehicle Management System");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        JButton totalDistanceButton = new JButton("Total Distance Traveled");
        JButton detectAnomaliesButton = new JButton("Detect Sensor Anomalies");
        JButton maintenanceHistoryButton = new JButton("Get Maintenance History");
        JButton frequentTripsButton = new JButton("Find Frequent Trips");

        
        totalDistanceButton.addActionListener(e -> displayTotalDistance());
        detectAnomaliesButton.addActionListener(e -> displaySensorAnomalies());
        maintenanceHistoryButton.addActionListener(e -> displayMaintenanceHistory());
        frequentTripsButton.addActionListener(e -> displayFrequentTrips());

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2));
        buttonPanel.add(totalDistanceButton);
        buttonPanel.add(detectAnomaliesButton);
        buttonPanel.add(maintenanceHistoryButton);
        buttonPanel.add(frequentTripsButton);
        add(buttonPanel);
    

        connectToDatabase();
    }



    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Motorq", "root", "Alanraj@1234");
            System.out.println("Database connected successfully!");
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Print the stack trace for debugging
        }
    
    }

    private void displayTotalDistance() {
        String query = "SELECT v.make, v.model, o.name, SUM(t.distance_traveled) AS total_distance " +
                       "FROM Vehicles v " +
                       "JOIN Owners o ON v.owner_id = o.owner_id " +
                       "JOIN Trips t ON v.vehicle_id = t.vehicle_id " +
                       "WHERE t.start_time >= NOW() - INTERVAL 30 DAY " +
                       "GROUP BY v.make, v.model, o.name";
    
        // Log the query for debugging
        System.out.println("Executing query: " + query);
    
        executeQueryAndDisplay(query, new String[]{"Make", "Model", "Name", "Total_Distance"});
    }
    
    
    

    private void displaySensorAnomalies() {
        String query = "SELECT v.make, v.model, s.sensor_type, s.sensor_reading, s.timestamp " +
                       "FROM Vehicles v JOIN Sensors s ON v.vehicle_id = s.vehicle_id " +
                       "WHERE (s.sensor_type = 'speed' AND s.sensor_reading > 120) " +
                       "OR (s.sensor_type = 'fuel level' AND s.sensor_reading < 10)";
        executeQueryAndDisplay(query, new String[]{"Make", "Model", "Sensor_Type", "Sensor_Reading", "Timestamp"});
    }
    

    private void displayMaintenanceHistory() {
        String vehicleId = JOptionPane.showInputDialog("Enter Vehicle ID:");
        if (vehicleId != null && !vehicleId.trim().isEmpty()) {
            String query = "SELECT maintenance_type, maintenance_date, maintenance_cost " +
                           "FROM Maintenance WHERE vehicle_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, Integer.parseInt(vehicleId));
                executeQueryAndDisplay(stmt, new String[]{"Maintenance_Type", "Maintenance_Date", "Maintenance_Cost"});
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error fetching maintenance history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Vehicle ID entered.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    private void displayFrequentTrips() {
        String query = "SELECT v.vehicle_id, v.make, v.model, COUNT(t.trip_id) AS trip_count " +
                       "FROM Vehicles v JOIN Trips t ON v.vehicle_id = t.vehicle_id " +
                       "WHERE t.start_time >= NOW() - INTERVAL 7 DAY " +
                       "GROUP BY v.vehicle_id HAVING trip_count > 5";
        
        try {
            executeQueryAndDisplay(query, new String[]{"Vehicle_ID", "Make", "Model", "Trip_count"});
        } catch (Exception e) {
            // Handle any exception that might be thrown
            System.err.println("Error executing query: " + e.getMessage());
        }
    }

    /*private void executeQueryAndDisplay(String query, String[] columnNames) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            displayResults(rs, columnNames);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error executing query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }*/

    private void executeQueryAndDisplay(String query, String[] columnNames) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Executing query: " + query); // Debugging line
            displayResults(rs, columnNames);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error executing query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Print the stack trace for debugging
        }
    }
    

    private void executeQueryAndDisplay(PreparedStatement stmt, String[] columnNames) {
        try (ResultSet rs = stmt.executeQuery()) {
            displayResults(rs, columnNames);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error executing query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void displayResults(ResultSet rs, String[] columnNames) throws SQLException {
        Vector<String> columnVector = new Vector<>();
        for (String columnName : columnNames) {
            columnVector.add(columnName);
        }
    
        Vector<Vector<Object>> dataVector = new Vector<>();
        while (rs.next()) {
            Vector<Object> rowVector = new Vector<>();
            for (String columnName : columnNames) {
                rowVector.add(rs.getObject(columnName));
            }
            dataVector.add(rowVector);
        }
        
        System.out.println("Results found: " + dataVector.size()); // Debugging line
    
        if (dataVector.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    
        JTable table = new JTable(dataVector, columnVector);
        JOptionPane.showMessageDialog(this, new JScrollPane(table), "Results", JOptionPane.INFORMATION_MESSAGE);
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VehicleManagementSystem vms = new VehicleManagementSystem();
            vms.setVisible(true);
        });
    }
}