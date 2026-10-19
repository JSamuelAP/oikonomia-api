# Oikonomía API

Proyecto personal para ayudar a registrar ingresos, gastos y seguimiento.

Un monolito modular construido con Spring Boot 4.1.1, Spring Modulith y Java 25. Organizado por dominio (user, category,
transaction, budget) con arquitectura hexagonal/DDD.

Actualmente en desarrollo - más funciones y tecnologías por venir.

## Objetivos

- Registrar ingresos y gastos
- Seguimiento de gastos por categoría
- Hacer seguimiento de transacciones y presupuestos

## Stack

- Java 25, Spring Boot 4.1.1
- Spring Modulith para límites de módulo
- MapStruct, Lombok, JWT
- PostgreSQL (Testcontainers para las pruebas)

## Estado

En desarrollo - consulta AGENTS.md para el setup de desarrollo.

## Ejecución

### Docker compose

Levantar base de datos Postgres y API

```bash
docker compose --profile app up -d --build
```

Levantar solo base de datos

```bash
docker compose up -d
```

Levantar solo la API

```bash
docker build -t oikonomia-api .
```

Eliminar servicios y datos

```bash
docker compose --profile app down -v
```

### Dockerfile

```bash
docker run --rm --name oikonomia-api -p 8081:8080 `
  --env-file .env `
  -e DB_URL='jdbc:postgresql://host.docker.internal:5432/oikonomia' `
  -e SPRING_PROFILES_ACTIVE=dev `
  oikonomia-api
```