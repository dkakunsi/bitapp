FROM ubuntu:24.04

## BASIC SETUP
# Explicitly prevent adding other architectures
# Install basic dependencies
RUN rm -f /etc/dpkg/dpkg.cfg.d/multiarch && \
    apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y \
    locales \
    watchman \
    wget \
    unzip \
    sudo \
    zip \
    curl \
    git-all \
    xz-utils \
    libglu1-mesa \
    libc6 \
    libstdc++6 \
    libbz2-1.0 \
    libgtk-4-1 \
    libadwaita-1-0 \
    lsb-release \
    gnupg \
    ca-certificates \
    build-essential && \
    locale-gen en_US.UTF-8 && \
    update-locale LANG=en_US.UTF-8

ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US:en
ENV LC_ALL=en_US.UTF-8

# Create a non-root user
ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID
RUN groupadd --gid $USER_GID $USERNAME 2>/dev/null || groupmod -n $USERNAME $(getent group $USER_GID | cut -d: -f1) && \
    useradd --uid $USER_UID --gid $USER_GID -m $USERNAME 2>/dev/null || usermod -l $USERNAME -d /home/$USERNAME -m $(getent passwd $USER_UID | cut -d: -f1) && \
    echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME && \
    chmod 0440 /etc/sudoers.d/$USERNAME

## JAVA AND MAVEN SETUP
# Install JDK 25 Temurin
# Set JAVA_HOME dynamically based on architecture
RUN apt-get install -y apt-transport-https && \
    wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - && \
    echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list && \
    apt-get update && \
    apt-get install -y temurin-25-jdk && \
    JAVA_DIR=$(ls -d /usr/lib/jvm/temurin-25-jdk-* 2>/dev/null | head -n 1) && \
    echo "export JAVA_HOME=${JAVA_DIR}" >> /etc/environment && \
    echo "export PATH=\${JAVA_HOME}/bin:\${PATH}" >> /etc/environment && \
    ln -s ${JAVA_DIR} /usr/lib/jvm/temurin-25-jdk

ENV JAVA_HOME=/usr/lib/jvm/temurin-25-jdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Install Maven
ARG MAVEN_VERSION=3.9.9
RUN wget https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -O /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven && \
    rm /tmp/maven.tar.gz

ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

## NODE.JS SETUP
# Install Node.js 22
# Install TypeScript and Vite globally
RUN mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_22.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list && \
    apt-get update && \
    apt-get install -y nodejs && \
    npm install -g typescript vite

## FLUTTER SETUP
# Install Gradle
ARG GRADLE_VERSION=8.11.1
RUN apt-get install -y \
    android-sdk && \
    wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -O /tmp/gradle.zip && \
    unzip /tmp/gradle.zip -d /opt && \
    ln -s /opt/gradle-${GRADLE_VERSION} /opt/gradle && \
    rm /tmp/gradle.zip

ENV GRADLE_HOME=/opt/gradle
ENV PATH="${GRADLE_HOME}/bin:${PATH}"

# Set environment variables
ENV ANDROID_SDK_ROOT=/usr/lib/android-sdk
ENV FLUTTER_SDK_ROOT=/usr/lib/flutter
ENV FLUTTER_SDK_VERSION=3.27.1-stable

# Include flutter and android tools in path
ENV PATH="${PATH}:${FLUTTER_SDK_ROOT}/bin:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools"

# Enable noninteractive installation
ENV DEBIAN_FRONTEND=noninteractive

# Add flutter to safe directory in git
# Configure git user name and email
# Set git config for the vscode user as well
RUN git config --global --add safe.directory /usr/lib/flutter && \
    git config --global user.name "Deddy Kakunsi" && \
    git config --global user.email "deddy.kakunsi@gmail.com" && \
    mkdir -p /home/vscode/.gitconfig.d && \
    git config --file /home/vscode/.gitconfig user.name "Deddy Kakunsi" && \
    git config --file /home/vscode/.gitconfig user.email "deddy.kakunsi@gmail.com" && \
    chown -R vscode:vscode /home/vscode/.gitconfig /home/vscode/.gitconfig.d

# Download and install Android Command Line Tools
# Accept Android SDK licenses and install necessary components
# Install Flutter SDK using Git (works for all architectures)
# Disable Flutter telemetry
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    wget -O commandlinetools.zip "https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip" && \
    unzip commandlinetools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm commandlinetools.zip && \
    yes | ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --licenses && \
    ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0" && \
    git clone https://github.com/flutter/flutter.git -b stable ${FLUTTER_SDK_ROOT} && \
    flutter --disable-analytics

## DOCKER SETUP
# Install Docker inside docker
RUN install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg && \
    chmod a+r /etc/apt/keyrings/docker.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
    $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin && \
    usermod -aG docker ${USERNAME}

## AI Agents SETUP
# Install Ghostty terminal emulator
# Install OpenCode Agent
# Install Copilot CLI
USER vscode
RUN curl -fsSL https://raw.githubusercontent.com/mkasberg/ghostty-ubuntu/HEAD/install.sh && \
    curl -fsSL https://opencode.ai/install | bash && \
    curl -fsSL https://gh.io/copilot-install | bash
USER root

# Clean everything
RUN apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy private key for cloning from git repositories
COPY .ssh/* /home/vscode/.ssh/
RUN chmod 600 /home/vscode/.ssh/id_rsa && \
    chown vscode:vscode /home/vscode/.ssh/*

# Default shell to bash
SHELL ["/bin/bash", "-c"]

