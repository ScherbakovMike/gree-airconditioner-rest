package com.gree.airconditioner;

import com.gree.airconditioner.gree.Client;
import com.gree.airconditioner.gree.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GREE HVAC Client Test Application - Java equivalent of test-gree-client.js
 */
public class GreeDemoClient {
    private static final Logger logger = LoggerFactory.getLogger(GreeDemoClient.class);

    // Configuration
    private static final String DEVICE_IP = "192.168.31.99"; // Replace with your device IP
    private static final int OPERATION_DELAY = 2000; // 2 seconds between operations

    private final Client client;

    public GreeDemoClient() {
        // Create client with debug enabled
        ClientOptions options = new ClientOptions(DEVICE_IP)
                .setPort(7000)
                .setConnectTimeout(5000)
                .setAutoConnect(false) // We'll connect manually
                .setPoll(false) // Disable automatic polling for cleaner logs
                .setDebug(true)
                .setLogLevel("debug");

        this.client = new Client(options);
        setupEventListeners();
    }

    public static void main(String[] args) {
        System.out.println("🌡️ GREE HVAC Client Test Application");
        System.out.println("====================================\n");

        // Check command line arguments
        if (args.length > 0 && "--show-constants".equals(args[0])) {
            showAvailableConstants();
            return;
        }

        GreeDemoClient app = new GreeDemoClient();
        
        try {
            app.runOperations().get();
        } catch (Exception e) {
            logger.error("💥 Unhandled error: ", e);
            System.exit(1);
        } finally {
            app.client.shutdown();
        }
    }

    /**
     * Helper function to wait
     */
    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper function to log current status
     */
    private void logCurrentStatus() {
        System.out.println("\n=== Current Device Status ===");
        Map<String, Object> props = client.getProperties();
        
        System.out.printf("Power: %s%n", 
                PropertyValue.Power.ON.equals(props.get("power")) ? "ON" : "OFF");
        System.out.printf("Temperature: %s°C%n", props.get("temperature"));
        System.out.printf("Target Temp: %s°C%n", props.get("temperature"));
        System.out.printf("Mode: %s%n", props.get("mode"));
        System.out.printf("Fan Speed: %s%n", props.get("fanSpeed"));
        System.out.printf("Swing Horizontal: %s%n", props.get("swingHor"));
        System.out.printf("Swing Vertical: %s%n", props.get("swingVert"));
        System.out.println("============================\n");
    }

    /**
     * Setup event listeners for detailed monitoring
     */
    private void setupEventListeners() {
        client.onConnect(() -> {
            System.out.println("🎉 Event: Connected to device");
        });

        client.onUpdate((updatedProperties) -> {
            System.out.println("📡 Event: Device properties updated");
            System.out.println("Updated: " + updatedProperties.keySet());
        });

        client.onSuccess((updatedProperties) -> {
            System.out.println("✅ Event: Command executed successfully");
            System.out.println("Changed properties: " + updatedProperties.keySet());
        });

        client.onError((error) -> {
            System.err.println("❌ Event: Error occurred - " + error.getMessage());
        });

        client.onDisconnect(() -> {
            System.out.println("🔌 Event: Disconnected from device");
        });

        client.onNoResponse(() -> {
            System.out.println("⚠️ Event: No response from device (timeout)");
        });
    }

    /**
     * Main operations workflow
     */
    public CompletableFuture<Void> runOperations() {
        return CompletableFuture.runAsync(() -> {
            try {
                System.out.println("🔌 Connecting to GREE device at " + DEVICE_IP);
                
                // Connect to the device
                client.connect().get();
                System.out.println("✅ Connected successfully!");
                System.out.println("📱 Device ID: " + client.getDeviceId());

                // Wait a bit for initial status
                sleep(1000);
                logCurrentStatus();

                // Operation 1: Turn ON
                System.out.println("🔛 Turning device ON...");
                client.setProperty(Property.POWER, PropertyValue.Power.ON).get();
                sleep(OPERATION_DELAY);
                logCurrentStatus();

                // Operation 2: Set temperature to 24°C
                System.out.println("🌡️ Setting temperature to 24°C...");
                client.setProperty(Property.TEMPERATURE, 24).get();
                sleep(OPERATION_DELAY);
                logCurrentStatus();

                // Operation 3: Set to cooling mode
                System.out.println("❄️ Setting to cooling mode...");
                client.setProperty(Property.MODE, PropertyValue.Mode.COOL).get();
                sleep(OPERATION_DELAY);
                logCurrentStatus();

                // Operation 4: Set fan speed to medium
                System.out.println("💨 Setting fan speed to medium...");
                client.setProperty(Property.FAN_SPEED, PropertyValue.FanSpeed.MEDIUM).get();
                sleep(OPERATION_DELAY);
                logCurrentStatus();

                // Operation 5: Enable horizontal swing
                System.out.println("↔️ Enabling horizontal swing...");
                client.setProperty(Property.SWING_HOR, PropertyValue.SwingHor.FULL).get();
                sleep(OPERATION_DELAY);
                logCurrentStatus();

                // Operation 6: Set multiple properties at once
                System.out.println("⚙️ Setting multiple properties at once...");
                Map<String, Object> multipleProps = new HashMap<>();
                multipleProps.put("temperature", 22);
                multipleProps.put("fanSpeed", PropertyValue.FanSpeed.HIGH);
                multipleProps.put("lights", PropertyValue.Lights.OFF);
                client.setProperties(multipleProps).get();
                sleep(OPERATION_DELAY);
                logCurrentStatus();

                // Operation 7: Turn OFF
                System.out.println("🔴 Turning device OFF...");
                client.setProperty(Property.POWER, PropertyValue.Power.OFF).get();
                sleep(OPERATION_DELAY);
                logCurrentStatus();

                System.out.println("✅ All operations completed successfully!");

            } catch (Exception e) {
                System.err.println("❌ Error occurred: " + e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.error("Stack trace:", e);
                }
            } finally {
                // Disconnect from the device
                try {
                    client.disconnect().get(5, TimeUnit.SECONDS);
                    System.out.println("🔌 Disconnected from device");
                } catch (Exception err) {
                    System.err.println("Error during disconnect: " + err.getMessage());
                }
            }
        });
    }

    /**
     * Display available constants for reference
     */
    private static void showAvailableConstants() {
        System.out.println("\n📋 Available Properties and Values:");
        
        System.out.println("\nProperties:");
        for (Property property : Property.values()) {
            System.out.print(property.getValue() + ", ");
        }
        System.out.println();
        
        System.out.println("\nPower Values: " + PropertyValue.Power.ON + ", " + PropertyValue.Power.OFF);
        System.out.println("Mode Values: " + PropertyValue.Mode.AUTO + ", " + PropertyValue.Mode.COOL + 
                ", " + PropertyValue.Mode.DRY + ", " + PropertyValue.Mode.FAN_ONLY + ", " + PropertyValue.Mode.HEAT);
        System.out.println("Fan Speed Values: " + PropertyValue.FanSpeed.AUTO + ", " + PropertyValue.FanSpeed.LOW + 
                ", " + PropertyValue.FanSpeed.MEDIUM + ", " + PropertyValue.FanSpeed.HIGH);
        System.out.println("Swing Horizontal Values: " + PropertyValue.SwingHor.DEFAULT + ", " + PropertyValue.SwingHor.FULL + 
                ", " + PropertyValue.SwingHor.FIXED_LEFT + ", " + PropertyValue.SwingHor.FIXED_RIGHT);
        System.out.println("Swing Vertical Values: " + PropertyValue.SwingVert.DEFAULT + ", " + PropertyValue.SwingVert.FULL + 
                ", " + PropertyValue.SwingVert.FIXED_TOP + ", " + PropertyValue.SwingVert.FIXED_BOTTOM);
        System.out.println("Light Values: " + PropertyValue.Lights.ON + ", " + PropertyValue.Lights.OFF);
        System.out.println();
    }
}