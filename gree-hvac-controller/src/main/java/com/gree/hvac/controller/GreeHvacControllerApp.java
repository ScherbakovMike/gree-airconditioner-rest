package com.gree.hvac.controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

/**
 * Main JavaFX application for controlling GREE HVAC devices
 */
@Slf4j
public class GreeHvacControllerApp extends Application {

    private Stage primaryStage;
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private boolean isMinimizedToTray = false;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;
            log.info("Starting GREE HVAC Controller application");
            
            // Prevent JavaFX from exiting when all windows are hidden
            Platform.setImplicitExit(false);
            
            // Initialize system tray
            initializeSystemTray();
            
            // Create the new UI layout
            VBox root = createEnhancedUI();
            
            // Create and configure the scene
            Scene scene = new Scene(root, 900, 500);
            
            // Configure the primary stage
            primaryStage.setTitle("GREE HVAC Controller");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(450);
            
            // Handle close request - minimize to tray instead of exit
            primaryStage.setOnCloseRequest(e -> {
                e.consume();
                minimizeToTray();
            });
            
            // Handle iconified (minimize) event - also minimize to tray
            primaryStage.iconifiedProperty().addListener((obs, wasIconified, isIconified) -> {
                if (isIconified && !isMinimizedToTray) {
                    Platform.runLater(this::minimizeToTray);
                }
            });
            
            primaryStage.show();
            
            // Start automatic discovery
            startBackgroundDiscovery();
            
            log.info("GREE HVAC Controller application started successfully");
            
        } catch (Exception e) {
            log.error("Failed to start application", e);
            showInlineStatus("Error: " + e.getMessage(), true);
        }
    }
    
    private VBox createEnhancedUI() {
        VBox root = new VBox(10);
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header with device control and status
        javafx.scene.layout.HBox header = createHeaderWithControls();
        
        // Main content area
        VBox mainContent = new VBox(15);
        mainContent.setStyle("-fx-padding: 20;");
        
        // Device selection and connection (merged)
        VBox deviceSection = createMergedDeviceSection();
        
        // Status messages area (replaces modal dialogs)
        statusMessageArea = createStatusMessageArea();
        
        mainContent.getChildren().addAll(deviceSection, statusMessageArea);
        
        root.getChildren().addAll(header, mainContent);
        return root;
    }
    
    private java.util.List<com.gree.hvac.dto.DeviceInfo> discoveredDevices = new java.util.ArrayList<>();
    private com.gree.hvac.dto.DeviceInfo selectedDevice = null;
    private com.gree.hvac.client.HvacClient currentClient = null;
    private javafx.scene.control.ComboBox<String> deviceComboBox;
    private javafx.scene.control.Button connectButton;
    private javafx.scene.control.Label connectionStatusLabel;
    private VBox statusMessageArea;
    
    // Control UI elements in header
    private javafx.scene.control.ToggleButton powerToggle;
    private javafx.scene.control.Button tempMinusButton;
    private javafx.scene.control.Button tempPlusButton;
    private javafx.scene.control.Label temperatureLabel;
    private javafx.scene.control.ComboBox<String> modeComboBox;
    private javafx.scene.control.ComboBox<String> fanSpeedComboBox;
    private javafx.scene.control.Label currentTempLabel;
    private int currentTemperature = 24; // Track current temperature setting
    
    // Background discovery
    private java.util.concurrent.ScheduledExecutorService discoveryExecutor;
    private java.util.concurrent.ScheduledFuture<?> discoveryTask;
    
    // Status polling
    private java.util.concurrent.ScheduledExecutorService statusPollingExecutor;
    private java.util.concurrent.ScheduledFuture<?> statusPollingTask;
    
    // New enhanced UI methods
    private javafx.scene.layout.HBox createHeaderWithControls() {
        javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(20);
        header.setStyle("-fx-background-color: linear-gradient(to right, #2196F3, #1976D2); -fx-padding: 15; -fx-alignment: center-left;");
        
        // App title
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("GREE HVAC Controller");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Control panel in header
        javafx.scene.layout.HBox controlsBox = createHeaderControls();
        controlsBox.setStyle("-fx-alignment: center;");
        
        // Current temperature in top right
        VBox tempStatusBox = new VBox(2);
        tempStatusBox.setStyle("-fx-alignment: center-right;");
        javafx.scene.control.Label tempTitle = new javafx.scene.control.Label("Current Temperature");
        tempTitle.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        currentTempLabel = new javafx.scene.control.Label("--°C");
        currentTempLabel.setStyle("-fx-text-fill: #FFE082; -fx-font-size: 18px; -fx-font-weight: bold;");
        tempStatusBox.getChildren().addAll(tempTitle, currentTempLabel);
        
        // Spacer to push temperature to right
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(titleLabel, controlsBox, spacer, tempStatusBox);
        return header;
    }
    
    private javafx.scene.layout.HBox createHeaderControls() {
        javafx.scene.layout.HBox controlsBox = new javafx.scene.layout.HBox(15);
        controlsBox.setStyle("-fx-alignment: center;");
        
        // Power control
        powerToggle = new javafx.scene.control.ToggleButton("OFF");
        powerToggle.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6px 12px; -fx-min-width: 60px;");
        powerToggle.setOnAction(e -> updateDevicePower());
        powerToggle.setDisable(true);
        
        // Temperature control with + and - buttons
        VBox tempBox = new VBox(2);
        tempBox.setStyle("-fx-alignment: center;");
        temperatureLabel = new javafx.scene.control.Label("24°C");
        temperatureLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
        
        javafx.scene.layout.HBox tempButtonBox = new javafx.scene.layout.HBox(5);
        tempButtonBox.setStyle("-fx-alignment: center;");
        
        javafx.scene.control.Button tempMinusButton = new javafx.scene.control.Button("−");
        tempMinusButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4px 8px; -fx-font-size: 14px; -fx-min-width: 30px;");
        tempMinusButton.setOnAction(e -> decreaseTemperature());
        tempMinusButton.setDisable(true);
        
        javafx.scene.control.Button tempPlusButton = new javafx.scene.control.Button("+");
        tempPlusButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4px 8px; -fx-font-size: 14px; -fx-min-width: 30px;");
        tempPlusButton.setOnAction(e -> increaseTemperature());
        tempPlusButton.setDisable(true);
        
        tempButtonBox.getChildren().addAll(tempMinusButton, tempPlusButton);
        tempBox.getChildren().addAll(temperatureLabel, tempButtonBox);
        
        // Store references for enabling/disabling
        this.tempMinusButton = tempMinusButton;
        this.tempPlusButton = tempPlusButton;
        
        // Mode control
        modeComboBox = new javafx.scene.control.ComboBox<>();
        modeComboBox.getItems().addAll("AUTO", "COOL", "HEAT", "DRY", "FAN_ONLY");
        modeComboBox.setValue("AUTO");
        modeComboBox.setPrefWidth(80);
        modeComboBox.setOnAction(e -> updateDeviceMode());
        modeComboBox.setDisable(true);
        
        // Fan speed control
        fanSpeedComboBox = new javafx.scene.control.ComboBox<>();
        fanSpeedComboBox.getItems().addAll("AUTO", "LOW", "MED", "HIGH");
        fanSpeedComboBox.setValue("AUTO");
        fanSpeedComboBox.setPrefWidth(70);
        fanSpeedComboBox.setOnAction(e -> updateDeviceFanSpeed());
        fanSpeedComboBox.setDisable(true);
        
        controlsBox.getChildren().addAll(powerToggle, tempBox, modeComboBox, fanSpeedComboBox);
        return controlsBox;
    }
    
    private VBox createMergedDeviceSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 8;");
        
        javafx.scene.control.Label sectionTitle = new javafx.scene.control.Label("Device Selection & Connection");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        
        // Device selection row
        javafx.scene.layout.HBox deviceRow = new javafx.scene.layout.HBox(15);
        deviceRow.setStyle("-fx-alignment: center-left;");
        
        javafx.scene.control.Label deviceLabel = new javafx.scene.control.Label("Device:");
        deviceLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 60px;");
        
        deviceComboBox = new javafx.scene.control.ComboBox<>();
        deviceComboBox.setPromptText("Select a discovered device...");
        deviceComboBox.setPrefWidth(300);
        deviceComboBox.setOnAction(e -> {
            String selected = deviceComboBox.getValue();
            if (selected != null && !selected.isEmpty()) {
                com.gree.hvac.dto.DeviceInfo newSelectedDevice = discoveredDevices.stream()
                    .filter(d -> (d.getName() + " (" + d.getIpAddress() + ")").equals(selected))
                    .findFirst().orElse(null);
                
                if (newSelectedDevice != null) {
                    // Check if we're switching to a different device while connected
                    if (currentClient != null && currentClient.isConnected() && 
                        selectedDevice != null && !newSelectedDevice.equals(selectedDevice)) {
                        
                        // Show switching status
                        connectionStatusLabel.setText("Switching to " + newSelectedDevice.getName() + "...");
                        connectionStatusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-style: italic;");
                        
                        // Disconnect from current device first (silently for switching)
                        disconnectFromDevice(true);
                        
                        // Wait a moment, then connect to new device
                        selectedDevice = newSelectedDevice;
                        java.util.concurrent.CompletableFuture.delayedExecutor(1, java.util.concurrent.TimeUnit.SECONDS)
                            .execute(() -> Platform.runLater(this::connectToDevice));
                    } else {
                        // Normal selection (not switching)
                        selectedDevice = newSelectedDevice;
                        connectButton.setDisable(false);
                    }
                }
            }
        });
        
        connectButton = new javafx.scene.control.Button("Connect");
        connectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px;");
        connectButton.setDisable(true);
        connectButton.setOnAction(e -> connectToDevice());
        
        deviceRow.getChildren().addAll(deviceLabel, deviceComboBox, connectButton);
        
        // Status row
        connectionStatusLabel = new javafx.scene.control.Label("Searching for devices...");
        connectionStatusLabel.setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");
        
        section.getChildren().addAll(sectionTitle, deviceRow, connectionStatusLabel);
        return section;
    }
    
    private VBox createStatusMessageArea() {
        VBox messageArea = new VBox(5);
        messageArea.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");
        messageArea.setVisible(false);
        return messageArea;
    }
    
    // Status message methods (replace modal dialogs)
    private void showInlineStatus(String message, boolean isError) {
        Platform.runLater(() -> {
            statusMessageArea.getChildren().clear();
            javafx.scene.control.Label messageLabel = new javafx.scene.control.Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setStyle(isError ? 
                "-fx-text-fill: #D32F2F; -fx-background-color: #FFEBEE; -fx-padding: 10; -fx-background-radius: 4;" :
                "-fx-text-fill: #388E3C; -fx-background-color: #E8F5E8; -fx-padding: 10; -fx-background-radius: 4;");
            statusMessageArea.getChildren().add(messageLabel);
            statusMessageArea.setVisible(true);
            
            // Auto-hide after 5 seconds
            java.util.concurrent.CompletableFuture.delayedExecutor(5, java.util.concurrent.TimeUnit.SECONDS)
                .execute(() -> Platform.runLater(() -> statusMessageArea.setVisible(false)));
        });
    }
    
    // System tray methods
    private void initializeSystemTray() {
        if (!SystemTray.isSupported()) {
            log.warn("System tray is not supported");
            return;
        }
        
        systemTray = SystemTray.getSystemTray();
        
        // Create a simple 16x16 icon for Windows 11 compatibility
        Image image = createTrayIcon();
        trayIcon = new TrayIcon(image, "GREE HVAC Controller");
        trayIcon.setImageAutoSize(true);
        
        // Create popup menu
        PopupMenu popup = new PopupMenu();
        
        // Power control in tray
        MenuItem powerItem = new MenuItem("Toggle Power");
        powerItem.addActionListener(e -> Platform.runLater(() -> {
            if (currentClient != null && currentClient.isConnected()) {
                powerToggle.setSelected(!powerToggle.isSelected());
                updateDevicePower();
            }
        }));
        
        MenuItem showItem = new MenuItem("Show Window");
        showItem.addActionListener(e -> Platform.runLater(this::showFromTray));
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> Platform.runLater(() -> {
            stop();
            Platform.exit();
            System.exit(0);
        }));
        
        popup.add(powerItem);
        popup.addSeparator();
        popup.add(showItem);
        popup.add(exitItem);
        
        trayIcon.setPopupMenu(popup);
        trayIcon.addActionListener(e -> Platform.runLater(this::showFromTray));
        
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            log.error("Failed to add system tray icon", e);
        }
    }
    
    private Image createTrayIcon() {
        // Create a simple 16x16 icon with blue background and white "AC" text
        int size = 16;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable antialiasing for better text rendering
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Fill background with blue color
        g2d.setColor(new Color(33, 150, 243)); // Material Blue
        g2d.fillOval(0, 0, size, size);
        
        // Draw white border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new java.awt.BasicStroke(1.0f));
        g2d.drawOval(0, 0, size-1, size-1);
        
        // Draw "AC" text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 8));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "AC";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        int x = (size - textWidth) / 2;
        int y = (size - textHeight) / 2 + textHeight;
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return image;
    }
    
    private void minimizeToTray() {
        if (trayIcon != null) {
            primaryStage.hide();
            isMinimizedToTray = true;
            trayIcon.displayMessage("GREE HVAC Controller", "Application minimized to tray", TrayIcon.MessageType.INFO);
        }
    }
    
    private void showFromTray() {
        if (isMinimizedToTray) {
            primaryStage.show();
            primaryStage.setIconified(false);
            primaryStage.toFront();
            primaryStage.requestFocus();
            isMinimizedToTray = false;
        }
    }
    
    // Background discovery methods
    private void startBackgroundDiscovery() {
        if (discoveryExecutor == null) {
            discoveryExecutor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        }
        
        // Initial discovery
        performDiscovery();
        
        // Schedule periodic discovery every 30 seconds
        discoveryTask = discoveryExecutor.scheduleAtFixedRate(this::performDiscovery, 30, 30, java.util.concurrent.TimeUnit.SECONDS);
    }
    
    private void performDiscovery() {
        com.gree.hvac.GreeHvac.discoverDevices()
            .thenAccept(devices -> {
                Platform.runLater(() -> {
                    boolean devicesChanged = !devices.equals(discoveredDevices);
                    discoveredDevices.clear();
                    discoveredDevices.addAll(devices);
                    updateDeviceComboBox();
                    
                    if (devicesChanged) {
                        if (devices.isEmpty()) {
                            connectionStatusLabel.setText("No devices found on network");
                            connectionStatusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-style: italic;");
                        } else {
                            connectionStatusLabel.setText("Found " + devices.size() + " device(s). Select one to connect.");
                            connectionStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-style: italic;");
                        }
                    }
                });
            })
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    connectionStatusLabel.setText("Discovery error: " + throwable.getMessage());
                    connectionStatusLabel.setStyle("-fx-text-fill: #F44336; -fx-font-style: italic;");
                });
                return null;
            });
    }
    
    private void updateDeviceComboBox() {
        // Remember currently selected device
        String currentSelection = deviceComboBox.getValue();
        
        deviceComboBox.getItems().clear();
        for (com.gree.hvac.dto.DeviceInfo device : discoveredDevices) {
            String displayName = device.getName() + " (" + device.getIpAddress() + ")";
            deviceComboBox.getItems().add(displayName);
        }
        
        // Restore selection if the device is still available
        if (currentSelection != null && deviceComboBox.getItems().contains(currentSelection)) {
            deviceComboBox.setValue(currentSelection);
        } else if (selectedDevice != null) {
            // Try to find the selected device in the new list
            String selectedDisplayName = selectedDevice.getName() + " (" + selectedDevice.getIpAddress() + ")";
            if (deviceComboBox.getItems().contains(selectedDisplayName)) {
                deviceComboBox.setValue(selectedDisplayName);
            }
        }
    }
    
    private void connectToDevice() {
        if (selectedDevice == null) return;
        
        connectButton.setText("Connecting...");
        connectButton.setDisable(true);
        connectionStatusLabel.setText("Connecting to " + selectedDevice.getName() + "...");
        connectionStatusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-style: italic;");
        
        try {
            currentClient = com.gree.hvac.GreeHvac.createClient(selectedDevice);
            currentClient.connect()
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        connectButton.setText("Disconnect");
                        connectButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px;");
                        connectButton.setOnAction(e -> disconnectFromDevice());
                        connectButton.setDisable(false);
                        
                        connectionStatusLabel.setText("Connected to " + selectedDevice.getName());
                        connectionStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-style: italic;");
                        setControlPanelEnabled(true);
                        
                        // Start status polling
                        startStatusPolling();
                        updateDeviceStatus();
                        
                        // Check if this was a device switch
                        String statusMessage = connectionStatusLabel.getText().contains("Switching") ?
                            "Successfully switched to " + selectedDevice.getName() :
                            "Successfully connected to " + selectedDevice.getName();
                        showInlineStatus(statusMessage, false);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        connectButton.setText("Connect");
                        connectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px;");
                        connectButton.setOnAction(e -> connectToDevice());
                        connectButton.setDisable(false);
                        
                        connectionStatusLabel.setText("Connection failed: " + throwable.getMessage());
                        connectionStatusLabel.setStyle("-fx-text-fill: #F44336; -fx-font-style: italic;");
                        currentClient = null;
                        
                        showInlineStatus("Failed to connect: " + throwable.getMessage(), true);
                    });
                    return null;
                });
        } catch (Exception e) {
            connectButton.setText("Connect");
            connectButton.setDisable(false);
            connectionStatusLabel.setText("Error: " + e.getMessage());
            connectionStatusLabel.setStyle("-fx-text-fill: #F44336; -fx-font-style: italic;");
            showInlineStatus("Connection error: " + e.getMessage(), true);
        }
    }
    
    private void disconnectFromDevice() {
        disconnectFromDevice(false);
    }
    
    private void disconnectFromDevice(boolean isSwitching) {
        if (currentClient != null) {
            currentClient.disconnect();
            currentClient = null;
        }
        
        connectButton.setText("Connect");
        connectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px;");
        connectButton.setOnAction(e -> connectToDevice());
        connectButton.setDisable(selectedDevice == null);
        
        if (!isSwitching) {
            connectionStatusLabel.setText("Disconnected");
            connectionStatusLabel.setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");
            showInlineStatus("Disconnected from device", false);
        }
        
        setControlPanelEnabled(false);
        resetControlsToDefault();
        stopStatusPolling();
    }
    

    
    private void startStatusPolling() {
        if (statusPollingExecutor == null) {
            statusPollingExecutor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        }
        
        // Poll device status every 3 seconds
        statusPollingTask = statusPollingExecutor.scheduleAtFixedRate(this::updateDeviceStatus, 1, 3, java.util.concurrent.TimeUnit.SECONDS);
    }
    
    private void stopStatusPolling() {
        if (statusPollingTask != null) {
            statusPollingTask.cancel(true);
            statusPollingTask = null;
        }
    }
    
    private void updateDeviceStatus() {
        if (currentClient == null || !currentClient.isConnected()) return;
        
        try {
            com.gree.hvac.dto.DeviceStatus status = currentClient.getStatus();
            javafx.application.Platform.runLater(() -> {
                if (status != null) {
                    // Update power toggle
                    if (status.getPower() != null) {
                        powerToggle.setSelected(status.getPower());
                        powerToggle.setText(status.getPower() ? "ON" : "OFF");
                    }
                    
                    // Update temperature setting and label
                    if (status.getTemperature() != null) {
                        currentTemperature = status.getTemperature();
                        temperatureLabel.setText(currentTemperature + "°C");
                    }
                    
                    // Update current temperature
                    if (status.getCurrentTemperature() != null && currentTempLabel != null) {
                        currentTempLabel.setText("Current: " + status.getCurrentTemperature() + "°C");
                    }
                    
                    // Update mode
                    if (status.getMode() != null) {
                        modeComboBox.setValue(status.getMode().toUpperCase());
                    }
                    
                    // Update fan speed
                    if (status.getFanSpeed() != null) {
                        fanSpeedComboBox.setValue(status.getFanSpeed().toUpperCase());
                    }
                }
            });
        } catch (Exception e) {
            // Silently handle status update errors to avoid spamming the UI
        }
    }
    
    private void resetControlsToDefault() {
        powerToggle.setSelected(false);
        powerToggle.setText("OFF");
        currentTemperature = 24;
        temperatureLabel.setText("24°C");
        modeComboBox.setValue("AUTO");
        fanSpeedComboBox.setValue("AUTO");
        if (currentTempLabel != null) {
            currentTempLabel.setText("--°C");
        }
    }
    
    private void setControlPanelEnabled(boolean enabled) {
        powerToggle.setDisable(!enabled);
        tempMinusButton.setDisable(!enabled);
        tempPlusButton.setDisable(!enabled);
        modeComboBox.setDisable(!enabled);
        fanSpeedComboBox.setDisable(!enabled);
    }
    
    private void updateDevicePower() {
        if (currentClient == null || !currentClient.isConnected()) return;
        
        boolean power = powerToggle.isSelected();
        powerToggle.setText(power ? "ON" : "OFF");
        
        com.gree.hvac.dto.DeviceControl control = new com.gree.hvac.dto.DeviceControl();
        control.setPower(power);
        currentClient.control(control)
            .thenRun(() -> {
                // Wait a moment then update status
                java.util.concurrent.CompletableFuture.delayedExecutor(1, java.util.concurrent.TimeUnit.SECONDS)
                    .execute(this::updateDeviceStatus);
            });
    }
    
    private void increaseTemperature() {
        if (currentClient == null || !currentClient.isConnected()) return;
        if (currentTemperature < 30) {
            currentTemperature++;
            updateTemperatureDisplay();
            sendTemperatureCommand();
        }
    }
    
    private void decreaseTemperature() {
        if (currentClient == null || !currentClient.isConnected()) return;
        if (currentTemperature > 16) {
            currentTemperature--;
            updateTemperatureDisplay();
            sendTemperatureCommand();
        }
    }
    
    private void updateTemperatureDisplay() {
        Platform.runLater(() -> {
            temperatureLabel.setText(currentTemperature + "°C");
        });
    }
    
    private void sendTemperatureCommand() {
        com.gree.hvac.dto.DeviceControl control = new com.gree.hvac.dto.DeviceControl();
        control.setTemperature(currentTemperature);
        currentClient.control(control)
            .thenRun(() -> {
                // Wait a moment then update status
                java.util.concurrent.CompletableFuture.delayedExecutor(1, java.util.concurrent.TimeUnit.SECONDS)
                    .execute(this::updateDeviceStatus);
            });
    }
    
    private void updateDeviceMode() {
        if (currentClient == null || !currentClient.isConnected()) return;
        
        String mode = modeComboBox.getValue();
        com.gree.hvac.dto.DeviceControl control = new com.gree.hvac.dto.DeviceControl();
        control.setMode(mode);
        currentClient.control(control)
            .thenRun(() -> {
                // Wait a moment then update status
                java.util.concurrent.CompletableFuture.delayedExecutor(1, java.util.concurrent.TimeUnit.SECONDS)
                    .execute(this::updateDeviceStatus);
            });
    }
    
    private void updateDeviceFanSpeed() {
        if (currentClient == null || !currentClient.isConnected()) return;
        
        String fanSpeed = fanSpeedComboBox.getValue();
        com.gree.hvac.dto.DeviceControl control = new com.gree.hvac.dto.DeviceControl();
        control.setFanSpeed(fanSpeed);
        currentClient.control(control)
            .thenRun(() -> {
                // Wait a moment then update status
                java.util.concurrent.CompletableFuture.delayedExecutor(1, java.util.concurrent.TimeUnit.SECONDS)
                    .execute(this::updateDeviceStatus);
            });
    }
    
    @Override
    public void stop() {
        log.info("Stopping GREE HVAC Controller application");
        
        // Clean up resources
        if (currentClient != null) {
            currentClient.disconnect();
        }
        
        if (discoveryTask != null) {
            discoveryTask.cancel(true);
        }
        
        if (discoveryExecutor != null) {
            discoveryExecutor.shutdown();
        }
        
        if (statusPollingTask != null) {
            statusPollingTask.cancel(true);
        }
        
        if (statusPollingExecutor != null) {
            statusPollingExecutor.shutdown();
        }
        
        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
