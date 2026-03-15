# Use an official JDK 25 base image (Eclipse Temurin is a reliable choice)
FROM eclipse-temurin:25-jdk-noble

# Set environment variables for non-interactive installations
ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies required for X11 forwarding and JOGL/Java3D (OpenGL support)
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 \
    libglu1-mesa \
    libx11-6 \
    libxext6 \
    libxi6 \
    libxrender1 \
    libxtst6 \
    libcanberra-gtk-module \
    libcanberra-gtk3-module \
    x11-apps \
    && rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and project files
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle gradle.properties ./

# Grant execution permission to the Gradle wrapper
RUN chmod +x gradlew

# Download dependencies (this layer will be cached unless build files change)
RUN ./gradlew dependencies --no-daemon

# Copy the rest of the application source code
COPY src src
COPY cutters cutters
COPY manifest manifest

# Default command: build and then run the application
# We use --no-daemon as is standard in container environments
CMD ["./gradlew", "runBoardCAD", "--no-daemon"]
