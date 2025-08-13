#!/bin/bash

echo "Starting GREE HVAC Controller..."
echo

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 21 or higher"
    exit 1
fi

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven 3.6 or higher"
    exit 1
fi

echo "Building and running the application..."
echo

# Build and run the application
mvn clean javafx:run

echo
echo "Application finished."


