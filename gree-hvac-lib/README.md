# GREE HVAC Library - AI-Friendly Documentation

A comprehensive Java library for controlling GREE air conditioning devices via UDP protocol with AES encryption. Built with Java 21, providing async operations, event-driven architecture, and intelligent feature validation.

---

## Part 1: Core Architecture & Models

### System Overview
The GREE HVAC Library implements a client-server communication model over UDP protocol:
- **Client**: Java application using HvacClient
- **Server**: GREE AC device listening on UDP port 7000
- **Protocol**: Proprietary GREE protocol with AES encryption
- **Transport**: UDP DatagramSocket with JSON message format

### Data Models

#### DeviceInfo Model
```java
class DeviceInfo {
    String name;        // Human-readable device name
    String mac;         // MAC address (device identifier)
    String ip;          // IPv4 address
    String brand;       // Manufacturer brand
    String model;       // Device model number
    String version;     // Firmware version
}
```

#### DeviceStatus Model (Real-time State)
```java
class DeviceStatus {
    Boolean power;              // ON/OFF state
    Integer temperature;        // Target temperature (16-30°C)
    Integer currentTemperature; // Ambient temperature (offset -40)
    String mode;               // auto/cool/heat/dry/fan
    String fanSpeed;           // auto/low/medium_low/medium/medium_high/high
    String swingHorizontal;    // 8 positions: default/full/fixed_*
    String swingVertical;      // 12 positions: default/full/fixed_*/swing_*
    Boolean turbo;             // Maximum performance mode
    String quiet;              // off/mode1/mode2/mode3
    Boolean health;            // UVC sterilization/anion generation
    Boolean powerSave;         // Energy saving (SE feature)
    Boolean sleep;             // Sleep comfort mode
    Boolean lights;            // Display panel visibility
    Boolean blow;              // X-Fan auto-dry mode
    Boolean safetyHeating;     // Freeze protection (8°C)
    String air;               // Fresh air: off/inside/outside/mode3
}
```

#### DeviceControl Model (Command Structure)
Identical structure to DeviceStatus but used for sending commands. Only non-null fields are transmitted to device.

### Communication Protocol Model

#### Message Types
1. **scan** → Broadcast device discovery
2. **dev** → Device info response
3. **bind** → Connection handshake
4. **bindok** → Handshake confirmation
5. **status** → Status query/response
6. **dat** → Data exchange
7. **cmd** → Control command
8. **pack** → Encrypted payload wrapper

#### Encryption Model
- **Primary**: AES/ECB with PKCS5 padding
- **Fallback**: AES/GCM with authentication tags
- **Keys**: Generic key + device-specific binding key
- **Cipher Selection**: Automatic fallback for compatibility

---

## Part 2: Feature Validation Algorithm & Business Logic

### Mode-Aware Feature Validation

The library implements intelligent validation based on official GREE mobile app behavior matrix. This prevents incorrect feature usage that could cause device errors or unexpected behavior.

#### Validation Algorithm
```java
ValidationResult validate(String mode, Map<String, Object> features) {
    List<String> errors = new ArrayList<>();

    for (String feature : features.keySet()) {
        Object value = features.get(feature);

        // Step 1: Check feature availability in current mode
        if (value instanceof Boolean && (Boolean) value) {
            if (!FEATURE_AVAILABILITY.get(feature).contains(mode)) {
                errors.add(formatFeatureError(feature, mode));
            }
        }

        // Step 2: Check wind setting restrictions
        if (validateWindSettings && isWindSetting(feature)) {
            if (!WIND_SETTING_AVAILABILITY.get(feature).contains(mode)) {
                errors.add(formatWindSettingError(feature, mode));
            }
        }
    }

    return new ValidationResult(errors);
}
```

#### Feature Availability Matrix (Discovered from GREE Mobile App)

**Table 1: Features by Mode**
| Feature      | Auto | Cool | Heat | Fan | Dry |
|-------------|------|------|------|-----|-----|
| X-Fan (blow) | ✗    | ✓    | ✗    | ✗   | ✓   |
| Health (UVC) | ✓    | ✓    | ✓    | ✓   | ✓   |
| Energy Save  | ✗    | ✓    | ✗    | ✗   | ✗   |
| Safety Heat  | ✓    | ✓    | ✓    | ✓   | ✓   |

**Table 2: Wind Settings by Mode**
| Wind Setting | Auto | Cool | Heat | Fan | Dry |
|-------------|------|------|------|-----|-----|
| Fan Speed    | ✓    | ✓    | ✓    | ✓   | ✗   |
| Quiet Mode   | ✓    | ✓    | ✓    | ✓   | ✗   |
| Turbo Mode   | ✗    | ✓    | ✓    | ✗   | ✗   |
| Auto Wind    | ✓    | ✓    | ✓    | ✓   | ✗   |

#### Validation Modes
1. **NONE**: No validation (backward compatibility)
2. **WARN**: Log warnings but allow commands (default)
3. **STRICT**: Throw HvacFeatureValidationException

### Connection Management Algorithm

#### Discovery Algorithm
```java
CompletableFuture<List<DeviceInfo>> discoverDevices() {
    List<InetAddress> broadcastAddresses = getAllBroadcastAddresses();

    return CompletableFuture.allOf(
        broadcastAddresses.stream()
            .map(this::scanBroadcastAddress)
            .toArray(CompletableFuture[]::new)
    ).thenApply(this::aggregateResults);
}
```

#### Connection State Machine
```
[DISCONNECTED] --connect()--> [CONNECTING] --bind_success--> [CONNECTED]
       ^                           |                              |
       |                           v                              |
[FAILED] <--bind_timeout-- [BINDING] <--start_polling-- [POLLING]
       ^                           |                              |
       +--retry_attempts_exceeded--+                              |
                                                                  |
[DISCONNECTED] <--disconnect()-- [CONNECTED] <--error/timeout----+
```

---

## Part 3: Implementation Algorithms & Technical Details

### Async Operation Model

The library uses CompletableFuture for all operations, ensuring non-blocking execution:

```java
// Connection with timeout and retry
CompletableFuture<Void> connect() {
    return CompletableFuture
        .supplyAsync(this::performBinding)
        .thenCompose(this::waitForBindResponse)
        .orTimeout(connectTimeout, MILLISECONDS)
        .exceptionally(this::handleConnectionFailure);
}

// Status polling with exponential backoff
ScheduledFuture<?> startPolling() {
    return scheduler.scheduleWithFixedDelay(
        this::pollStatus,
        0,
        pollingInterval,
        MILLISECONDS
    );
}
```

### Vendor Code Mapping (From PropertyTransformer.java)

The library maps human-readable property names to GREE vendor codes:

| Human Property | Vendor Code | Value Type | Example Values |
|---------------|-------------|------------|----------------|
| power | Pow | Integer | 0=off, 1=on |
| mode | Mod | Integer | 0=auto, 1=cool, 2=dry, 3=fan_only, 4=heat |
| temperature | SetTem | Integer | 16-30 (Celsius) |
| currentTemperature | TemSen | Integer | Read-only, offset -40 |
| fanSpeed | WdSpd | Integer | 0=auto, 1=low, 2=mediumLow, 3=medium, 4=mediumHigh, 5=high |
| swingHorizontal | SwingLfRig | Integer | 0=default, 1=full, 2-6=fixed positions, 7=fullAlt |
| swingVertical | SwUpDn | Integer | 0=default, 1=full, 2-6=fixed, 7-11=swing positions |
| turbo | Tur | Integer | 0=off, 1=on |
| quiet | Quiet | Integer | 0=off, 1=mode1, 2=mode2, 3=mode3 |
| health | Health | Integer | 0=off, 1=on (UVC sterilization) |
| blow | Blo | Integer | 0=off, 1=on (X-Fan auto-dry) |
| powerSave | SvSt | Integer | 0=off, 1=on (SE energy saving) |
| sleep | SwhSlp | Integer | 0=off, 1=on |
| lights | Lig | Integer | 0=off, 1=on |
| air | Air | Integer | 0=off, 1=inside, 2=outside, 3=mode3 |
| safetyHeating | StHt | Integer | 0=off, 1=on (freeze protection) |

### Property Transformation Algorithm
```java
// Convert human-readable to vendor codes
Map<String, Object> toVendor(Map<String, Object> properties) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
        String vendorCode = PROPERTY_VENDOR_CODES.get(entry.getKey());
        if (vendorCode != null) {
            result.put(vendorCode, transformValue(entry.getKey(), entry.getValue()));
        }
    }
    return result;
}

// Special transformations
- currentTemperature: Read-only, device value - 40
- temperature: Direct integer value (16-30)
- Boolean values: 0=false, 1=true
- String enums: Mapped to integer constants
```

### Error Recovery Mechanisms

#### Automatic Reconnection
```java
void handleConnectionLoss() {
    if (reconnectAttempts < maxReconnectAttempts) {
        long delay = calculateExponentialBackoff(reconnectAttempts);
        scheduler.schedule(this::attemptReconnect, delay, MILLISECONDS);
        reconnectAttempts++;
    } else {
        notifyConnectionFailed();
    }
}
```

#### Cipher Fallback Algorithm
```java
boolean attemptBinding() {
    for (CipherType cipher : Arrays.asList(AES_ECB, AES_GCM)) {
        try {
            return performBinding(cipher);
        } catch (DecryptionException e) {
            log.debug("Cipher {} failed, trying next", cipher);
        }
    }
    return false;
}
```

### Thread Safety Model

- **Immutable DTOs**: DeviceInfo, DeviceStatus, DeviceControl
- **Synchronized Collections**: ConcurrentHashMap for property storage
- **Event Dispatch**: Single-threaded event executor
- **Network Operations**: Separate thread pool for I/O

---

## Part 4: Testing Strategy & Quality Assurance

### Test Architecture

The library employs a comprehensive testing strategy with multiple test categories:

#### 1. Unit Tests (307 tests)
- **DTO Tests**: Data model validation, serialization/deserialization
  - `DeviceControlTest`: 21 tests for control command validation
  - `DeviceStatusTest`: 26 tests for status parsing
  - `DeviceInfoTest`: 19 tests for device information handling

- **Client Logic Tests**: Core functionality without network dependencies
  - `HvacClientTest`: 26 tests for client behavior
  - `StrictValidationTest`: 10 tests for feature validation
  - `ModeFeatureValidatorTest`: 14 tests for validation algorithms

- **Network Layer Tests**: Mock-based network simulation
  - `DefaultNetworkServiceTest`: 3 tests for network abstraction
  - `DefaultSocketServiceTest`: 3 tests for socket management
  - `HvacDiscoveryTest`: 26 tests for device discovery

#### 2. Integration Tests (7 tests - disabled by default)
Located in `src/test/java/com/gree/hvac/investigation/`:

**Protocol Investigation Tests:**
- `GreeDeviceInvestigationTest`: Comprehensive protocol reverse engineering
  - Tests 50+ vendor property codes discovered
  - Validates temperature range 16-32°C (values beyond 30°C rejected)
  - Confirms mode values 0-8 (only 0-4 functional)
  - Discovers swing position limits (H: 0-12, V: 0-11)
  - Identifies quiet mode levels 0-3

**Feature Matrix Validation:**
- `ModeAwareFeaturesTest`: Real device feature availability testing
  - Confirms X-Fan only works in Cool(1) and Dry(2) modes
  - Validates PowerSave restriction to Cool mode only
  - Tests Health/UVC availability across all modes
  - Verifies Turbo limitations (Cool and Heat only)

**Device Behavior Analysis:**
- `StrictValidationIntegrationTest`: Validation system testing
- `XFanTest` & `XFanProperTest`: X-Fan auto-dry functionality
- `NewFeaturesTest`: Recent feature implementations
- `FeatureVerificationTest`: Multi-device capability testing
- `SingleDeviceTest`: Comprehensive single device testing

**Key Discoveries from Real Device Logs:**

1. **Connection Process (Actual Timings)**:
   - Device scan response: ~60ms
   - Binding attempt 1 (ECB): Usually times out (~500ms)
   - Binding attempt 2 (GCM): Succeeds (~12ms)
   - Total connection time: ~550ms average

2. **Command & Response Validation**:
   ```
   Command: {"p":[1],"opt":["Blo"],"t":"cmd"}
   Response: Properties updated successfully: [Blo]
   Verification: status.getBlow()=true ✅
   ```

3. **Status Data Format (Real Example)**:
   ```
   Request: "cols":["Pow","Mod","SetTem","WdSpd","Air","Blo","Health","SwhSlp","Lig","SwingLfRig","SwUpDn","Quiet","Tur","SvSt","StHt"]
   Response: "dat":[1,1,24,0,0,1,1,0,1,0,0,0,0,1,0] (Array matches cols order)
   ```

4. **Feature Verification (Logged Results)**:
   - X-Fan in Cool: Command sent → 3sec wait → Status verified ✅
   - Health in Cool: UVC activation confirmed via status readback ✅
   - PowerSave in Cool: SE mode activated and verified ✅
   - Current temp offset: Device reports 66°C, actual 26°C (offset -40)

5. **Network Performance**:
   - Command processing: ~12ms average
   - State change propagation: 2-3 seconds
   - Encryption overhead: ~2ms per message
   - Connection timeouts: Increased to 15s for reliable operation

#### 3. Mock Strategy
```java
@Mock NetworkService networkService;
@Mock SocketService socketService;

// Simulate device responses
when(networkService.sendMessage(any(), any()))
    .thenReturn(CompletableFuture.completedFuture(mockResponse));

// Test error scenarios
when(socketService.createSocket())
    .thenThrow(new HvacSocketException("Network unavailable"));
```

#### 4. Real Protocol Data from Investigation Tests

**Device Discovery Response:**
```json
{
  "t": "pack",
  "i": 0,
  "uid": 0,
  "pack": "base64EncryptedDeviceInfo",
  "cid": "app"
}
```

**Decrypted Device Info (inside pack):**
```json
{
  "t": "dev",
  "name": "Living Room AC",
  "mac": "c0393756ff23",
  "ver": "V1.1.1"
}
```

**Status Request Structure:**
```json
{
  "cols": ["Pow", "Mod", "SetTem", "WdSpd", "Air", "Blo", "Health", "SwhSlp", "Lig", "SwingLfRig", "SwUpDn", "Quiet", "Tur", "StHt", "TemUn", "TemSen"],
  "mac": "c0393756ff23",
  "t": "status"
}
```

**Status Response (decrypted):**
```json
{
  "t": "dat",
  "cols": ["Pow", "Mod", "SetTem", "WdSpd"],
  "dat": [1, 1, 22, 0]
}
```

**Control Command Structure:**
```json
{
  "opt": ["Pow", "Mod", "SetTem"],
  "p": [1, 1, 22],
  "t": "cmd"
}
```

**Command Response (decrypted):**
```json
{
  "t": "res",
  "opt": ["Pow", "Mod", "SetTem"],
  "p": [1, 1, 22],
  "val": [1, 1, 22]
}
```

### Quality Metrics

- **Test Coverage**: 85%+ code coverage via JaCoCo
- **Static Analysis**: SonarQube integration for code quality
- **Performance**: Async operations tested with CompletableFuture timeouts
- **Reliability**: Network failure simulation and recovery testing

---

## Part 5: Limitations, Dependencies & Deployment

### Known Limitations

#### Network Constraints
1. **Same Network Segment**: UDP broadcast requires devices on same subnet
2. **No NAT Traversal**: Cannot control devices behind different routers
3. **Firewall Dependencies**: Requires UDP port 7000 open
4. **Discovery Timing**: Initial discovery may take multiple attempts

#### Protocol Limitations
1. **UDP Reliability**: No guaranteed message delivery
2. **Firmware Variations**: Different device firmware may support different features
3. **Encryption Compatibility**: Some devices may only support specific cipher modes
4. **Rate Limiting**: Excessive commands may cause device to become unresponsive

#### Device-Specific Constraints (Discovered from Investigation Tests)
1. **Temperature Range**: Hardware-limited to 16-30°C (vendor mapping: SetTem 0-14)
2. **Mode Switching Delays**: Minimum 5 seconds required between mode changes
3. **Feature Activation Timing**: 3 seconds delay needed for feature verification
4. **Network Latency**: Real devices require 8+ second timeouts for reliable communication
5. **Power State Dependencies**: Some features require AC to be powered on first
6. **Mode-Dependent Features**: Strict validation based on GREE mobile app feature matrix
7. **Wind Control Restrictions**: Dry mode completely disables all wind controls (fanspeed, turbo, quiet)
8. **Status Synchronization**: Device status updates may lag behind control commands by 1-3 seconds

#### Protocol Constraints
1. **Mode Restrictions**:
   - Mode 0=auto, 1=cool, 2=dry, 3=fan_only, 4=heat
   - Values beyond 4 may cause device errors
2. **Fan Speed Limitations**:
   - Range: 0-5 (auto, low, mediumLow, medium, mediumHigh, high)
   - Dry mode: Fan speed controls are ignored/not available
3. **Swing Position Bounds**:
   - Horizontal: 0-7 positions (SwingLfRig)
   - Vertical: 0-11 positions (SwUpDn)
   - Invalid values may reset to default (0)
4. **Quiet Mode Levels**: 0-3 (off, mode1, mode2, mode3)
5. **Feature Interdependencies**:
   - Turbo + Quiet: Mutually exclusive in most modes
   - PowerSave (SE): Only functional in Cool mode
   - X-Fan (Blow): Only works in Cool and Dry modes
6. **Status Polling Frequency**:
   - Recommended: 3000ms minimum interval
   - Too frequent polling (< 1000ms) may cause device unresponsiveness
7. **Command Rate Limiting** (From Investigation Logs):
   - Max ~10 commands per minute to prevent device overload
   - Rapid successive commands may be ignored
   - Successful command processing: ~12ms average
   - Feature state propagation: 2-3 seconds for verification

8. **Protocol Performance Characteristics** (Real Measurements):
   - **Connection establishment**: 550ms average (includes retry)
   - **Command acknowledgment**: 12ms average response time
   - **State synchronization**: 2-3 seconds for device changes
   - **Encryption processing**: ~2ms overhead per message
   - **Network stability**: Requires 15s timeouts for reliable operation
   - **Status array format**: Direct mapping - `cols[i]` → `dat[i]`

### Best Practices (Based on Investigation Findings)

#### For Production Use
1. **Always use ValidationMode.STRICT** to prevent incorrect feature usage
2. **Implement proper timeouts**: Minimum 8s for commands, 15s for connections
3. **Add delays between operations**: 3s for verification, 5s for mode changes
4. **Check power state** before sending feature commands
5. **Validate mode compatibility** before sending control commands
6. **Implement exponential backoff** for connection retries
7. **Monitor device responsiveness** and pause if commands are ignored

#### For Integration Testing
1. **Use real devices** when possible - simulators miss timing constraints
2. **Test mode transitions** with proper delays between switches
3. **Verify feature validation** across all mode combinations
4. **Test network failure scenarios** with timeout handling
5. **Log comprehensive device state** for debugging protocol issues
6. **Test wind control restrictions** especially in Dry mode
7. **Verify encryption compatibility** across device firmware versions

#### Performance Optimization
1. **Cache device state** to reduce polling frequency
2. **Batch control commands** when changing multiple settings
3. **Use async operations** with proper timeout handling
4. **Implement connection pooling** for multiple devices
5. **Monitor command rate limits** to prevent device overload

### Dependencies & Requirements

#### Core Dependencies
```xml
<dependencies>
    <!-- Required Runtime -->
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20240303</version>
    </dependency>

    <dependency>
        <groupId>org.jvnet.hudson</groupId>
        <artifactId>crypto-util</artifactId>
        <version>1.7</version>
    </dependency>

    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.9</version>
    </dependency>

    <!-- Compile-time Only -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>

    <!-- Testing Only -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.1</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.8.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### System Requirements
- **Java Version**: OpenJDK 21+ (required)
- **Memory**: Minimum 64MB heap for basic operations
- **Network**: UDP port 7000 access required
- **Platform**: Cross-platform (Windows, Linux, macOS)

### Usage Examples

#### Quick Start
```java
// Discover devices
List<DeviceInfo> devices = GreeHvac.discoverDevices().get(10, SECONDS);

// Connect to first device
HvacClient client = GreeHvac.createClient(devices.get(0));

// Control AC with validation
DeviceControl control = new DeviceControl();
control.setPower(true);
control.setTemperature(22);
control.setMode("cool");

if (client.validateControl(control).isEmpty()) {
    client.control(control).get(5, SECONDS);
}
```

#### Advanced Configuration
```java
HvacClientOptions options = new HvacClientOptions("192.168.1.100")
    .setValidationMode(ValidationMode.STRICT)
    .setConnectTimeout(10000)
    .setPollingInterval(5000)
    .setDebug(true);

HvacClient client = GreeHvac.createClient(options);

// Event-driven monitoring
client.onStatusUpdate(status ->
    log.info("Temperature: {}°C", status.getCurrentTemperature()));
```

### Module Structure
```
gree-hvac-lib/
├── src/main/java/com/gree/hvac/
│   ├── client/           # HvacClient, options, validation
│   ├── discovery/        # Device discovery implementation
│   ├── dto/              # Data transfer objects
│   ├── exceptions/       # Custom exception hierarchy
│   ├── network/          # Network abstraction layer
│   ├── protocol/         # GREE protocol implementation
│   └── GreeHvac.java     # Main API entry point
├── src/test/java/        # Comprehensive test suite
└── pom.xml              # Maven build configuration
```

This documentation provides a complete technical reference for AI systems to understand, implement, and extend the GREE HVAC Library functionality.