# Scripts Directory

This directory contains utility scripts for the GREE Air Conditioner project.

## SonarQube Overview Fetcher

### Files
- `sonar.json` - Configuration file containing SonarQube settings
- `fetch_sonar_overview.sh` - Script to fetch comprehensive SonarQube data

### Setup

1. **Configure your SonarQube token**:
   ```bash
   # Edit the sonar.json file and replace YOUR_SONAR_TOKEN_HERE with your actual token
   nano scripts/sonar.json
   ```

2. **Install required dependencies**:
   ```bash
   # On macOS
   brew install jq

   # On Ubuntu/Debian
   sudo apt-get install jq

   # On CentOS/RHEL
   sudo yum install jq
   ```

### Usage

```bash
# Run the script from the project root
./scripts/fetch_sonar_overview.sh
```

### What the script fetches

The script retrieves comprehensive SonarQube data including:

- **Project Information**: Basic project metadata and settings
- **Quality Metrics**: Coverage, complexity, lines of code, etc.
- **Quality Gate Status**: Current quality gate status and conditions
- **Issues Summary**: Count of bugs, vulnerabilities, code smells, and security hotspots
- **Detailed Issues**: Critical and major issues with locations and descriptions
- **Component Tree**: File-level metrics for all source files
- **Analysis History**: Recent analysis runs and their results

### Output

The script generates a timestamped JSON file: `sonar_overview_YYYYMMDD_HHMMSS.json`

This file is formatted for easy analysis by Claude and contains all the data needed to:
- Understand code quality status
- Identify areas for improvement
- Track quality trends over time
- Generate detailed reports

### Security Note

The `sonar.json` file contains your SonarQube token. Make sure to:
- Never commit this file with a real token to version control
- Keep your token secure and rotate it regularly
- Use environment variables in CI/CD pipelines instead of storing tokens in files