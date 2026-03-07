FROM ghcr.io/dkakunsi/lab/java:1.0

# Copy private key for cloning from git repositories
COPY .ssh/* "/root/.ssh/"
RUN chmod 600 "/root/.ssh/id_rsa"

# Default shell to bash
SHELL ["/bin/bash", "-c"]

