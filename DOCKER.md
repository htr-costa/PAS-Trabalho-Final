# Docker - Guia de Execução

## 🐳 Executar com Docker Compose (Recomendado)

### Pré-requisitos

- Docker Desktop instalado e rodando
- Docker Compose (incluído no Docker Desktop)

### Construir e Executar

```powershell
# Construir e iniciar todos os serviços
docker-compose up --build

# Ou em background (detached mode)
docker-compose up -d --build
```

### Parar os Serviços

```powershell
# Parar e remover containers
docker-compose down

# Parar, remover containers e volumes
docker-compose down -v
```

### Ver Logs

```powershell
# Ver logs de todos os serviços
docker-compose logs -f

# Ver logs de um serviço específico
docker-compose logs -f estoque-service
docker-compose logs -f main-service
```

## 🔧 Executar Containers Individuais

### Microserviço de Estoque

```powershell
# Construir imagem
docker build -t telepizza-estoque -f estoque-microservice/Dockerfile .

# Executar container
docker run -d -p 8081:8081 --name estoque telepizza-estoque
```

### Serviço Principal

```powershell
# Construir imagem
docker build -t telepizza-main -f Dockerfile .

# Executar container (conectando ao serviço de estoque)
docker run -d -p 8080:8080 --name main \
  -e ESTOQUE_MICROSERVICE_URL=http://estoque:8081 \
  --link estoque:estoque \
  telepizza-main
```

## 🧪 Testar os Serviços

Após iniciar os containers:

```powershell
# Testar microserviço de estoque
curl http://localhost:8081/api/estoque

# Testar serviço principal
curl http://localhost:8080/api/cardapios
```

## 📊 Acessar Consoles H2

- Microserviço de Estoque: http://localhost:8081/h2-console
- Serviço Principal: http://localhost:8080/h2

## 🔍 Comandos Úteis

```powershell
# Ver containers rodando
docker ps

# Ver logs de um container
docker logs telepizza-estoque
docker logs telepizza-main

# Acessar shell de um container
docker exec -it telepizza-estoque sh
docker exec -it telepizza-main sh

# Parar containers
docker stop telepizza-estoque telepizza-main

# Remover containers
docker rm telepizza-estoque telepizza-main

# Remover imagens
docker rmi telepizza-estoque telepizza-main
```

## 🚀 Workflow Completo

```powershell
# 1. Construir e iniciar
docker-compose up -d --build

# 2. Verificar se estão rodando
docker-compose ps

# 3. Ver logs
docker-compose logs -f

# 4. Testar APIs
curl http://localhost:8081/api/estoque
curl http://localhost:8080/api/cardapios

# 5. Parar quando terminar
docker-compose down
```

## ⚙️ Configurações

Os serviços estão configurados com:

### Microserviço de Estoque (estoque-service)

- **Porta**: 8081
- **Banco**: H2 em memória (estoquedb)
- **Health Check**: GET /api/estoque

### Serviço Principal (main-service)

- **Porta**: 8080
- **Banco**: H2 em memória (pizzadb)
- **Conecta com**: estoque-service via rede Docker
- **Health Check**: GET /api/cardapios

## 🌐 Networking

Os serviços se comunicam através de uma rede Docker chamada `telepizza-network`. O serviço principal acessa o microserviço de estoque via `http://estoque-service:8081`.

## 📝 Notas

- Os containers usam imagens Alpine para serem leves
- Build em múltiplos estágios (multi-stage) para otimizar tamanho
- Health checks garantem que os serviços estejam prontos antes de aceitar tráfego
- O serviço principal só inicia após o microserviço de estoque estar saudável
