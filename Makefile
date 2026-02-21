# Default Maven goal
GOAL=install

.PHONY: backend backend-test backend-version backend-chart help

# Usage instructions
help:
	@echo "Usage:"
	@echo "  make backend [GOAL=<goal>] [VERSION=<version>]				# Build the backend (default goal: install)"
	@echo "  make backend-test                          					# Run tests for the backend"
	@echo "  make backend-chart TOKEN=<token> VERSION=<version>   # Trigger helm-charts release workflow"
	@echo "  make help                          									# Show this help message"

# Target for building the backend
backend:
	@if [ -z "$(VERSION)" ]; then \
		mvn --file ./backend/pom.xml clean $(GOAL); \
	else \
		mvn --file ./backend/pom.xml clean $(GOAL) -Drevision=$(VERSION); \
	fi

backend-test: GOAL=verify
backend-test: backend

backend-chart:
	if [ -z "$(TOKEN)" ] || [ -z "$(VERSION)" ]; then \
		echo "TOKEN and VERSION are required. Usage: make backend-chart TOKEN=<github_token> VERSION=<version>"; \
		exit 1; \
	fi; \
	echo "Releasing bitapp chart version: $(VERSION)"; \
	curl --fail-with-body -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $(TOKEN)" \
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  https://api.github.com/repos/dkakunsi/helm-charts/actions/workflows/release.yml/dispatches \
	  -d '{"ref":"master","inputs":{"app":"bitapp","version":"'"$(VERSION)"'"}}'
