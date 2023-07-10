import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class WeatherForecastApp extends JFrame {
    private JTextField locationField;
    private JButton submitButton;
    private JTextArea displayArea;

    private final String serverAddress = "localhost";
    private final int serverPort = 1234;

    public WeatherForecastApp() {
        super("Weather Forecast Application");
        initializeGUI();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setSize(780, 800);
    }

    private void initializeGUI() {
        locationField = new JTextField(20);
        submitButton = new JButton("Submit");
        displayArea = new JTextArea(10, 30);
        displayArea.setEditable(false);

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String location = locationField.getText();
                sendLocationToServer(location);
            }
        });

        JPanel panel = new JPanel();
        panel.add(new JLabel("Location: "));
        panel.add(locationField);
        panel.add(submitButton);
        this.add(panel);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(displayArea), BorderLayout.CENTER);
    }

    private void sendLocationToServer(String location) {
        try {
            Client client = new Client(serverAddress, serverPort);
            client.sendRequest(location, new ResponseHandler() {
                public void handleSuccessResponse(String response) {
                    updateGUIWithWeatherDetails(response);
                }

                public void handleNotFoundResponse() {
                    displayArea.setText("Location not found");
                }

                public void handleErrorResponse() {
                    displayArea.setText("Error occurred while retrieving data");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateGUIWithWeatherDetails(String response) {
        displayArea.setText(response);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new WeatherForecastApp();
            }
        });
    }

    private interface ResponseHandler {
        void handleSuccessResponse(String response);

        void handleNotFoundResponse();

        void handleErrorResponse();
    }

    private class Client {
        private String serverAddress;
        private int serverPort;

        public Client(String serverAddress, int serverPort) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
        }

        public void sendRequest(String location, ResponseHandler responseHandler) {
            try {
                Socket socket = new Socket(serverAddress, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println(location);
                String response = in.readLine();

                if (response.equals("Location not found")) {
                    responseHandler.handleNotFoundResponse();
                } else if (response.equals("Error occurred while retrieving data")) {
                    responseHandler.handleErrorResponse();
                } else {
                    responseHandler.handleSuccessResponse(response);
                }

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
