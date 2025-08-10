package examples;

import com.gree.hvac.GreeHvac;
import com.gree.hvac.client.HvacClient;
import com.gree.hvac.dto.DeviceControl;
import com.gree.hvac.dto.DeviceInfo;
import com.gree.hvac.dto.DeviceStatus;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * Basic example showing device discovery and control
 */
public class BasicControl {
    
    public static void main(String[] args) {
        System.out.println("GREE HVAC Library - Basic Control Example");
        System.out.println("==========================================");
        
        // Discover devices
        System.out.println("Discovering GREE devices on network...");
        
        GreeHvac.discoverDevices()
            .thenCompose(BasicControl::selectAndControlDevice)
            .exceptionally(error -> {
                System.err.println("Error: " + error.getMessage());
                return null;
            })
            .join(); // Wait for completion
    }
    
    private static CompletableFuture<Void> selectAndControlDevice(List<DeviceInfo> devices) {
        if (devices.isEmpty()) {
            System.out.println("No GREE devices found on the network.");
            return CompletableFuture.completedFuture(null);
        }
        
        System.out.println("\nFound " + devices.size() + " device(s):");
        for (int i = 0; i < devices.size(); i++) {
            DeviceInfo device = devices.get(i);
            System.out.printf("%d. %s (%s) at %s\n", 
                i + 1, device.getName(), device.getBrand(), device.getIpAddress());
        }
        
        // For this example, we'll use the first device
        DeviceInfo selectedDevice = devices.get(0);
        System.out.println("\nConnecting to: " + selectedDevice.getName());
        
        // Create client
        HvacClient client = GreeHvac.createClient(selectedDevice);
        
        // Set up event listeners
        client.onConnect(() -> {
            System.out.println("✓ Connected to " + selectedDevice.getName());
        });
        
        client.onDisconnect(() -> {
            System.out.println("✗ Disconnected from " + selectedDevice.getName());
        });
        
        client.onError(error -> {
            System.err.println("Device error: " + error.getMessage());
        });
        
        client.onStatusUpdate(status -> {
            System.out.println("Status update - Temperature: " + 
                status.getCurrentTemperature() + "°C, Mode: " + status.getMode());
        });
        
        // Connect and control
        return client.connect()
            .thenCompose(v -> demonstrateControl(client))
            .whenComplete((result, error) -> {
                // Cleanup
                client.shutdown();
                if (error != null) {
                    System.err.println("Error during control: " + error.getMessage());
                }
            });
    }
    
    private static CompletableFuture<Void> demonstrateControl(HvacClient client) {
        System.out.println("\n--- Device Control Demonstration ---");
        
        return CompletableFuture.runAsync(() -> {
            try {
                // Get current status
                System.out.println("Current device status:");
                DeviceStatus status = client.getStatus();
                printStatus(status);
                
                // Wait a moment
                Thread.sleep(1000);
                
                // Turn on the device and set cooling mode
                System.out.println("\nTurning on device and setting to cooling mode...");
                DeviceControl control = new DeviceControl();
                control.setPower(true);
                control.setMode("COOL");
                control.setTemperature(22);
                control.setFanSpeed("AUTO");
                
                client.control(control).get();
                System.out.println("✓ Control commands sent");
                
                // Wait for status update
                Thread.sleep(2000);
                
                // Get updated status
                System.out.println("\nUpdated device status:");
                DeviceStatus newStatus = client.getStatus();
                printStatus(newStatus);
                
                // Demonstrate different control commands
                System.out.println("\nDemonstrating various controls:");
                
                // Set to heating mode
                System.out.println("Setting to heating mode (25°C)...");
                control = new DeviceControl();
                control.setMode("HEAT");
                control.setTemperature(25);
                client.control(control).get();
                Thread.sleep(1000);
                
                // Enable turbo mode
                System.out.println("Enabling turbo mode...");
                control = new DeviceControl();
                control.setTurbo(true);
                client.control(control).get();
                Thread.sleep(1000);
                
                // Set fan to high speed
                System.out.println("Setting fan speed to HIGH...");
                control = new DeviceControl();
                control.setFanSpeed("HIGH");
                client.control(control).get();
                Thread.sleep(1000);
                
                // Final status check
                System.out.println("\nFinal device status:");
                DeviceStatus finalStatus = client.getStatus();
                printStatus(finalStatus);
                
                System.out.println("\n--- Control demonstration completed ---");
                
            } catch (Exception e) {
                throw new RuntimeException("Control demonstration failed", e);
            }
        });
    }
    
    private static void printStatus(DeviceStatus status) {
        System.out.println("  Power: " + (status.getPower() ? "ON" : "OFF"));
        System.out.println("  Mode: " + status.getMode());
        System.out.println("  Target Temperature: " + status.getTemperature() + "°C");
        System.out.println("  Current Temperature: " + status.getCurrentTemperature() + "°C");
        System.out.println("  Fan Speed: " + status.getFanSpeed());
        System.out.println("  Turbo: " + (status.getTurbo() ? "ON" : "OFF"));
        System.out.println("  Quiet: " + (status.getQuiet() ? "ON" : "OFF"));
    }
}