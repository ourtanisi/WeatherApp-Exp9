import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.json.JSONObject;

public class WeatherApp extends JFrame {
    private JTextField cityField;
    private JTextArea weatherLabel;
    private JLabel mapLabel;
    private JMapViewer mapViewer;
    private WeatherService weatherService = new WeatherService();

    public WeatherApp() {
        setTitle("Weather Information App");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(240, 248, 255));

        // Header
        JLabel headerLabel = new JLabel("Welcome to Weather Information App");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setHorizontalAlignment(JLabel.CENTER);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerLabel.setBackground(new Color(70, 130, 180));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setOpaque(true);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 248, 255));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel cityLabel = new JLabel("Enter City:");
        cityLabel.setFont(new Font("Arial", Font.BOLD, 16));
        inputPanel.add(cityLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        cityField = new JTextField(15);
        cityField.setFont(new Font("Arial", Font.PLAIN, 16));
        cityField.setBackground(Color.WHITE);
        cityField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        inputPanel.add(cityField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        JButton fetchButton = new JButton("Get Weather");
        fetchButton.setFont(new Font("Arial", Font.BOLD, 14));
        fetchButton.setBackground(new Color(70, 130, 180));
        fetchButton.setForeground(Color.WHITE);
        fetchButton.setFocusPainted(false);
        fetchButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        fetchButton.addActionListener(new FetchWeatherListener());
        inputPanel.add(fetchButton, gbc);

        weatherLabel = new JTextArea("Weather will be displayed here.", 5, 1);
        weatherLabel.setFont(new Font("Arial", Font.BOLD, 16));
        weatherLabel.setForeground(new Color(0, 100, 0));
        weatherLabel.setLineWrap(true);
        weatherLabel.setWrapStyleWord(true);
        weatherLabel.setOpaque(true);
        weatherLabel.setBackground(Color.WHITE);
        weatherLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        weatherLabel.setEditable(false);
        weatherLabel.setFocusable(false);

        JScrollPane scrollPane = new JScrollPane(weatherLabel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        mapViewer = new JMapViewer();
        mapViewer.setZoomControlsVisible(false);
        mapLabel = new JLabel("Map will be displayed here.");
        mapLabel.setHorizontalAlignment(JLabel.CENTER);
        mapLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, new JPanel() {
            {
                setLayout(new BorderLayout());
                add(mapViewer, BorderLayout.CENTER);
            }
        });
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.5);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Footer
        JLabel footerLabel = new JLabel("Contact: Tanisi Yadav | Developed by CSBS 2nd Yr SVU Team");
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerLabel.setHorizontalAlignment(JLabel.CENTER);
        footerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        footerLabel.setBackground(new Color(70, 130, 180));
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setOpaque(true);

        add(headerLabel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(footerLabel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private class FetchWeatherListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String city = cityField.getText().trim();
            if (!city.isEmpty()) {
                try {
                    String weatherData = weatherService.fetchWeatherData(city);
                    displayWeather(weatherData);
                } catch (Exception ex) {
                    weatherLabel.setText("Error fetching weather data: " + ex.getMessage());
                }
            } else {
                weatherLabel.setText("Please enter a city name.");
            }
        }
    }

    private void displayWeather(String jsonData) {
        try {
            JSONObject obj = new JSONObject(jsonData);
            if (obj.has("current")) {
                JSONObject current = obj.getJSONObject("current");
                double temp = current.getDouble("temp_c");
                String condition = current.getJSONObject("condition").getString("text").toLowerCase();
                String location = obj.getJSONObject("location").getString("name");
                String lastUpdated = current.getString("last_updated");
                double lat = obj.getJSONObject("location").getDouble("lat");
                double lon = obj.getJSONObject("location").getDouble("lon");

                weatherLabel.setText(weatherService.getWeatherDisplay(jsonData));
                displayWeatherVisual(condition);
                displayTemperatureGraph(temp);
                displayMap(lat, lon);
            } else {
                weatherLabel.setText("City not found or API error.");
                mapViewer.setDisplayPosition(new org.openstreetmap.gui.jmapviewer.Coordinate(0, 0), 1); // Default position with zoom level
                mapLabel.setText("No map available.");
            }
        } catch (Exception e) {
            weatherLabel.setText("Error parsing weather data: " + e.getMessage());
            mapLabel.setText("Map loading error.");
        }
    }

    private void displayWeatherVisual(String condition) {
        WeatherVisualStrategy strategy = getWeatherVisualStrategy(condition);
        mapLabel.setText("<html><center>" + strategy.getVisual() + "</center></html>");
    }

    private WeatherVisualStrategy getWeatherVisualStrategy(String condition) {
        if (condition.contains("sunny") || condition.contains("clear")) return new SunnyVisual();
        else if (condition.contains("rain")) return new RainVisual();
        else if (condition.contains("thunderstorm") || condition.contains("storm")) return new ThunderstormVisual();
        else if (condition.contains("cloud")) return new CloudyVisual();
        else return new DefaultVisual();
    }

    private void displayTemperatureGraph(double temp) {
        String chartCode = "chartjs\n" +
            "{\n" +
            "  \"type\": \"line\",\n" +
            "  \"data\": {\n" +
            "    \"labels\": [\"Now\"],\n" +
            "    \"datasets\": [{\n" +
            "      \"label\": \"Temperature (°C)\",\n" +
            "      \"data\": [" + temp + "],\n" +
            "      \"fill\": false,\n" +
            "      \"borderColor\": \"rgb(75, 192, 192)\",\n" +
            "      \"tension\": 0.1\n" +
            "    }]\n" +
            "  },\n" +
            "  \"options\": {\n" +
            "    \"scales\": {\n" +
            "      \"y\": {\n" +
            "        \"beginAtZero\": true\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
        System.out.println(chartCode); // Render in canvas panel
    }

    private void displayMap(double lat, double lon) {
        mapViewer.setDisplayPosition(new org.openstreetmap.gui.jmapviewer.Coordinate(lat, lon), 10);
        mapLabel.setVisible(false); // Hide fallback label when map is active
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherApp::new);
    }
}