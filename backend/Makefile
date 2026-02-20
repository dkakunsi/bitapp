# Default Maven goal
GOAL=install

.PHONY: build report test

# Target for building the build
build:
	@if [ -z "$(VERSION)" ]; then \
		mvn --file ./pom.xml clean $(GOAL); \
	else \
		mvn --file ./pom.xml clean $(GOAL) -Drevision=$(VERSION); \
	fi

# Target for testing the build
test: GOAL=verify
test: build

# Usage instructions
help:
	@echo "Usage:"
	@echo "  make build [GOAL=<goal>] [VERSION=<version>]	# Build the build (default goal: verify)"
	@echo "  make report [TOKEN=<token>]        					# Build the report (optional: pass TOKEN for Sonar)"
	@echo "  make test                          					# Run tests for the build"
	@echo "  make help                          					# Show this help message"