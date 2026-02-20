# Default Maven goal
GOAL=install

.PHONY: backend backend-test backend-version help

# Target for building the build
backend:
	@if [ -z "$(VERSION)" ]; then \
		mvn --file ./backend/pom.xml clean $(GOAL); \
	else \
		mvn --file ./backend/pom.xml clean $(GOAL) -Drevision=$(VERSION); \
	fi

# Target for testing the build
backend-test: GOAL=verify
backend-test: backend

backend-version:
	@echo "$$(mvn --file ./backend/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)"

# Usage instructions
help:
	@echo "Usage:"
	@echo "  make backend [GOAL=<goal>] [VERSION=<version>]				# Build the backend (default goal: verify)"
	@echo "  make report [TOKEN=<token>]        									# Build the report (optional: pass TOKEN for Sonar)"
	@echo "  make backend-test                          					# Run tests for the backend"
	@echo "  make backend-version                          					# Show the current backend version"
	@echo "  make help                          									# Show this help message"