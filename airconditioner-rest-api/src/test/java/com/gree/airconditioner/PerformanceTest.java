package com.gree.airconditioner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.gree.airconditioner.dto.api.ApiResponse;
import com.gree.airconditioner.dto.api.DeviceControlDto;
import com.gree.airconditioner.dto.api.DeviceInfoDto;
import com.gree.airconditioner.service.HvacDeviceService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PerformanceTest {

  @Mock private HvacDeviceService hvacDeviceService;

  private DeviceControlDto testControlDto;
  private List<DeviceInfoDto> testDevices;

  @BeforeEach
  void setUp() {
    testControlDto = new DeviceControlDto();
    testControlDto.setPower(true);
    testControlDto.setTemperature(22);
    testControlDto.setMode("COOL");
    testControlDto.setFanSpeed("AUTO");

    testDevices = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      DeviceInfoDto device = new DeviceInfoDto();
      device.setId("device-" + i);
      device.setName("Test AC " + i);
      device.setBrand("GREE");
      device.setModel("TEST-MODEL-" + i);
      device.setIpAddress("192.168.1." + (100 + i));
      testDevices.add(device);
    }
  }

  @Test
  void testResponseTimeMeasurement() {
    // Test basic response time measurement
    Instant start = Instant.now();

    // Simulate some work
    try {
      Thread.sleep(10); // 10ms delay
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    Instant end = Instant.now();
    Duration duration = Duration.between(start, end);

    // Verify that duration measurement works
    assertTrue(duration.toMillis() >= 10);
    assertTrue(duration.toMillis() < 100); // Should be much less than 100ms
  }

  @Test
  void testConcurrentDeviceDiscovery() throws InterruptedException {
    // Test concurrent device discovery operations
    int threadCount = 5;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    List<CompletableFuture<Long>> futures = new ArrayList<>();

    // Mock the service to return test devices
    when(hvacDeviceService.discoverDevices())
        .thenReturn(CompletableFuture.completedFuture(testDevices));

    for (int i = 0; i < threadCount; i++) {
      CompletableFuture<Long> future =
          CompletableFuture.supplyAsync(
              () -> {
                Instant start = Instant.now();

                try {
                  // Simulate device discovery
                  List<DeviceInfoDto> devices = hvacDeviceService.discoverDevices().get();
                  assertNotNull(devices);
                  assertEquals(10, devices.size());

                  Instant end = Instant.now();
                  return Duration.between(start, end).toMillis();
                } catch (Exception e) {
                  return -1L; // Error indicator
                }
              },
              executor);

      futures.add(future);
    }

    // Wait for all operations to complete
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    // Verify all operations completed successfully
    for (CompletableFuture<Long> future : futures) {
      try {
        Long duration = future.get();
        assertNotNull(duration);
        assertTrue(duration >= 0);
      } catch (Exception e) {
        fail("Future execution failed: " + e.getMessage());
      }
    }

    executor.shutdown();
    assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
  }

  @Test
  void testBulkDeviceControl() throws InterruptedException {
    // Test bulk device control operations
    int deviceCount = 5;
    int operationsPerDevice = 3;
    ExecutorService executor = Executors.newFixedThreadPool(deviceCount);
    List<CompletableFuture<Long>> futures = new ArrayList<>();

    // Mock the service to return success
    when(hvacDeviceService.controlDevice(anyString(), any(DeviceControlDto.class)))
        .thenReturn(CompletableFuture.completedFuture(true));

    for (int deviceIndex = 0; deviceIndex < deviceCount; deviceIndex++) {
      final String deviceId = "device-" + deviceIndex;

      for (int opIndex = 0; opIndex < operationsPerDevice; opIndex++) {
        CompletableFuture<Long> future =
            CompletableFuture.supplyAsync(
                () -> {
                  Instant start = Instant.now();

                  try {
                    // Simulate device control
                    Boolean result =
                        hvacDeviceService.controlDevice(deviceId, testControlDto).get();
                    assertTrue(result);

                    Instant end = Instant.now();
                    return Duration.between(start, end).toMillis();
                  } catch (Exception e) {
                    return -1L; // Error indicator
                  }
                },
                executor);

        futures.add(future);
      }
    }

    // Wait for all operations to complete
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    // Verify all operations completed successfully
    for (CompletableFuture<Long> future : futures) {
      try {
        Long duration = future.get();
        assertNotNull(duration);
        assertTrue(duration >= 0);
      } catch (Exception e) {
        fail("Future execution failed: " + e.getMessage());
      }
    }

    executor.shutdown();
    assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
  }

  @Test
  void testMemoryUsage() {
    // Test basic memory usage patterns
    Runtime runtime = Runtime.getRuntime();

    // Get initial memory state
    long initialMemory = runtime.totalMemory() - runtime.freeMemory();

    // Create some objects to consume memory
    List<String> testData = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      testData.add("Test data string " + i + " with some additional content to consume memory");
    }

    // Get memory after object creation
    long afterCreationMemory = runtime.totalMemory() - runtime.freeMemory();

    // Verify memory usage increased
    assertTrue(afterCreationMemory >= initialMemory);

    // Clear the data
    testData.clear();
    testData = null;

    // Force garbage collection (this is just for testing, not recommended in production)
    System.gc();

    // Get final memory state
    long finalMemory = runtime.totalMemory() - runtime.freeMemory();

    // Memory should be lower after cleanup
    assertTrue(finalMemory <= afterCreationMemory);
  }

  @Test
  void testApiResponseCreationPerformance() {
    // Test API response creation performance
    int iterations = 1000;
    Instant start = Instant.now();

    for (int i = 0; i < iterations; i++) {
      ApiResponse<String> response = ApiResponse.success("Test message " + i, "Data " + i);
      assertNotNull(response);
      assertTrue(response.isSuccess());
      assertEquals("Test message " + i, response.getMessage());
      assertEquals("Data " + i, response.getData());
    }

    Instant end = Instant.now();
    Duration duration = Duration.between(start, end);

    // Verify performance is reasonable (should be very fast for simple object creation)
    assertTrue(duration.toMillis() < 1000); // Should complete in less than 1 second
    assertTrue(duration.toNanos() >= 0); // Duration should be non-negative
  }

  @Test
  void testDtoCreationPerformance() {
    // Test DTO creation performance
    int iterations = 1000;
    Instant start = Instant.now();

    for (int i = 0; i < iterations; i++) {
      DeviceControlDto dto = new DeviceControlDto();
      dto.setPower(i % 2 == 0);
      dto.setTemperature(16 + (i % 15));
      dto.setMode("COOL");
      dto.setFanSpeed("AUTO");

      assertNotNull(dto);
      assertEquals(i % 2 == 0, dto.getPower());
      assertEquals(16 + (i % 15), dto.getTemperature());
    }

    Instant end = Instant.now();
    Duration duration = Duration.between(start, end);

    // Verify performance is reasonable
    assertTrue(duration.toMillis() < 1000);
    assertTrue(duration.toNanos() >= 0); // Duration should be non-negative
  }

  @Test
  void testResponseEntityCreationPerformance() {
    // Test ResponseEntity creation performance
    int iterations = 1000;
    Instant start = Instant.now();

    for (int i = 0; i < iterations; i++) {
      ResponseEntity<ApiResponse<String>> response =
          ResponseEntity.ok(ApiResponse.success("Message " + i, "Data " + i));

      assertNotNull(response);
      assertEquals(200, response.getStatusCodeValue());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().isSuccess());
    }

    Instant end = Instant.now();
    Duration duration = Duration.between(start, end);

    // Verify performance is reasonable
    assertTrue(duration.toMillis() < 1000);
    assertTrue(duration.toNanos() >= 0); // Duration should be non-negative
  }

  @Test
  void testConcurrentApiResponseCreation() throws InterruptedException {
    // Test concurrent API response creation
    int threadCount = 10;
    int responsesPerThread = 100;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    List<CompletableFuture<Integer>> futures = new ArrayList<>();

    for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
      final int threadId = threadIndex;

      CompletableFuture<Integer> future =
          CompletableFuture.supplyAsync(
              () -> {
                int successCount = 0;

                for (int i = 0; i < responsesPerThread; i++) {
                  try {
                    ApiResponse<String> response =
                        ApiResponse.success(
                            "Thread " + threadId + " Message " + i,
                            "Thread " + threadId + " Data " + i);

                    if (response.isSuccess()
                        && response.getMessage().contains("Thread " + threadId)) {
                      successCount++;
                    }
                  } catch (Exception e) {
                    // Count failures
                  }
                }

                return successCount;
              },
              executor);

      futures.add(future);
    }

    // Wait for all operations to complete
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    // Verify all threads completed successfully
    for (CompletableFuture<Integer> future : futures) {
      try {
        Integer successCount = future.get();
        assertNotNull(successCount);
        assertEquals(responsesPerThread, successCount);
      } catch (Exception e) {
        fail("Future execution failed: " + e.getMessage());
      }
    }

    executor.shutdown();
    assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
  }
}
