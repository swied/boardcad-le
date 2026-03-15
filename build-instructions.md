# Building and Running BoardCAD LE on Ubuntu Linux

This guide provides instructions for building and running BoardCAD LE directly from the source code using Visual Studio (Code) on Ubuntu.

## Prerequisites

Before you begin, ensure you have the required Java Development Kit installed on your system:
* **Java JDK 25:** The project is configured to require Java 25 to compile and run. You can install a compatible JDK via your package manager or download it from a distribution like Adoptium/Eclipse Temurin. 
  *(You can verify your active Java version by running `java -version` and `javac -version` in your terminal).*

## Running the Software (Development Mode)

The repository includes a Gradle Wrapper (`gradlew`), which means you do not need to install Gradle globally on your machine.

1. **Open the Integrated Terminal:** In Visual Studio, open the terminal by navigating to **Terminal > New Terminal** (or pressing `` Ctrl+` ``).
2. **Make the Wrapper Executable:** Depending on how you cloned the repository, the wrapper script might need execute permissions. Run this command to be safe:
   ```bash
   chmod +x gradlew
   ```
3. **Launch BoardCAD LE:** To compile the code and launch the application directly from the terminal, execute the predefined run task:
   ```bash
   ./gradlew runBoardCAD
   ```
   *Note: The first time you run this command, Gradle will automatically download all required dependencies (such as JOGL, Java3D, and the FlatLaf UI libraries) which may take a minute or two.*

## Building a Native Release Package (.deb)

If you want to compile the software into a standalone installer for your Ubuntu server or desktop:

1. **Run the Release Task:** In your terminal, execute the packaging task:
   ```bash
   ./gradlew release
   ```
2. **Locate the Installer:** Once the build process is complete, the `jpackage` utility will have generated a `.deb` package. You can find this inside the newly created `release` directory at the root of your workspace (`./release/`).
3. **Install the Package:** You can install this generated `.deb` file using the standard Debian package manager:
   ```bash
   sudo dpkg -i release/boardcad*.deb
   ```