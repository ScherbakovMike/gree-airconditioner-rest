#!/bin/bash

# SonarQube Overview Fetcher Script
# This script fetches comprehensive project overview data from SonarCloud
# and saves it in a format suitable for analysis by Claude

set -e

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="$SCRIPT_DIR/sonar.json"
OUTPUT_FILE="$SCRIPT_DIR/sonar_overview_$(date +%Y%m%d_%H%M%S).json"

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed. Please install jq first."
    exit 1
fi

# Check if config file exists
if [[ ! -f "$CONFIG_FILE" ]]; then
    echo "Error: Configuration file not found: $CONFIG_FILE"
    exit 1
fi

# Load configuration
SONAR_HOST_URL=$(jq -r '.SONAR_HOST_URL' "$CONFIG_FILE")
SONAR_PROJECT_KEY=$(jq -r '.SONAR_PROJECT_KEY' "$CONFIG_FILE")
SONAR_TOKEN=$(jq -r '.SONAR_TOKEN' "$CONFIG_FILE")

# Validate configuration
if [[ "$SONAR_TOKEN" == "YOUR_SONAR_TOKEN_HERE" || "$SONAR_TOKEN" == "null" ]]; then
    echo "Error: Please set your SONAR_TOKEN in $CONFIG_FILE"
    exit 1
fi

if [[ "$SONAR_HOST_URL" == "null" || "$SONAR_PROJECT_KEY" == "null" ]]; then
    echo "Error: Invalid configuration in $CONFIG_FILE"
    exit 1
fi

echo "Fetching SonarQube overview for project: $SONAR_PROJECT_KEY"
echo "Host: $SONAR_HOST_URL"
echo "Output file: $OUTPUT_FILE"

# Create base API URL
API_BASE="$SONAR_HOST_URL/api"
AUTH_HEADER="Authorization: Bearer $SONAR_TOKEN"

# Function to make API calls
make_api_call() {
    local endpoint="$1"
    local url="$API_BASE/$endpoint"

    curl -s -H "$AUTH_HEADER" "$url" || {
        echo "Error: Failed to fetch data from $url" >&2
        return 1
    }
}

# Initialize output JSON
echo "{" > "$OUTPUT_FILE"
echo '  "metadata": {' >> "$OUTPUT_FILE"
echo "    \"project_key\": \"$SONAR_PROJECT_KEY\"," >> "$OUTPUT_FILE"
echo "    \"host_url\": \"$SONAR_HOST_URL\"," >> "$OUTPUT_FILE"
echo "    \"fetch_timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"" >> "$OUTPUT_FILE"
echo '  },' >> "$OUTPUT_FILE"

# Fetch project information
echo "Fetching project information..."
echo '  "project_info": ' >> "$OUTPUT_FILE"
make_api_call "projects/search?projects=$SONAR_PROJECT_KEY" | jq '.components[0] // {}' >> "$OUTPUT_FILE"
echo ',' >> "$OUTPUT_FILE"

# Fetch project measures (quality gate metrics)
echo "Fetching project measures..."
echo '  "measures": ' >> "$OUTPUT_FILE"
METRICS="alert_status,quality_gate_details,bugs,vulnerabilities,security_hotspots,code_smells,coverage,duplicated_lines_density,lines,ncloc,complexity,cognitive_complexity,security_rating,maintainability_rating,reliability_rating,sqale_rating,sqale_index,sqale_debt_ratio"
make_api_call "measures/component?component=$SONAR_PROJECT_KEY&metricKeys=$METRICS" >> "$OUTPUT_FILE"
echo ',' >> "$OUTPUT_FILE"

# Fetch quality gate status
echo "Fetching quality gate status..."
echo '  "quality_gate": ' >> "$OUTPUT_FILE"
make_api_call "qualitygates/project_status?projectKey=$SONAR_PROJECT_KEY" >> "$OUTPUT_FILE"
echo ',' >> "$OUTPUT_FILE"

# Fetch issues summary
echo "Fetching issues summary..."
echo '  "issues_summary": {' >> "$OUTPUT_FILE"

# Bugs
echo '    "bugs": ' >> "$OUTPUT_FILE"
make_api_call "issues/search?componentKeys=$SONAR_PROJECT_KEY&types=BUG&ps=1&facets=severities,types" | jq '.total' >> "$OUTPUT_FILE"
echo ',' >> "$OUTPUT_FILE"

# Vulnerabilities
echo '    "vulnerabilities": ' >> "$OUTPUT_FILE"
make_api_call "issues/search?componentKeys=$SONAR_PROJECT_KEY&types=VULNERABILITY&ps=1&facets=severities,types" | jq '.total' >> "$OUTPUT_FILE"
echo ',' >> "$OUTPUT_FILE"

# Code Smells
echo '    "code_smells": ' >> "$OUTPUT_FILE"
make_api_call "issues/search?componentKeys=$SONAR_PROJECT_KEY&types=CODE_SMELL&ps=1&facets=severities,types" | jq '.total' >> "$OUTPUT_FILE"
echo ',' >> "$OUTPUT_FILE"

# Security Hotspots
echo '    "security_hotspots": ' >> "$OUTPUT_FILE"
make_api_call "hotspots/search?projectKey=$SONAR_PROJECT_KEY&ps=1" | jq '.paging.total' >> "$OUTPUT_FILE"

echo '  },' >> "$OUTPUT_FILE"

# Fetch detailed issues by severity (top 20 for each type)
echo "Fetching detailed issues..."
echo '  "detailed_issues": {' >> "$OUTPUT_FILE"

echo '    "critical_bugs": ' >> "$OUTPUT_FILE"
make_api_call "issues/search?componentKeys=$SONAR_PROJECT_KEY&types=BUG&severities=CRITICAL&ps=20" >> "$OUTPUT_FILE"
echo ',' >> "$OUTPUT_FILE"

echo '    "major_bugs": ' >> "$OUTPUT_FILE"
make_api_call "issues/search?componentKeys=$SONAR_PROJECT_KEY&types=BUG&severities=MAJOR&ps=20" >> "$OUTPUT_FILE"
echo ',' >> "$OUTPUT_FILE"

echo '    "critical_vulnerabilities": ' >> "$OUTPUT_FILE"
make_api_call "issues/search?componentKeys=$SONAR_PROJECT_KEY&types=VULNERABILITY&severities=CRITICAL&ps=20" >> "$OUTPUT_FILE"
echo ',' >> "$OUTPUT_FILE"

echo '    "major_vulnerabilities": ' >> "$OUTPUT_FILE"
make_api_call "issues/search?componentKeys=$SONAR_PROJECT_KEY&types=VULNERABILITY&severities=MAJOR&ps=20" >> "$OUTPUT_FILE"
echo ',' >> "$OUTPUT_FILE"

echo '    "major_code_smells": ' >> "$OUTPUT_FILE"
make_api_call "issues/search?componentKeys=$SONAR_PROJECT_KEY&types=CODE_SMELL&severities=MAJOR&ps=20" >> "$OUTPUT_FILE"

echo '  },' >> "$OUTPUT_FILE"

# Fetch component tree (files and directories structure)
echo "Fetching component tree..."
echo '  "component_tree": ' >> "$OUTPUT_FILE"
make_api_call "components/tree?component=$SONAR_PROJECT_KEY&qualifiers=FIL&ps=500&metricKeys=ncloc,complexity,coverage,duplicated_lines_density" >> "$OUTPUT_FILE"
echo ',' >> "$OUTPUT_FILE"

# Fetch project analysis history (last 10 analyses)
echo "Fetching analysis history..."
echo '  "analysis_history": ' >> "$OUTPUT_FILE"
make_api_call "project_analyses/search?project=$SONAR_PROJECT_KEY&ps=10" >> "$OUTPUT_FILE"

# Close JSON
echo '}' >> "$OUTPUT_FILE"

echo ""
echo "✓ SonarQube overview data successfully fetched and saved to: $OUTPUT_FILE"
echo ""
echo "Summary of fetched data:"
echo "- Project information and metadata"
echo "- Quality metrics and measures"
echo "- Quality gate status"
echo "- Issues summary by type"
echo "- Detailed critical and major issues"
echo "- Component tree with metrics"
echo "- Analysis history"
echo ""
echo "You can now use this file for analysis with Claude."

# Make the output more readable by formatting it
if command -v jq &> /dev/null; then
    echo "Formatting JSON for better readability..."
    jq '.' "$OUTPUT_FILE" > "${OUTPUT_FILE}.formatted"
    mv "${OUTPUT_FILE}.formatted" "$OUTPUT_FILE"
    echo "✓ JSON formatted successfully"
fi