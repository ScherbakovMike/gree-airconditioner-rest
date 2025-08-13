# GREE HVAC Controller

A modern JavaFX application for controlling GREE air conditioning units using the `gree-hvac-lib` library.

## Features

- **Device Discovery**: Automatically discover GREE HVAC devices on your network
- **Device Connection**: Connect to discovered devices for control
- **Power Control**: Turn air conditioners on and off
- **Temperature Control**: Adjust temperature settings (16°C - 30°C)
- **Mode Selection**: Choose between AUTO, COOL, HEAT, DRY, and FAN_ONLY modes
- **Fan Speed Control**: Adjust fan speed (AUTO, LOW, MEDIUM, HIGH)
- **Additional Features**: Turbo mode, Quiet mode, and other advanced settings
- **Real-time Status**: Monitor current device status and temperature
- **Modern UI**: Clean, responsive interface with professional styling

## Requirements

- Java 21 or higher
- Maven 3.6 or higher
- Network access to GREE HVAC devices

## Building and Running

### Prerequisites

Make sure you have the following installed:
- Java 21 JDK
- Maven 3.6+

### Build the Application

```bash
# Navigate to the module directory
cd gree-hvac-controller

# Build the project
mvn clean package
```

### Run the Application

```bash
# Run using Maven
mvn javafx:run

# Or run the JAR file directly
java -jar target/gree-hvac-controller-1.0-SNAPSHOT.jar
```

## Usage

### 1. Device Discovery

1. Click the **"Discover Devices"** button to scan your network for GREE HVAC devices
2. Wait for the discovery process to complete
3. Discovered devices will appear in the table with their details

### 2. Device Connection

1. Select a device from the discovered devices table
2. Click the **"Connect"** button to establish a connection
3. Wait for the connection to be established
4. The control panel will become active once connected

### 3. Device Control

Once connected, you can control the device using:

- **Power Toggle**: Turn the device on/off
- **Temperature Slider**: Adjust the target temperature (16°C - 30°C)
- **Mode Selection**: Choose the operation mode
- **Fan Speed**: Adjust the fan speed setting
- **Turbo Mode**: Enable maximum cooling/heating
- **Quiet Mode**: Enable quiet operation

### 4. Monitoring

The application continuously monitors the device status:
- Current temperature is displayed in real-time
- Device status is updated every 5 seconds
- Connection status is clearly indicated

### 5. Disconnection

Click the **"Disconnect"** button to safely disconnect from the device.

## Architecture

The application follows the MVC (Model-View-Controller) pattern:

- **Model**: Uses the `gree-hvac-lib` for device communication
- **View**: JavaFX FXML-based user interface
- **Controller**: `MainController` class handling business logic

### Key Components

- `GreeHvacControllerApp`: Main application class
- `MainController`: Main controller handling UI interactions
- `MainController.fxml`: UI layout definition
- `styles.css`: Application styling

## Network Requirements

- The application must be run on the same network as your GREE HVAC devices
- Devices must support the GREE protocol
- Network discovery uses UDP broadcast packets

## Troubleshooting

### No Devices Found

- Ensure you're on the same network as your GREE devices
- Check if devices are powered on and connected to the network
- Verify network firewall settings allow UDP discovery

### Connection Failed

- Ensure the device is still available on the network
- Check if another application is already connected to the device
- Verify network connectivity to the device's IP address

### Control Commands Not Working

- Ensure the device is connected and responsive
- Check if the device supports the specific feature you're trying to control
- Verify the device is not in a locked or maintenance mode

## Development

### Project Structure

```
gree-hvac-controller/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/gree/hvac/controller/
│   │   │       ├── GreeHvacControllerApp.java
│   │   │       └── MainController.java
│   │   └── resources/
│   │       ├── fxml/
│   │       │   └── MainController.fxml
│   │       └── css/
│   │           └── styles.css
│   └── test/
│       └── java/
└── pom.xml
```

### Adding New Features

1. **New Control**: Add the control to `DeviceControl` DTO and update the controller
2. **New UI Element**: Add to FXML and bind to controller methods
3. **New Styling**: Add CSS classes for consistent appearance

### Testing

```bash
# Run unit tests
mvn test

# Run with test coverage
mvn jacoco:report
```

## Dependencies

- **gree-hvac-lib**: Core HVAC communication library
- **JavaFX**: UI framework
- **Lombok**: Reduces boilerplate code
- **SLF4J**: Logging framework

## License

This project is part of the GREE Air Conditioner project suite.

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review the `gree-hvac-lib` documentation
3. Check network connectivity and device compatibility


