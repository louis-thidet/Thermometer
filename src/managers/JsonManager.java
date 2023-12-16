package managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonManager {
    
	// Declaration of global variables
	private static List<double[]> temperatureHistory = new ArrayList<>(); // List to keep the observations loaded during the session and show them in the chart
    private static String streamId = "xxxx"; // Id of the observation's stream
    private static String url = "https://octave-api.sierrawireless.io/v5.0/particulier/event/" + streamId + "?start=0"; // URL of API. '?start=0' is to retrieve last value only
    private static String authToken = "xxxx"; // account token
    private static String authUser = "xxxx"; // account name
    
    // Don't forget to replace xxxx by proper values!
    
	// ================================================
    // ===== RETRIEVE DATA FROM OCTAVE'S REST API =====
	// ================================================
    
    public static String fetchData() throws IOException {
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("GET");

        // Set headers
        connection.setRequestProperty("X-Auth-Token", authToken);
        connection.setRequestProperty("X-Auth-User", authUser);

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read the response
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return response.toString();
            }
        } else {
            throw new IOException("HTTP error! Status: " + responseCode);
        }
    }

	// ==============================================================================
    // ===== RETRIEVE THE TEMPERATURE AND THE TIME OF THE OBSERVATION FROM JSON =====
	// ==============================================================================
    
    public static double[] getTemperatureFromJson(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);

        // Assuming you want the temperature from the first item in the body array
        JSONArray bodyArray = jsonObject.getJSONArray("body");
        JSONObject firstItem = bodyArray.getJSONObject(0);
        JSONObject environment = firstItem.getJSONObject("elems").getJSONObject("environment");
        double temperature = environment.getDouble("temperature");

        // Get the time value
        long generatedDate = firstItem.getLong("generatedDate");

        // Add the new temperature and time values to the history
        temperatureHistory.add(new double[]{temperature, generatedDate});
        
        // Each time an observation is made, it's saved in an history file
        String time = TimeManager.formatTime(generatedDate);
        HistoryManager.updateHistory(temperature + "," + time + "," + TimeManager.getDate());

        // Ensure the size of temperatureHistory does not exceed 10
        if (temperatureHistory.size() > 10) {
            temperatureHistory.remove(0); // Remove the oldest value
        }

        return new double[]{temperature, generatedDate};
    }

	// ==========================================
    // ===== CREATE A DATASET FOR THE CHART =====
	// ==========================================
    
    public static CategoryDataset createDataset() {
        // Create a dataset using the temperature history
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		// Add all temperature and time values to the dataset
		for (int i = 0; i < temperatureHistory.size(); i++) {
		    double temperature = temperatureHistory.get(i)[0];
		    long time = (long) temperatureHistory.get(i)[1];

		    // Convert time to HH:MM format (you may need to adjust this based on your requirements)
		    String timeLabel = TimeManager.formatTime(time);

		    dataset.addValue(temperature, "Temperature", timeLabel);
		}
		return dataset;
    }



}
