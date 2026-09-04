# 🎫 Tatakae — Full-Stack Integration

Academic project for the Talento Ready program (Desafío Latam / Globant). This repository is the Spring Boot microservice: athletes, friendships, training sessions and leaderboards, persisted in PostgreSQL.

---

## 🛠️ Tech Stack

* **Backend:** Java 21, Spring Boot 3, Spring Data JPA, Hibernate, OpenAPI/Swagger.
* **Frontend:** Vanilla TypeScript, Vite, Native ESM, semantic HTML5/CSS3.
* **Infrastructure:** Docker Compose, PostgreSQL 16 Alpine.
* **Quality and testing:** JUnit 5, Mockito, JaCoCo, TDD and Clean Architecture.

---

## 🔗 Reference Repositories

* Domain core / Milestone 1: https://github.com/yeikobu/tatakae-backend
* Backend Spring Boot / Milestone 4: https://github.com/yeikobu/tatakae-api
* Frontend Vite + TS / Milestone 2: https://github.com/yeikobu/dragonball-wiki

The UI that talks to this API in the final delivery is https://github.com/yeikobu/Tatakae-frontend (clone it next to this repository).

---

## 🚀 Local Getting Started Guide

Clone both integration repositories into the same parent folder:

```bash
git clone https://github.com/yeikobu/tatakae-api.git
git clone https://github.com/yeikobu/Tatakae-frontend.git
```

### 1. Start the relational database

```bash
cd tatakae-api
docker compose up -d
```

PostgreSQL 16 Alpine listens on `localhost:5432` (database `tatakae_db`, user `tatakae_user`). Override `DB_NAME`, `DB_USER`, `DB_PASSWORD` and `DB_PORT` with environment variables if needed. Production credentials are never hardcoded: `application-prod.yaml` reads `DB_USER` and `DB_PASSWORD` with no fallback.

### 2. Run the automated tests

```bash
./mvnw clean test
```

JUnit 5 + Mockito, against PostgreSQL via Testcontainers. JaCoCo enforces 100% line and branch coverage on `./mvnw verify`.

### 3. Start the backend microservice

```bash
./mvnw spring-boot:run
```

`application.yaml` sets `spring.profiles.default: dev`, so Swagger is on and the local database is used.

* REST API: http://localhost:8080/api/v1
* Swagger UI (dev profile only): http://localhost:8080/swagger-ui.html

Controllers declare `@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})`. Production (`SPRING_PROFILES_ACTIVE=prod`) keeps Swagger disabled.

### 4. Start the web frontend

```bash
cd ../Tatakae-frontend
cp .env.example .env
npm install
npm run dev
```

* Web app: http://localhost:5173
* API base URL (`.env`): `http://localhost:8080/api/v1`
