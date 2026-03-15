# BoardCAD LE

BoardCAD is an easy to use CAD/CAM-program that allows you to quickly design your own surfboards. BoardCAD LE is a fork of the original BoardCAD project where a few things have been removed to make it leaner and easier to maintain.

## Features

* **Surfboard Design:** Intuitive CAD/CAM tools to precisely model your custom surfboards.
* **Modern UI:** Features an updated and clean user interface powered by the FlatLaf, DarkLaf, and Radiance UI libraries.
* **Hardware Accelerated 3D:** Relies on JOGL and Java3D for responsive 3D rendering and visualization.

## Documentation

* **User Guide:** You can find the official BoardCAD LE documentation and user guide detailing keyboard shortcuts, editing curves, UI overviews, and usage instructions at [https://havardnj.github.io/boardcad-le/user_guide.html](https://havardnj.github.io/boardcad-le/user_guide.html).
* **Community & Support:** If you encounter bugs or want to request features, you can open an issue or start a discussion on the project's [GitHub Repository](https://github.com/HavardNJ/boardcad-le).

## Installation

### 1. Basic Install (Recommended)

You can download the latest pre-compiled releases for your operating system directly from the **Releases** page of this repository.

* **Windows:** Download the `.msi` installer.
* **macOS:** Download the `.dmg` disk image.
* **Linux:** Download the `.deb` package.

### 2. Building from Source (Local)

If you prefer to compile the project yourself, you will need **Java Development Kit (JDK) 25** installed on your system. 

**Running the App Locally:**
```bash
chmod +x gradlew
./gradlew runBoardCAD
```

### 3. Containerized Environment (Docker) - **New & Easy**

If you don't want to install Java 25 on your computer, or you want a "clean" environment that works exactly the same way every time, you can use **Docker**.

#### What is Docker?
Think of Docker as a way to package the entire application—including the specific version of Java and all the graphical libraries it needs—into a single "container." This container runs in isolation from the rest of your system, ensuring that it doesn't conflict with other software you have installed.

#### Prerequisites
You must have **Docker** and **Docker Compose** installed on your machine.
- [Download Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows/macOS)
- [Install Docker on Linux](https://docs.docker.com/engine/install/)

#### How to use it:

1.  **Prepare your Display (Linux Users Only):**
    Because BoardCAD LE is a graphical app, you need to give Docker permission to show windows on your screen. Open your terminal and run:
    ```bash
    xhost +local:docker
    ```

2.  **Build and Start:**
    In your terminal, navigate to the project folder and run:
    ```bash
    docker-compose up --build
    ```
    - `up`: This tells Docker to start the application.
    - `--build`: This tells Docker to "cook" a fresh version of the application using the source code on your machine. Use this whenever you've made changes to the code.

3.  **Shutting Down:**
    To stop the application and clean up the container, press `Ctrl+C` in your terminal or run:
    ```bash
    docker-compose down
    ```

4.  **Rebuilding:**
    If you change the source code and want to see the updates, simply run the `down` command followed by the `up --build` command again.

---

## Usage Examples

Once BoardCAD LE is installed and running, you can:
1. **Draft Geometry:** Use the 2D views to manipulate control points and edit your surfboard's Outline, Profile (Rocker), and Cross-Sections.
2. **3D Visualization:** Switch into the 3D rendering tab to get a realistic view of the board's foil, volume, and contours.
3. **CAM & Export:** Prepare your design for machining by generating G-code for CNC routers, or export the 3D model into standard formats like STL or DXF for further CAD integration.
