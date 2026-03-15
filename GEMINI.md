# BoardCAD LE Project Overview

BoardCAD LE is a specialized CAD/CAM application for designing surfboards. It is a leaner, modernized fork of the original BoardCAD project, focusing on ease of use, maintainability, and modern Java standards.

## Project Structure

- `src/cadcore/`: Core geometry engine handling Bezier splines, knots, and mathematical utilities.
- `src/board/`: Data models for surfboard designs and I/O handlers for various file formats (notably `.brd`).
- `src/boardcam/`: CAM (Computer-Aided Manufacturing) logic, including toolpath generation for CNC machines and machine-specific writers.
- `src/boardcad/`: Main application logic, GUI components (Swing), and high-level CAD tools.
  - `src/boardcad/gui/jdk/`: Core Swing GUI implementation.
  - `src/boardcad/i18n/`: Internationalization resources.
  - `src/boardcad/settings/`: Application and machine settings management.

## Key Technologies

- **Java 25**: The project requires JDK 25.
- **Gradle**: Build and dependency management.
- **Java3D & JOGL**: Hardware-accelerated 3D rendering for board visualization.
- **FlatLaf, DarkLaf, Radiance**: Modern UI Look-and-Feel libraries.

## Building and Running

### Prerequisites
- JDK 25 installed and configured in your path.

### Key Commands

- **Run Application**:
  ```bash
  ./gradlew runBoardCAD
  ```
- **Build Project**:
  ```bash
  ./gradlew build
  ```
- **Create Native Installers**:
  Generates platform-specific installers (`.msi`, `.dmg`, or `.deb`) in the `./release` directory.
  ```bash
  ./gradlew release
  ```

### Docker Environment

A containerized development environment is available via Docker and Docker Compose. This environment handles all GUI and Java dependencies.

- **Build and Start**:
  ```bash
  docker-compose up --build
  ```
- **Shut Down**:
  ```bash
  docker-compose down
  ```

*Note: Linux users may need to run `xhost +local:docker` to allow the container to access the host's X11 display.*

## Development Conventions

### Coding Style
- Follow standard Java coding conventions.
- Use the singleton pattern for major application controllers (e.g., `BoardCAD.mInstance`).
- Swing is used for the GUI; ensure UI updates happen on the Event Dispatch Thread (EDT).

### Internationalization (i18n)
- All user-facing strings should be externalized to `src/boardcad/i18n/LanguageResource.properties`.
- Use `LanguageResource.getString("key")` to retrieve localized strings.

### File Formats
- The primary format is `.brd`, a parametric text-based (or encrypted) format.
- Technical specifications for the `.brd` format are documented in `brd_tech_spec.md`.

### Testing
- Standard Gradle test tasks are available. Ensure new geometry or CAM logic is validated.
