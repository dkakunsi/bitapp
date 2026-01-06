#!/bin/bash

# Create docker config directory if it doesn't exist
sudo mkdir -p /etc/docker

# Copy daemon configuration
sudo cp /workspaces/lab/.devcontainer/daemon.json /etc/docker/daemon.json

# Start Docker daemon in the background with custom config
sudo dockerd > /tmp/dockerd.log 2>&1 &

# Wait for Docker to be ready (up to 30 seconds)
echo "Starting Docker daemon..."
for i in {1..30}; do
    if docker info >/dev/null 2>&1; then
        echo "Docker daemon is ready"
        exit 0
    fi
    sleep 1
done

echo "Docker daemon failed to start within 30 seconds"
cat /tmp/dockerd.log
exit 1
