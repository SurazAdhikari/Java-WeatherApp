import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Server {
    private static final int PORT = 1234;
    private static final String DB_URL = "jdbc:sqlite:/Users/xdzc0/Desktop/java weather/database.db";

    private ServerSocket serverSocket;
    private Connection connection;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server socket initialized and listening on port " + PORT);
        } catch (IOException e) {
            System.out.println("Failed to initialize server socket: " + e.getMessage());
        }
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to the SQLite database");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
        }
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleClientRequest(clientSocket); // Handle client request
            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    private void handleClientRequest(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String location = in.readLine();

            // Query the database to retrieve weather information based on the location
            String query = "SELECT * FROM weather WHERE location = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, location);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Weather information found
                    String weatherInfo = resultSet.getString("info");
                    out.println(weatherInfo);
                } else {
                    // Location not found
                    out.println("Location not found");
                }
            }

            clientSocket.close();
        } catch (IOException | SQLException e) {
            System.out.println("Error handling client request: " + e.getMessage());
        }
    }

    public void stop() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Disconnected from the database");
            }
            if (serverSocket != null) {
                serverSocket.close();
                System.out.println("Server socket closed");
            }
        } catch (IOException | SQLException e) {
            System.out.println("Error closing server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
        server.stop();
    }
}

/*
 * Use these location for data
 * ...> ('Kathmandu', 'Temperature: 28°C, Condition: Sunny'),
 * ...> ('Pokhara', 'Temperature: 25°C, Condition: Cloudy'),
 * ...> ('Chitwan', 'Temperature: 30°C, Condition: Rainy'),
 * ...> ('Lumbini', 'Temperature: 27°C, Condition: Thunderstorms'),
 * ...> ('Everest Base Camp', 'Temperature: -10°C, Condition: Snowy');
 */