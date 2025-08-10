# GREE Air Conditioner REST API

A comprehensive REST API for controlling GREE air conditioning devices over a local network. This Spring Boot application provides a modern, well-documented interface for discovering, connecting to, and controlling GREE HVAC units.

## Features

- **Device Discovery**: Automatically scan and discover GREE devices on your network
- **Real-time Control**: Control multiple air conditioning properties simultaneously
- **Status Monitoring**: Get current device status and properties
- **Modern Architecture**: Built with Spring Boot 3.5.0 and Java 17
- **OpenAPI Documentation**: Interactive API documentation with Swagger UI
- **Comprehensive Logging**: Detailed logging for debugging and monitoring

## Requirements

- **Java 17** or higher
- **Maven 3.6+**
- **Network access** to GREE devices on the same local network

## Quick Start

### 1. Build the Application

```bash
mvn clean install
```

### 2. Run the Application

```bash
java -jar target/airconditioner-remote-1.0-SNAPSHOT.jar
```

### 3. Access the API Documentation

Open your browser and navigate to:
```
http://localhost:8081/swagger-ui/index.html
```

## API Endpoints

### Device Discovery
- `GET /api/devices/discover` - Scan network for GREE devices
- `GET /api/devices` - List all discovered devices

### Device Connection
- `POST /api/devices/{deviceId}/connect` - Connect to a specific device  
- `POST /api/devices/{deviceId}/disconnect` - Disconnect from a device

### Device Control
- `POST /api/devices/{deviceId}/control` - Control multiple device properties
- `GET /api/devices/{deviceId}/status` - Get current device status

### Convenience Endpoints
- `POST /api/devices/{deviceId}/power?on=true` - Toggle power
- `POST /api/devices/{deviceId}/temperature?temperature=22` - Set temperature
- `POST /api/devices/{deviceId}/mode?mode=COOL` - Set operation mode
- `POST /api/devices/{deviceId}/fanspeed?fanSpeed=AUTO` - Set fan speed

## Usage Examples

### 1. Discover Devices
```bash
curl -X GET "http://localhost:8081/api/devices/discover"
```

### 2. Connect to a Device
```bash
curl -X POST "http://localhost:8081/api/devices/192.168.1.100/connect"
```

### 3. Control Multiple Properties
```bash
curl -X POST "http://localhost:8081/api/devices/192.168.1.100/control" \
  -H "Content-Type: application/json" \
  -d '{
    "power": true,
    "temperature": 22,
    "mode": "COOL",
    "fanSpeed": "AUTO",
    "swingHorizontal": "DEFAULT",
    "swingVertical": "DEFAULT",
    "lights": true,
    "turbo": false,
    "quiet": false,
    "health": false,
    "powerSave": false,
    "sleep": false
  }'
```

### 4. Get Device Status
```bash
curl -X GET "http://localhost:8081/api/devices/192.168.1.100/status"
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

## Configuration

### Change Server Port

The default port is **8081**. You can change it by:

1. **Environment variable:**
   ```bash
   SERVER_PORT=8080 java -jar target/airconditioner-remote-1.0-SNAPSHOT.jar
   ```

2. **Command line parameter:**
   ```bash
   java -jar target/airconditioner-remote-1.0-SNAPSHOT.jar --server.port=8080
   ```

3. **Modify application.yml:**
   ```yaml
   server:
     port: 8080
   ```

## Development

### Code Quality

This project uses several tools to maintain code quality:

- **Spotless**: Code formatting with Google Java Format
- **SonarQube**: Static code analysis (configured in CI/CD)
- **JUnit 5**: Unit testing framework

### Build Commands

```bash
# Clean and build
mvn clean install

# Run tests
mvn test

# Check code formatting
mvn spotless:check

# Apply code formatting
mvn spotless:apply
```

## Architecture

The application follows a layered architecture:

- **Controller Layer**: REST endpoints (`GreeDeviceController`)
- **Service Layer**: Business logic (`GreeDeviceManager`)
- **Client Layer**: Device communication (`Client`, `PropertyTransformer`)
- **DTO Layer**: Data transfer objects for API responses

## Troubleshooting

### Common Issues

1. **Device not discovered**: Ensure your device and server are on the same network
2. **Connection failed**: Check if the device IP is correct and accessible
3. **Control not working**: Ensure the device is connected before sending control commands

### Logging

Enable debug logging by adding to your `application.yml`:
```yaml
logging:
  level:
    com.gree.airconditioner: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Ensure code quality with `mvn spotless:apply`
5. Run tests with `mvn test`
6. Submit a pull request

## Acknowledgments

This project is based on the excellent research and work from:
- [gree-remote](https://github.com/tomikaa87/gree-remote)
- [gree-hvac-client](https://www.npmjs.com/package/gree-hvac-client)
- [gree-airconditioner-rest](https://github.com/alexmuntean/gree-airconditioner-rest)

Special thanks to all contributors who made understanding the GREE protocol possible!

## License

This project is open source and available under the [MIT License](LICENSE).
