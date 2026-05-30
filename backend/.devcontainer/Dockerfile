FROM ghcr.io/dkakunsi/lab/java:1.2

COPY .m2/settings.xml /root/.m2/settings.xml
RUN chmod 700 "/root/.m2/settings.xml"

COPY .ssh/* "/root/.ssh/"
RUN chmod 600 "/root/.ssh/id_rsa"

# Default shell to bash
SHELL ["/bin/bash", "-c"]

