package com.gree.hvac.controller;

import com.gree.hvac.GreeHvac;
import com.gree.hvac.client.HvacClient;
import com.gree.hvac.dto.DeviceControl;
import com.gree.hvac.dto.DeviceInfo;
import com.gree.hvac.dto.DeviceStatus;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/** Main controller for the GREE HVAC Controller application */
@Slf4j
public class MainController {

  @FXML private VBox mainContainer;

  @FXML private Button discoverButton;

  @FXML private Button connectButton;

  @FXML private Button disconnectButton;

  @FXML private TableView<DeviceInfo> deviceTable;

  @FXML private TableColumn<DeviceInfo, String> nameColumn;

  @FXML private TableColumn<DeviceInfo, String> ipColumn;

  @FXML private TableColumn<DeviceInfo, String> modelColumn;

  @FXML private TableColumn<DeviceInfo, String> statusColumn;

  @FXML private VBox controlPanel;

  @FXML private Label deviceInfoLabel;

  @FXML private ToggleButton powerToggle;

  @FXML private Slider temperatureSlider;

  @FXML private Label temperatureLabel;

  @FXML private ComboBox<String> modeComboBox;

  @FXML private ComboBox<String> fanSpeedComboBox;

  @FXML private ToggleButton turboToggle;

  @FXML private ToggleButton quietToggle;

  @FXML private Label currentTempLabel;

  @FXML private Label connectionStatusLabel;

  @FXML private ProgressIndicator discoveryProgress;

  @FXML private ProgressIndicator connectionProgress;

  private Stage primaryStage;
  private HvacClient currentClient;
  private DeviceInfo selectedDevice;
  private final ObservableList<DeviceInfo> discoveredDevices = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    log.info("Initializing MainController");

    // Initialize device table
    setupDeviceTable();

    // Initialize control panel
    setupControlPanel();

    // Initialize mode and fan speed options
    setupComboBoxes();

    // Set initial state
    setControlPanelEnabled(false);
    setConnectionStatus("Not connected");

    log.info("MainController initialized successfully");
  }

  private void setupDeviceTable() {
    nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
    ipColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIpAddress()));
    modelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getModel()));
    statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

    deviceTable.setItems(discoveredDevices);
    deviceTable
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> onDeviceSelectionChanged(newValue));
  }

  private void setupControlPanel() {
    // Temperature slider setup
    temperatureSlider.setMin(16);
    temperatureSlider.setMax(30);
    temperatureSlider.setValue(24);
    temperatureSlider
        .valueProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              temperatureLabel.setText(String.format("%.0f°C", newValue.doubleValue()));
            });
    temperatureLabel.setText("24°C");

    // Power toggle setup
    powerToggle
        .selectedProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (currentClient != null && currentClient.isConnected()) {
                updateDevicePower(newValue);
              }
            });
  }

  private void setupComboBoxes() {
    modeComboBox.getItems().addAll("AUTO", "COOL", "HEAT", "DRY", "FAN_ONLY");
    modeComboBox.setValue("AUTO");

    fanSpeedComboBox.getItems().addAll("AUTO", "LOW", "MEDIUM", "HIGH");
    fanSpeedComboBox.setValue("AUTO");

    modeComboBox.setOnAction(
        event -> {
          if (currentClient != null && currentClient.isConnected()) {
            updateDeviceMode(modeComboBox.getValue());
          }
        });

    fanSpeedComboBox.setOnAction(
        event -> {
          if (currentClient != null && currentClient.isConnected()) {
            updateDeviceFanSpeed(fanSpeedComboBox.getValue());
          }
        });
  }

  @FXML
  private void onDiscoverDevices() {
    log.info("Starting device discovery");
    setDiscoveryProgress(true);
    discoverButton.setDisable(true);

    CompletableFuture<List<DeviceInfo>> discoveryFuture = GreeHvac.discoverDevices();

    discoveryFuture
        .thenAccept(
            devices -> {
              Platform.runLater(
                  () -> {
                    discoveredDevices.clear();
                    discoveredDevices.addAll(devices);
                    setDiscoveryProgress(false);
                    discoverButton.setDisable(false);

                    if (devices.isEmpty()) {
                      showAlert(
                          "No devices found",
                          "No GREE HVAC devices were discovered on the network.");
                    } else {
                      log.info("Discovered {} devices", devices.size());
                    }
                  });
            })
        .exceptionally(
            throwable -> {
              Platform.runLater(
                  () -> {
                    setDiscoveryProgress(false);
                    discoverButton.setDisable(false);
                    showAlert(
                        "Discovery Error", "Failed to discover devices: " + throwable.getMessage());
                  });
              return null;
            });
  }

  @FXML
  private void onConnectToDevice() {
    if (selectedDevice == null) {
      showAlert("No Device Selected", "Please select a device from the table first.");
      return;
    }

    log.info("Connecting to device: {}", selectedDevice.getName());
    setConnectionProgress(true);
    connectButton.setDisable(true);

    try {
      currentClient = GreeHvac.createClient(selectedDevice);

      CompletableFuture<Void> connectFuture = currentClient.connect();
      connectFuture
          .thenRun(
              () -> {
                Platform.runLater(
                    () -> {
                      setConnectionProgress(false);
                      connectButton.setDisable(false);
                      setControlPanelEnabled(true);
                      setConnectionStatus("Connected to " + selectedDevice.getName());

                      // Start status polling
                      startStatusPolling();

                      log.info("Successfully connected to device: {}", selectedDevice.getName());
                    });
              })
          .exceptionally(
              throwable -> {
                Platform.runLater(
                    () -> {
                      setConnectionProgress(false);
                      connectButton.setDisable(false);
                      showAlert("Connection Error", "Failed to connect: " + throwable.getMessage());
                      setConnectionStatus("Connection failed");
                    });
                return null;
              });

    } catch (Exception e) {
      setConnectionProgress(false);
      connectButton.setDisable(false);
      showAlert("Connection Error", "Failed to create client: " + e.getMessage());
      setConnectionStatus("Connection failed");
    }
  }

  @FXML
  private void onDisconnect() {
    if (currentClient != null) {
      log.info("Disconnecting from device");
      currentClient.disconnect();
      currentClient = null;
      selectedDevice = null;

      setControlPanelEnabled(false);
      setConnectionStatus("Not connected");
      currentTempLabel.setText("--°C");

      log.info("Disconnected from device");
    }
  }

  private void onDeviceSelectionChanged(DeviceInfo device) {
    selectedDevice = device;
    if (device != null) {
      deviceInfoLabel.setText(
          String.format("Selected: %s (%s)", device.getName(), device.getIpAddress()));
      connectButton.setDisable(false);
    } else {
      deviceInfoLabel.setText("No device selected");
      connectButton.setDisable(true);
    }
  }

  private void updateDevicePower(boolean power) {
    if (currentClient == null || !currentClient.isConnected()) return;

    DeviceControl control = new DeviceControl();
    control.setPower(power);
    currentClient
        .control(control)
        .thenRun(() -> log.info("Updated device power: {}", power ? "ON" : "OFF"))
        .exceptionally(
            throwable -> {
              log.error("Failed to update device power", throwable);
              Platform.runLater(
                  () ->
                      showAlert(
                          "Control Error", "Failed to update power: " + throwable.getMessage()));
              return null;
            });
  }

  private void updateDeviceTemperature(double temperature) {
    if (currentClient == null || !currentClient.isConnected()) return;

    DeviceControl control = new DeviceControl();
    control.setTemperature((int) temperature);
    currentClient
        .control(control)
        .thenRun(() -> log.info("Updated device temperature: {}°C", temperature))
        .exceptionally(
            throwable -> {
              log.error("Failed to update device temperature", throwable);
              Platform.runLater(
                  () ->
                      showAlert(
                          "Control Error",
                          "Failed to update temperature: " + throwable.getMessage()));
              return null;
            });
  }

  private void updateDeviceMode(String mode) {
    if (currentClient == null || !currentClient.isConnected()) return;

    DeviceControl control = new DeviceControl();
    control.setMode(mode);
    currentClient
        .control(control)
        .thenRun(() -> log.info("Updated device mode: {}", mode))
        .exceptionally(
            throwable -> {
              log.error("Failed to update device mode", throwable);
              Platform.runLater(
                  () ->
                      showAlert(
                          "Control Error", "Failed to update mode: " + throwable.getMessage()));
              return null;
            });
  }

  private void updateDeviceFanSpeed(String fanSpeed) {
    if (currentClient == null || !currentClient.isConnected()) return;

    DeviceControl control = new DeviceControl();
    control.setFanSpeed(fanSpeed);
    currentClient
        .control(control)
        .thenRun(() -> log.info("Updated device fan speed: {}", fanSpeed))
        .exceptionally(
            throwable -> {
              log.error("Failed to update device fan speed", throwable);
              Platform.runLater(
                  () ->
                      showAlert(
                          "Control Error",
                          "Failed to update fan speed: " + throwable.getMessage()));
              return null;
            });
  }

  private void startStatusPolling() {
    if (currentClient == null) return;

    // Poll device status every 5 seconds
    CompletableFuture.runAsync(
        () -> {
          while (currentClient != null && currentClient.isConnected()) {
            try {
              DeviceStatus status = currentClient.getStatus();
              Platform.runLater(() -> updateStatusDisplay(status));
              Thread.sleep(5000);
            } catch (Exception e) {
              log.error("Error polling device status", e);
              break;
            }
          }
        });
  }

  private void updateStatusDisplay(DeviceStatus status) {
    if (status != null) {
      // Update power toggle
      if (status.getPower() != null) {
        powerToggle.setSelected(status.getPower());
      }

      // Update temperature display
      if (status.getCurrentTemperature() != null) {
        currentTempLabel.setText(String.format("%.1f°C", status.getCurrentTemperature()));
      }

      // Update temperature slider
      if (status.getTemperature() != null) {
        temperatureSlider.setValue(status.getTemperature());
      }

      // Update mode
      if (status.getMode() != null) {
        modeComboBox.setValue(status.getMode());
      }

      // Update fan speed
      if (status.getFanSpeed() != null) {
        fanSpeedComboBox.setValue(status.getFanSpeed());
      }
    }
  }

  private void setControlPanelEnabled(boolean enabled) {
    powerToggle.setDisable(!enabled);
    temperatureSlider.setDisable(!enabled);
    modeComboBox.setDisable(!enabled);
    fanSpeedComboBox.setDisable(!enabled);
    turboToggle.setDisable(!enabled);
    quietToggle.setDisable(!enabled);
    disconnectButton.setDisable(!enabled);
  }

  private void setDiscoveryProgress(boolean visible) {
    discoveryProgress.setVisible(visible);
  }

  private void setConnectionProgress(boolean visible) {
    connectionProgress.setVisible(visible);
  }

  private void setConnectionStatus(String status) {
    connectionStatusLabel.setText(status);
  }

  private void showAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }

  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }
}
