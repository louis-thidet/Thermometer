package managers;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeManager {

	// ===============================
    // ===== RETRIEVE LOCAL DATE =====
	// ===============================
	
	public static String getDate() {
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter);
        
        return formattedDate;
	}
	
	// =========================================
    // ===== FORMATTING OBSERVATION'S TIME =====
	// =========================================
	
    public static String formatTime(long milliseconds) {
        // Convert milliseconds to Date object
        Date date = new Date(milliseconds);

        // Create a SimpleDateFormat for the desired format (HH:mm)
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        // Format the date
        return sdf.format(date);
    }
}
