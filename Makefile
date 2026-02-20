# Default Maven goal
GOAL=install

.PHONY: build report test

# Target for building the build
build-backend:
	@if [ -z "$(VERSION)" ]; then \
		mvn --file ./backend/pom.xml clean $(GOAL); \
	else \
		mvn --file ./backend/pom.xml clean $(GOAL) -Drevision=$(VERSION); \
	fi

# Target for testing the build
test-backend: GOAL=verify
test-backend: build-backend

# Usage instructions
help:
	@echo "Usage:"
	@echo "  make build-backend [GOAL=<goal>] [VERSION=<version>]	# Build the backend (default goal: verify)"
	@echo "  make report [TOKEN=<token>]        									# Build the report (optional: pass TOKEN for Sonar)"
	@echo "  make test-backend                          					# Run tests for the backend"
	@echo "  make help                          									# Show this help message"