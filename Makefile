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

# Tarefa para dar push nas imagens
.PHONY: push
push:
	@echo "Pushing Docker images to the repository"
	docker push $(IMAGE_NAME):$(VERSION)
	docker push $(IMAGE_NAME):latest

# Tarefa completa para buildar e dar push
.PHONY: build-and-push
build-and-push: build build-latest push
