# Setup Instructions — MediTrack

## Prerequisites

| Tool | Minimum Version | Check Command |
|------|----------------|---------------|
| JDK | 21 | `java -version` |
| Maven | 3.8 | `mvn -version` |
| Git | 2.x | `git --version` |

### Installing JDK 21

**Windows (winget):**
```bash
winget install Microsoft.OpenJDK.21
```

**macOS (Homebrew):**
```bash
brew install openjdk@21
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

**Linux (apt):**
```bash
sudo apt update && sudo apt install openjdk-21-jdk
```

Verify: `java -version` should show `openjdk 21`.

### Installing Maven

**Windows (winget):**
```bash
winget install Apache.Maven
```

**macOS:**
```bash
brew install maven
```

**Linux:**
```bash
sudo apt install maven
```

Verify: `mvn -version` should show `Apache Maven 3.x`.

---

## Clone the Repository

```bash
git clone <repository-url>
cd MediTrack
```

---

## Build the Project

```bash
mvn clean compile
```

Maven downloads SLF4J and Logback dependencies automatically on the first build.

---

## Run the Application

### Standard mode (in-memory data only)

```bash
mvn exec:java -Dexec.mainClass="com.airtribe.meditrack.Main"
```

### Load persisted data on startup

If you have previously saved CSV/serialization data in the `data/` directory:

```bash
mvn exec:java -Dexec.mainClass="com.airtribe.meditrack.Main" -Dexec.args="--loadData"
```

The `--loadData` flag reads `data/doctors.csv`, `data/patients.csv`, and `data/appointments.ser` if they exist.

### Alternatively — build a fat JAR and run it

```bash
mvn package -DskipTests
java -jar target/meditrack-1.0-SNAPSHOT.jar
java -jar target/meditrack-1.0-SNAPSHOT.jar --loadData
```

> **Note:** The default `mvn package` does not produce an executable fat JAR without the `maven-shade-plugin`. Use `mvn exec:java` for the simplest path.

---

## Run the Test Suite

```bash
mvn exec:java -Dexec.mainClass="com.airtribe.meditrack.test.TestRunner"
```

Expected output ends with a summary line like:

```
[RESULT] Tests passed: 85 | Tests failed: 0
```

---

## Generate JavaDoc HTML

```bash
mvn javadoc:javadoc
```

Output is written to `target/site/apidocs/index.html`. Open it in any browser:

**Windows:**
```bash
start target/site/apidocs/index.html
```

**macOS / Linux:**
```bash
open target/site/apidocs/index.html
```

---

## Project Directory Structure

```
MediTrack/
├── data/                          # CSV and serialized data files (auto-created)
│   ├── doctors.csv
│   ├── patients.csv
│   └── appointments.ser
├── docs/                          # Documentation
│   ├── Design_Decisions.md
│   ├── JVM_Report.md
│   ├── Setup_Instructions.md
│   └── UML_ClassDiagram.md
├── logs/                          # Rolling log files (auto-created)
│   └── meditrack.log
├── src/
│   └── main/
│       ├── java/com/airtribe/meditrack/
│       │   ├── Main.java                    # Entry point + CLI menu
│       │   ├── concurrency/                 # ScheduledExecutorService reminders
│       │   ├── constants/                   # Application-wide constants
│       │   ├── entity/                      # Domain model (Patient, Doctor, Appointment, Bill)
│       │   │   └── enums/                   # AppointmentStatus, BloodGroup, Specialization, …
│       │   ├── exception/                   # Custom exceptions
│       │   ├── interfaces/                  # Service contracts + strategy interfaces
│       │   ├── observer/                    # Observer pattern implementations
│       │   ├── service/                     # Business logic implementations
│       │   ├── strategy/                    # Billing strategy implementations
│       │   ├── template/                    # Template Method pattern
│       │   ├── test/                        # Manual TestRunner
│       │   └── util/                        # Validators, DateUtil, IdGenerator, CSV/Serialization
│       └── resources/
│           └── logback.xml                  # SLF4J/Logback configuration
└── pom.xml
```

---

## Logging

Logs are written to two destinations:

| Destination | Location | Level | Format |
|-------------|----------|-------|--------|
| Console | stdout | INFO+ | `HH:mm:ss [LEVEL] logger - message` |
| File | `logs/meditrack.log` | INFO+ | Full timestamp + thread name |

The log file rotates daily and retains 7 days of history.

---

## Common Issues

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| `java: error: release version 21 not supported` | JDK < 21 active | Set `JAVA_HOME` to JDK 21 |
| `ClassNotFoundException` on run | Skipped `mvn compile` | Run `mvn clean compile` first |
| `data/` files not found with `--loadData` | Never saved data | Run option 7 (Save to CSV) in the menu first |
| Port/resource conflicts | None — app is fully local | N/A |
