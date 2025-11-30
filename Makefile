# Nome da imagem
IMAGE_NAME = ghcr.io/vitormarcal/chatvault

# Versão da imagem (informe via linha de comando ou utilize a padrão "dev")
VERSION ?= dev

# Tarefa padrão de build
.PHONY: build
build:
	@echo "Building Docker image with version: $(VERSION)"
	docker build -t $(IMAGE_NAME):$(VERSION) .

# Tarefa para buildar a versão latest
.PHONY: build-latest
build-latest:
	@echo "Building Docker image with version: latest"
	docker build -t $(IMAGE_NAME):latest .

.PHONY: build-dev
build-dev:
	@echo "Building Docker image with version: dev"
	docker build -t $(IMAGE_NAME):dev .

# Tarefa para dar push nas imagens
.PHONY: push
push:
	@echo "Pushing Docker images to the repository"
	docker push $(IMAGE_NAME):$(VERSION)
	docker push $(IMAGE_NAME):latest

# Push só da imagem dev
.PHONY: push-dev
push-dev:
	@echo "Pushing dev image to the repository"
	docker push $(IMAGE_NAME):dev

# Tarefa completa para buildar e dar push
.PHONY: build-and-push
build-and-push: build build-latest push

.PHONY: build-dev-and-push
build-dev-and-push: build-dev push-dev
