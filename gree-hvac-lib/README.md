# GREE HVAC Library

A Java library for controlling GREE air conditioning devices over a local network. This library provides a simple, modern API for device discovery, connection, and control.

## Features

- **Device Discovery**: Automatically scan and discover GREE devices on your network
- **Async Communication**: Non-blocking operations using CompletableFuture
- **Event-Driven**: Listen to device status updates and connection events  
- **Type Safety**: Strongly typed DTOs for all operations
- **Modern Java**: Built with Java 21, uses modern patterns and practices
- **Logging**: Comprehensive logging with SLF4J
- **Protocol Handling**: Full implementation of GREE's UDP protocol with encryption

## Requirements

- **Java 21** or higher
- **Network access** to GREE devices on the same local network

## Installation

### Maven

```xml
<dependency>
    <groupId>com.gree</groupId>
    <artifactId>gree-hvac-lib</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### 1. Discover Devices

```java
import com.gree.hvac.GreeHvac;
import com.gree.hvac.dto.DeviceInfo;

// Discover all devices on the network
GreeHvac.discoverDevices()
    .thenAccept(devices -> {
        System.out.println("Found " + devices.size() + " devices:");
        devices.forEach(device -> 
            System.out.println("- " + device.getName() + " at " + device.getIpAddress())
        );
    });
```

### 2. Connect to a Device

```java
import com.gree.hvac.client.HvacClient;

// Create client and connect
HvacClient client = GreeHvac.createClient("192.168.1.100");

client.onConnect(() -> 
    System.out.println("Connected to device!")
);

client.onError(error -> 
    System.err.println("Error: " + error.getMessage())
);

client.connect().thenRun(() ->
    System.out.println("Connection established")
);
```

### 3. Control the Device

```java
import com.gree.hvac.dto.DeviceControl;

// Create control command
DeviceControl control = new DeviceControl();
control.setPower(true);
control.setTemperature(22);
control.setMode("COOL");
control.setFanSpeed("AUTO");

// Send to device
client.control(control).thenRun(() ->
    System.out.println("Device controlled successfully")
);
```

### 4. Monitor Device Status

```java
// Get current status
DeviceStatus status = client.getStatus();
System.out.println("Current temperature: " + status.getCurrentTemperature() + "°C");
System.out.println("Target temperature: " + status.getTemperature() + "°C");
System.out.println("Mode: " + status.getMode());

// Listen for status updates
client.onStatusUpdate(status -> {
    System.out.println("Status updated: " + status);
});
```

## Advanced Usage

### Custom Client Options

```java
import com.gree.hvac.client.HvacClientOptions;

HvacClientOptions options = new HvacClientOptions("192.168.1.100")
    .setPort(7000)
    .setConnectTimeout(5000)
    .setAutoConnect(false)
    .setPoll(true)
    .setPollingInterval(10000);

HvacClient client = GreeHvac.createClient(options);
```

### Using Raw Properties

```java
import java.util.HashMap;
import java.util.Map;

// Set properties directly
Map<String, Object> properties = new HashMap<>();
properties.put("power", "on");
properties.put("temperature", 24);
properties.put("mode", "cool");

client.setProperties(properties);
```

### Event Handling

```java
client.onConnect(() -> {
    System.out.println("Device connected");
});

client.onDisconnect(() -> {
    System.out.println("Device disconnected");
});

client.onError(error -> {
    System.err.println("Device error: " + error.getMessage());
});

client.onStatusUpdate(status -> {
    System.out.println("Temperature: " + status.getCurrentTemperature() + "°C");
});
```

## Supported Device Properties

| Property | Type | Values | Description |
|----------|------|--------|-------------|
| `power` | Boolean | `true`, `false` | Turn device on/off |
| `temperature` | Integer | `16-30` | Target temperature in Celsius |
| `mode` | String | `AUTO`, `COOL`, `HEAT`, `DRY`, `FAN_ONLY` | Operation mode |
| `fanSpeed` | String | `AUTO`, `LOW`, `MEDIUM`, `HIGH` | Fan speed setting |
| `swingHorizontal` | String | `DEFAULT`, `FULL`, `FIXED_LEFT`, `FIXED_RIGHT` | Horizontal swing |
| `swingVertical` | String | `DEFAULT`, `FULL`, `FIXED_TOP`, `FIXED_BOTTOM` | Vertical swing |
| `lights` | Boolean | `true`, `false` | Display panel lights |
| `turbo` | Boolean | `true`, `false` | Turbo mode |
| `quiet` | Boolean | `true`, `false` | Quiet operation |
| `health` | Boolean | `true`, `false` | Health mode |
| `powerSave` | Boolean | `true`, `false` | Power saving mode |
| `sleep` | Boolean | `true`, `false` | Sleep mode |

## Error Handling

The library uses CompletableFuture for async operations. Handle errors appropriately:

```java
client.connect()
    .thenRun(() -> System.out.println("Connected successfully"))
    .exceptionally(error -> {
        System.err.println("Connection failed: " + error.getMessage());
        return null;
    });
```

For synchronous error handling:

```java
try {
    client.connect().get(); // Blocks until completion
    System.out.println("Connected");
} catch (Exception e) {
    System.err.println("Connection failed: " + e.getMessage());
}
```

## Best Practices

### 1. Resource Cleanup
Always shutdown clients when done:

```java
// Shutdown single client
client.shutdown();

// Or use try-with-resources pattern
// (implement AutoCloseable in your wrapper)
```

### 2. Connection Management
- Check connection status before sending commands
- Handle connection events appropriately
- Implement retry logic for failed operations

```java
if (client.isConnected()) {
    client.control(command);
} else {
    client.connect().thenCompose(v -> client.control(command));
}
```

### 3. Error Handling
- Always handle CompletableFuture exceptions
- Log errors appropriately
- Implement fallback strategies

### 4. Network Discovery
- Discovery may take several seconds
- Not all devices may respond immediately
- Consider running discovery periodically

## Examples

See the [examples directory](examples/) for complete working examples:

- [Basic Device Control](examples/BasicControl.java)
- [Device Discovery](examples/DeviceDiscovery.java)
- [Status Monitoring](examples/StatusMonitoring.java)
- [Multi-Device Management](examples/MultiDevice.java)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## Troubleshooting

### Common Issues

1. **Device not discovered**: Ensure device and client are on same network
2. **Connection timeout**: Check firewall settings and device availability
3. **Control commands ignored**: Verify device is connected before sending commands

### Enable Debug Logging

```java
// Add to your logback.xml or logback-spring.xml
<logger name="com.gree.hvac" level="DEBUG"/>
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

This library is based on research and work from:
- [gree-remote](https://github.com/tomikaa87/gree-remote)
- [gree-hvac-client](https://www.npmjs.com/package/gree-hvac-client)

Special thanks to all contributors who made understanding the GREE protocol possible!