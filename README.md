# Campora GUI

A campus grievance logging, automated triage, and verified resolution desktop application built with pure Java Swing and JDBC.

## Features

- **Student & Faculty Feed:** Anonymous or identified issue reporting, category filtering, and real-time community endorsements.
- **Urgency Scoring Engine:** Automated prioritization index based on report domain, safety hazards, and upvote counts.
- **Administrative Triage Console:** Passcode-protected management portal (`campora2026`) for ticket status tracking and resolution notes.
- **Resilient Storage Layer:** Thread-safe in-memory cache synchronized with MySQL via JDBC, backed by periodic snapshot persistence.

## Prerequisites

- Java Development Kit (JDK 17+)
- MySQL Database (Cloud or Local instance)
- MySQL Connector/J driver placed in `lib/`

## Build & Run

Open PowerShell in the project root:

```powershell
# 1. Compile source files
javac -cp "bin;lib/*" -d bin backend/exception/*.java backend/model/*.java backend/storage/*.java ui/theme/*.java ui/components/*.java ui/*.java App.java

# 2. Run application
java -cp "bin;lib/*" App
