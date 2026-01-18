# Image name
IMAGE_NAME ?= ghcr.io/vitormarcal/chatvault

# Image version/tag (e.g. 1.2.3-beta.1 or 1.2.3)
VERSION ?= dev

# Optional extra tags (e.g. latest)
EXTRA_TAGS ?=

DOCKERFILE ?= Dockerfile
CONTEXT ?= .

# ---------- Helpers ----------
define tag_image
	docker tag $(IMAGE_NAME):$(VERSION) $(IMAGE_NAME):$(1)
endef

define push_tag
	docker push $(IMAGE_NAME):$(1)
endef

# ---------- Help ----------
.PHONY: help
help:
	@echo ""
	@echo "ChatVault Docker Makefile"
	@echo ""
	@echo "Main commands:"
	@echo "  make dev VERSION=1.2.3-beta.1"
	@echo "    Build and push a dev/test image using only the VERSION tag."
	@echo ""
	@echo "  make release VERSION=1.2.3"
	@echo "    Build and push a release image using VERSION and also tag/push 'latest'."
	@echo "    Includes safety checks to prevent pre-release tags from being released as latest."
	@echo ""
	@echo "Lower-level commands:"
	@echo "  make build VERSION=1.2.3-beta.1"
	@echo "    Build the Docker image tagged as VERSION."
	@echo ""
	@echo "  make push VERSION=1.2.3-beta.1"
	@echo "    Push the Docker image tagged as VERSION."
	@echo ""
	@echo "  make build VERSION=1.2.3 EXTRA_TAGS=\"latest\""
	@echo "    Build VERSION and also add extra tags (without pushing them unless you run 'push')."
	@echo ""
	@echo "  make push VERSION=1.2.3 EXTRA_TAGS=\"latest\""
	@echo "    Push VERSION and also push extra tags."
	@echo ""
	@echo "  make check-release VERSION=1.2.3"
	@echo "    Validate that VERSION is not a pre-release (alpha/beta/rc/snapshot/dev) and not 'latest'."
	@echo ""
	@echo "Variables (optional):"
	@echo "  IMAGE_NAME=ghcr.io/vitormarcal/chatvault"
	@echo "  VERSION=dev"
	@echo "  EXTRA_TAGS=\"latest other\""
	@echo "  DOCKERFILE=Dockerfile"
	@echo "  CONTEXT=."
	@echo ""
	@echo "Notes:"
	@echo "  - 'dev' publishes only VERSION."
	@echo "  - 'release' publishes VERSION and 'latest'."
	@echo ""

# ---------- Targets ----------
.PHONY: build
build:
	@echo "Building Docker image: $(IMAGE_NAME):$(VERSION)"
	docker build -f $(DOCKERFILE) -t $(IMAGE_NAME):$(VERSION) $(CONTEXT)
	@for t in $(EXTRA_TAGS); do \
		echo "Adding additional tag: $(IMAGE_NAME):$$t"; \
		$(call tag_image,$$t); \
	done

.PHONY: push
push:
	@echo "Pushing Docker image: $(IMAGE_NAME):$(VERSION)"
	$(call push_tag,$(VERSION))
	@for t in $(EXTRA_TAGS); do \
		echo "Pushing additional tag: $(IMAGE_NAME):$$t"; \
		$(call push_tag,$$t); \
	done

# Dev/test builds: push only the explicit version tag
.PHONY: dev
dev: build push
	@echo "Dev image published with tag: $(VERSION)"

# Safety check for release tags
.PHONY: check-release
check-release:
	@echo "Validating release tag: $(VERSION)"
	@if [ "$(VERSION)" = "latest" ]; then \
		echo "ERROR: VERSION must not be 'latest'. Use a semantic version like 1.2.3."; \
		exit 1; \
	fi
	@if echo "$(VERSION)" | grep -Eiq '(^|[-+.])(alpha|beta|rc|snapshot|dev)'; then \
		echo "ERROR: Release tag must not be a pre-release (alpha/beta/rc/snapshot/dev)."; \
		echo "ERROR: Provided VERSION='$(VERSION)'"; \
		exit 1; \
	fi
	@echo "Release tag looks valid."

# Release builds: push version tag and also tag as latest
.PHONY: release
release: EXTRA_TAGS=latest
release: check-release build push
	@echo "Release image published with tags: $(VERSION), latest"
