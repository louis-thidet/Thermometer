package managers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class HistoryManager {
	
	// ========================================
    // ===== RETRIEVE DIRECTORY FILE PATH =====
	// ========================================
	
	public static String getDirectoryPath() {
        return System.getProperty("user.home") + File.separator + ".thermometer";
	}
	
	// ======================================
    // ===== RETRIEVE HISTORY FILE PATH =====
	// ======================================
	
	public static String getFilePath() {
        String directoryPath = getDirectoryPath();
        String filePath;
        filePath = directoryPath + File.separator + "temperature_history.csv";
        return filePath;
	}
	
	// ===============================
    // ===== CREATE HISTORY FILE =====
	// ===============================

	public static void createFile() {
		String filePath = getFilePath();
		String headers[] = {"Temperature", "Time", "Date"};
		
		// Check if history directory exists
		File directory = new File(getDirectoryPath());
		
		// If directory doesn't exist, it's created
		if (!directory.exists()) {
			directory.mkdirs();
		}
		
    	// Trying to create the file requested
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println(String.join(",", headers));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	// ===============================
    // ===== UPDATE HISTORY FILE =====
	// ===============================
	
	public static void updateHistory(String values) {
		String filePath = getFilePath();
        // Create or append to the log file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            // Write the message to the log file
        	System.out.println("ok");
            writer.write(values);
            // Add a new line to separate each log entry
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
