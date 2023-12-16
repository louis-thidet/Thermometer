package gui;

import com.formdev.flatlaf.FlatLightLaf;
import jaco.mp3.player.MP3Player;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import managers.HistoryManager;
import managers.JsonManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleAnchor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class Main extends javax.swing.JFrame {
	
    // Declarations of global variables
    private double[] nonFormatedTemperature; // Keep the last temperature recorded
    private static MP3Player alarmSound; // Played when the temperature is below the minimum or above the maximum configured
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Scheduler to update the temperature recorded
    private static boolean connected = false; // Tell whether the program is connected to Octave's API or not
    
    // Used to handle wrong value entered in minimum and maximum check values
    private int oldMinValue = 18;
    private int oldMaxValue = 22;
    
	// =======================
    // ===== CONSTRUCTOR =====
	// =======================
    
    public Main() throws IOException {
        initComponents();
        
        // Create an history file if no one exist on the computer
        File file = new File(HistoryManager.getFilePath());
        if (!file.exists()) {
        	HistoryManager.createFile();
        }
        
        // Center the program's window on the middle of the screen
        centerFrameOnScreen();
        
        // Set the window's minimum size
        setMinimumSize(new java.awt.Dimension(650, 550));
        
        // Load alarm sound
        java.net.URL alarmURL = this.getClass().getResource("/assets/sounds/alarm_sound.mp3");
        alarmSound = new MP3Player(alarmURL);
        alarmSound.setRepeat(true);
        
        // Listener to control minimum temperature value when it's changed with slider
        minTempSlider.addChangeListener((ChangeEvent e) -> {
        	// Retrieve the new value
            double newMinValue = minTempSlider.getValue();
            // Retrieve the max value
            double maxValue = Double.parseDouble(maxTempBox.getText());
            // If the new min value is superior to max value
            if(newMinValue >  maxValue){
            	// An error message appear
                JOptionPane.showMessageDialog(
                    null,
                    "Minimum Value cannot be superior to Maximum Value.",
                    "Wrong Value selected",
                    JOptionPane.WARNING_MESSAGE
                );
                // The min value is sat back to to the former min value
                minTempSlider.setValue(oldMinValue);
                minTempBox.setText(oldMinValue+"");
            // If the new min value is valid
            } else {
            	oldMinValue = (int) newMinValue;
                // Updating labels
                minTempBox.setText(String.valueOf(minTempSlider.getValue()));
                updateLabels();
                // Updating chart, so the line corresponding to the value changes
                CategoryDataset dataset = JsonManager.createDataset();
                updateChart(dataset);
            }
        });
        
        // Listener to control minimum temperature value when it's changed with text field
        minTempBox.addKeyListener(new KeyAdapter() {
        	@Override
        	public void keyReleased(KeyEvent e) {
        		// If the value entered is numeric
        		if(isNumeric(minTempBox.getText())) {
        			// Retrieve max value and new min value
            		double maxValue = Double.parseDouble(maxTempBox.getText());
            		double newMinValue = Double.parseDouble(minTempBox.getText());
            		// If new min value is superior to max value
        			if(newMinValue > maxValue) {
        				// An error message appear
                        JOptionPane.showMessageDialog(
                            null,
                            "Minimum Value cannot be superior to Maximum Value.",
                            "Wrong Value selected",
                            JOptionPane.WARNING_MESSAGE
                        );
                        // The former value is put back in the textfield
                        minTempBox.setText(oldMinValue+"");
                     // If the new max value is valid
        			} else {
        				// Updating slider
        				oldMinValue = (int) newMinValue;
                        minTempSlider.setValue((int) newMinValue);
        			}
        		// If the value entered is not numeric
        		} else {
        			// The former value is put back in the textfield
        			minTempBox.setText(oldMinValue+"");
        		}
        	}
        });
        
        
        // Listener to control maximum temperature value when it's changed with slider
        maxTempSlider.addChangeListener((ChangeEvent e) -> {
        	// Retrieve the new value
            double newMaxValue = maxTempSlider.getValue();
            // Retrieve the min value
            double minValue = Double.parseDouble(minTempBox.getText());
            // If the new max value is inferior to min value
            if(newMaxValue <  minValue){
                // An error message appear
                JOptionPane.showMessageDialog(
                    null,
                    "Maximum Value cannot be inferior to Minimum Value.",
                    "Wrong Value selected",
                    JOptionPane.WARNING_MESSAGE
                );
                // The max value is sat back to to the former max value
                maxTempSlider.setValue(oldMaxValue);
                maxTempBox.setText(oldMaxValue+"");
            // If the new max value is valid
            } else {
            	oldMaxValue = (int) newMaxValue;
            	// Updating labels
                maxTempBox.setText(String.valueOf(maxTempSlider.getValue()));
                updateLabels();
                // Updating chart, so the line corresponding to the value changes
                CategoryDataset dataset = JsonManager.createDataset();
                updateChart(dataset);
            }
        });
        
        // Listener to control maximum temperature value when it's changed with text field
        maxTempBox.addKeyListener(new KeyAdapter() {
        	@Override
        	public void keyReleased(KeyEvent e) {
        		// If the value entered is numeric
        		if(isNumeric(maxTempBox.getText())) {
        			// Retrieve min value and new max value
            		double minValue = Double.parseDouble(minTempBox.getText());
            		double newMaxValue = Double.parseDouble(maxTempBox.getText());
            		// If new max value is inferior to min value
        			if(newMaxValue < minValue) {
        				// An error message appear
                        JOptionPane.showMessageDialog(
                            null,
                            "Maximum Value cannot be superior to Minimum Value.",
                            "Wrong Value selected",
                            JOptionPane.WARNING_MESSAGE
                        );
                        // The former value is put back in the textfield
                        maxTempBox.setText(oldMaxValue+"");
                    // If the new max value is valid
        			} else {
        				// Updating slider
        				oldMaxValue = (int) newMaxValue;
                        maxTempSlider.setValue((int) newMaxValue);
        			}
        		// If the value entered is not numeric
        		} else {
        			// The former value is put back in the textfield
        			maxTempBox.setText(oldMaxValue+"");
        		}
        	}
        });
        
        
        // Initial updateTemperature
        updateTemperature();
        
        // Schedule subsequent updates
        scheduler.scheduleAtFixedRate(this::updateTemperature, 60, 60, TimeUnit.SECONDS);
        
        // Set program Icon
        Image icon = new ImageIcon(this.getClass().getResource("/assets/images/thermometer.png")).getImage();
        this.setIconImage(icon);

        // Set frame title
        DecimalFormat df = new DecimalFormat("#.##");
        String temperature = df.format(nonFormatedTemperature[0]);
        setTitle(temperature + " °C");
    }

	// ================
    // ===== MAIN =====
	// ================

    @SuppressWarnings("unused")
	public static void main(String args[]) {
        // Set the FlatLightLaf look and feel
        FlatLightLaf.setup();
        
        // Checking if the program is correctly retrieving data of a stream
        try {
			String testFetching = JsonManager.fetchData();
			connected = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        // If the fetching failed
        if(!connected) {
            JOptionPane.showMessageDialog(
                null,
                "Failed to retrieve stream. Please check your IDs in JsonManager.java",
                "Failed to retrieve stream",
                JOptionPane.ERROR_MESSAGE
            );
            // The program is exited
            System.exit(0);
        }

        // Create and display the form
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Main().setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
	// =========================
    // ===== UPDATE LABELS =====
	// =========================

    private void updateLabels() {
        // Formatting temperature 
        DecimalFormat df = new DecimalFormat("#.##");
        String temperature = df.format(nonFormatedTemperature[0]);
        setTitle(temperature + " °C");

        // Retrieve min and max check values
        double minTempCheck = Double.parseDouble(minTempBox.getText());
        double maxTempCheck = Double.parseDouble(maxTempBox.getText());

        // If the temperature is superior to min check and inferior to max check
        if(nonFormatedTemperature[0] > minTempCheck && nonFormatedTemperature[0] < maxTempCheck){
        	// Everything is alright !
            tempLabel.setForeground(new Color(0, 100, 0));
            tempLabel.setText("Temperature : " + temperature);
            alarmSound.stop();
        // If the temperature is inferior to min check
        } else if(nonFormatedTemperature[0] < minTempCheck){
        	// It's too cold!
            tempLabel.setForeground(new Color(0, 180, 180));
            tempLabel.setText("Temperature : " + temperature + " (Too cold!)");
            alarmSound.play(); // The alarm sound is played
        // If the temperature is superior to max check
        } else {
        	// It's too hot!
            tempLabel.setForeground(Color.red);
            tempLabel.setText("Temperature : " + temperature + " (Too hot!)");
            alarmSound.play(); // The alarm sound is played
        }
    }
    
	// ==================================================================================
    // ===== CREATE A CHART THAT DISPLAYS THE TEMPERATURE'S EVOLUTION OVER THE TIME =====
	// ==================================================================================
    
    private void updateChart(CategoryDataset dataset) {
    	// Remove the previous chart created
        chartJPanel.removeAll();

        // Create a line chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Evolution of Temperature ",  // Chart title
                "Time",                       // X-axis label
                "Temperature °C",             // Y-axis label
                dataset,                      // Dataset
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Create a plot for the chart
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.getRenderer().setSeriesPaint(0, Color.yellow);
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2));

        // Find the minimum and maximum values for the time
        double minTemp = Double.MAX_VALUE;
        double maxTemp = Double.MIN_VALUE;

        for (int i = 0; i < dataset.getRowCount(); i++) {
            Comparable<?> rowKey = dataset.getRowKey(i);
            for (int j = 0; j < dataset.getColumnCount(); j++) {
                Comparable<?> columnKey = dataset.getColumnKey(j);
                double temperature = dataset.getValue(rowKey, columnKey).doubleValue();

                // Update min and max values
                minTemp = Math.min(minTemp, temperature);
                maxTemp = Math.max(maxTemp, temperature);
            }
        }

        // Set the default view of the plot
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(minTemp - 6, maxTemp + 6); // Adjust the padding as needed

        // Add a line corresponding to the minimum temperature check
        ValueMarker marker10 = new ValueMarker(oldMaxValue);
        marker10.setPaint(Color.RED);
        marker10.setLabel("Max Temperature Desired");
        marker10.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        marker10.setStroke(new BasicStroke(1.5f));
        plot.addRangeMarker(marker10);

        // Add a line corresponding to the maximum temperature check
        ValueMarker marker5 = new ValueMarker(oldMinValue);
        marker5.setPaint(new Color(0, 180, 180));
        marker5.setLabel("Min Temperature Desired");
        marker5.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        marker5.setStroke(new BasicStroke(1.5f));
        plot.addRangeMarker(marker5);

        // Create a chart panel to display the chart
        ChartPanel chartPanel = new ChartPanel(chart);

        // Allow to use mousewheel to change the chart's view
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(true);

        // Adding chartPanel to the proper JPanel
        chartPanel.setBorder(new EmptyBorder(10, 10, 40, 10));
        chartPanel.setBackground(Color.white);
        chartJPanel.add(chartPanel, BorderLayout.CENTER);

        // Updating gui
        chartJPanel.revalidate();
        chartJPanel.repaint();
    }
    
	// ====================================================
    // ===== CHECK WHETHER A STRING IS NUMERIC OR NOT =====
	// ====================================================
    
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
	// ===============================================================
    // ===== CENTER THE PROGRAM'S WINDOW ON MIDDLE OF THE SCREEN =====
	// ===============================================================

    private void centerFrameOnScreen() {
        // Get the dimensions of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Calculate the center position
        int centerX = (screenSize.width - getWidth()) / 2;
        int centerY = (screenSize.height - getHeight()) / 2;

        // Set the frame location
        setLocation(centerX, centerY);
    }

	// ==================================
    // ===== UPDATE THE TEMPERATURE =====
	// ==================================
    
    private void updateTemperature() {
        try {
        	System.out.println("Update");
            // Retrieve data
            String jsonResponse = JsonManager.fetchData();
            // Get last temperature
            nonFormatedTemperature = JsonManager.getTemperatureFromJson(jsonResponse);
            // Update labels
            updateLabels();
            // Update the chart
            CategoryDataset dataset = JsonManager.createDataset();
            updateChart(dataset);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
	// ==========================================
    // ===== GUI CODE GENERATED BY NETBEANS =====
	// ==========================================
    
    private javax.swing.JPanel chartJPanel;
    private javax.swing.JPanel chooseTempPanel;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField maxTempBox;
    private javax.swing.JLabel maxTempLabel;
    private javax.swing.JSlider maxTempSlider;
    private javax.swing.JTextField minTempBox;
    private javax.swing.JLabel minTempLabel;
    private javax.swing.JSlider minTempSlider;
    private javax.swing.JLabel tempLabel;
    
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        chartJPanel = new javax.swing.JPanel();
        chooseTempPanel = new javax.swing.JPanel();
        tempLabel = new javax.swing.JLabel();
        maxTempSlider = new javax.swing.JSlider();
        minTempSlider = new javax.swing.JSlider();
        minTempLabel = new javax.swing.JLabel();
        maxTempLabel = new javax.swing.JLabel();
        maxTempBox = new javax.swing.JTextField();
        minTempBox = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.CardLayout());

        jSplitPane1.setDividerSize(0);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setEnabled(false);

        chartJPanel.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setRightComponent(chartJPanel);

        chooseTempPanel.setBackground(new java.awt.Color(255, 255, 255));
        chooseTempPanel.setPreferredSize(new java.awt.Dimension(752, 457));

        tempLabel.setFont(new java.awt.Font("Source Sans Pro", 0, 24)); // NOI18N
        tempLabel.setText("Temperature : UNKNOWN");

        maxTempSlider.setMaximum(120);
        maxTempSlider.setMinimum(-20);
        maxTempSlider.setValue(22);

        minTempSlider.setMaximum(120);
        minTempSlider.setMinimum(-20);
        minTempSlider.setValue(18);

        minTempLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        minTempLabel.setText("Min Temperature");

        maxTempLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        maxTempLabel.setText("Max Temperature");

        maxTempBox.setText("22");
        maxTempBox.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        minTempBox.setText("18");
        minTempBox.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout chooseTempPanelLayout = new javax.swing.GroupLayout(chooseTempPanel);
        chooseTempPanel.setLayout(chooseTempPanelLayout);
        chooseTempPanelLayout.setHorizontalGroup(
            chooseTempPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(chooseTempPanelLayout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(chooseTempPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(chooseTempPanelLayout.createSequentialGroup()
                        .addGroup(chooseTempPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(chooseTempPanelLayout.createSequentialGroup()
                                .addComponent(minTempSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minTempBox, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(chooseTempPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(minTempLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                        .addGroup(chooseTempPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(chooseTempPanelLayout.createSequentialGroup()
                                .addComponent(maxTempSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(maxTempBox, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(chooseTempPanelLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(maxTempLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(58, 58, 58))
                    .addGroup(chooseTempPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(tempLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        chooseTempPanelLayout.setVerticalGroup(
            chooseTempPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(chooseTempPanelLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(tempLabel)
                .addGap(35, 35, 35)
                .addGroup(chooseTempPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minTempLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxTempLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(chooseTempPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minTempSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxTempSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxTempBox, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minTempBox, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(298, Short.MAX_VALUE))
        );

        minTempLabel.getAccessibleContext().setAccessibleName("Max Temperature");
        maxTempLabel.getAccessibleContext().setAccessibleName("Min Temperature");

        jSplitPane1.setLeftComponent(chooseTempPanel);

        getContentPane().add(jSplitPane1, "card2");

        pack();
    }
}
