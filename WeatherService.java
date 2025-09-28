import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WeatherService {
    private static final String API_KEY = "3b01e6387b254828b0560407252809";
    private static final String API_URL = "http://api.weatherapi.com/v1/current.json?key=";

    public String fetchWeatherData(String city) throws Exception {
        String urlString = API_URL + API_KEY + "&q=" + city + "&aqi=no";
        URI uri = URI.create(urlString);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP Error: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();
        return response.toString();
    }

    public String getWeatherDisplay(String jsonData) {
        try {
            JSONObject obj = new JSONObject(jsonData);
            if (obj.has("current")) {
                JSONObject current = obj.getJSONObject("current");
                double temp = current.getDouble("temp_c");
                String condition = current.getJSONObject("condition").getString("text").toLowerCase();
                String location = obj.getJSONObject("location").getString("name");
                String lastUpdated = current.getString("last_updated");
                return String.format(
                    "City: %s%nTemperature: %.1f°C%nCondition: %s%nLast Updated: %s%nChecked: 07:44 PM IST, Sep 28, 2025",
                    location, temp, condition, lastUpdated);
            }
            return "City not found or API error.";
        } catch (Exception e) {
            return "Error parsing weather data: " + e.getMessage();
        }
    }
}