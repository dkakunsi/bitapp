# Default Maven goal
GOAL=verify

.PHONY: build report test release

# Target for testing the build
test:
	mvn --file ./pom.xml clean verify

# Target for building the build
build:
	mvn --file ./pom.xml clean $(GOAL)

# Target for building the report with Sonar
report:
	@if [ -z "$(TOKEN)" ]; then \
		mvn --file ./report/pom.xml clean verify; \
	else \
		mvn --file ./report/pom.xml clean verify -Dsonar.token=$(TOKEN); \
	fi

# Usage instructions
help:
	@echo "Usage:"
	@echo "  make build [GOAL=<goal>]           # Build the build (default goal: verify)"
	@echo "  make report [TOKEN=<token>]        # Build the report (optional: pass TOKEN for Sonar)"
	@echo "  make test                          # Run tests for the build"
	@echo "  make help                          # Show this help message"