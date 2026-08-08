# TaskFlow API

A RESTful task management API built with **Spring Boot 3.2** and **Java 21**, featuring JWT authentication with refresh-token rotation, PostgreSQL persistence, and Docker-based local development. Built as a portfolio project to demonstrate production-oriented Spring Boot patterns — layered architecture, JPA relationship modeling, Spring Security, and clean exception handling.

## Status

🚧 **Phase 1 — User & Authentication** (in progress)

Phase 1 covers user registration, login, JWT access/refresh tokens, and logout. Phase 2 (Task CRUD with specification-based filtering and pagination) is planned next.

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Runtime | Java 21, Maven |
| Framework | Spring Boot 3.2.x |
| Persistence | Spring Data JPA (Hibernate), PostgreSQL 15 |
| Security | Spring Security, JWT (jjwt 0.12.x) |
| Validation | Jakarta Bean Validation |
| Mail | Spring Mail + Thymeleaf templates, MailDev (local SMTP) |
| Boilerplate | Lombok |
| Local infra | Docker Compose (PostgreSQL + MailDev) |
| Testing | JUnit 5 |
| API Docs | SpringDoc OpenAPI *(planned)* |

## Architecture

Standard layered Spring Boot structure:

```
com.taskflow
├── config          # SecurityConfig, DataInitializer
├── controller       # REST endpoints
├── dto
│   ├── request       # Inbound request payloads (validated)
│   └── response       # Outbound response payloads
├── entity            # JPA entities (User, Role, RefreshToken)
├── exception          # Custom exceptions + global handler
├── repository         # Spring Data JPA repositories
├── security           # JWT filter, UserDetails/UserDetailsService
└── service            # Business logic (Auth, Jwt, RefreshToken)
```

**Auth flow at a glance:**

```
Client                AuthController        AuthService         Spring Security
  │  POST /auth/login       │                    │                     │
  ├─────────────────────────▶                    │                     │
  │                          ├────────────────────▶                     │
  │                          │                    ├─────────────────────▶ authenticate()
  │                          │                    │                     │  → UserDetailsService
  │                          │                    │                     │  → BCrypt match
  │                          │                    ◀─────────────────────┤
  │                          │            JwtService.generateAccessToken │
  │                          │            RefreshTokenService.create...  │
  │                          ◀────────────────────┤                     │
  ◀─────────────────────────┤  AuthResponse (access + refresh token)     │
```

## Getting Started

### Prerequisites

- Java 21
- Maven (or use the included `./mvnw` wrapper)
- Docker & Docker Compose

### 1. Start local infrastructure

```bash
docker compose up -d
```

This starts:
- **PostgreSQL 15** on `localhost:5432` (db: `taskflow`, user: `taskflow_user`)
- **MailDev** on `localhost:1080` (web UI) / `localhost:1025` (SMTP) — catches outgoing emails locally, nothing is sent to real inboxes

### 2. Configure environment

Copy the example env file and adjust as needed:

```bash
cp .env.example .env
```

| Variable | Purpose | Default |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL connection | `localhost:5432/taskflow` |
| `JWT_SECRET` | Base64-encoded HMAC signing key | *(set your own — see below)* |
| `JWT_ACCESS_EXPIRATION` | Access token TTL, in ms | `900000` (15 min) |
| `JWT_REFRESH_EXPIRATION` | Refresh token TTL, in ms | `604800000` (7 days) |
| `MAIL_HOST`, `MAIL_PORT` | SMTP target | `localhost:1025` (MailDev) |
| `SERVER_PORT` | App port | `8080` |

**Generating a JWT secret:** the signing key must be Base64 and decode to at least 256 bits for HS256. Generate one with:
```bash
openssl rand -base64 32
```

### 3. Run the app

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` with the `dev` profile active. On first startup, `DataInitializer` seeds the `ROLE_USER` and `ROLE_ADMIN` roles automatically.

### 4. Run tests

```bash
./mvnw test
```

## API Endpoints

Base path: `/api/v1/auth`

| Method | Endpoint | Auth required | Description |
|---|---|---|---|
| `POST` | `/register` | No | Register a new user |
| `POST` | `/login` | No | Authenticate, receive access + refresh tokens |
| `POST` | `/refresh` | No (refresh token in body) | Exchange a valid refresh token for a new access token |
| `POST` | `/logout` | No (refresh token in body) | Revoke a refresh token |

All other endpoints require a valid JWT in the `Authorization: Bearer <token>` header.

### Example: Register

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "[email protected]",
    "password": "secret123"
  }'
```

### Example: Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "[email protected]",
    "password": "secret123"
  }'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "b3f1c2d4-...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Example: Refresh

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "b3f1c2d4-..."}'
```

## Design Notes

- **Access tokens** are stateless JWTs (HS256), short-lived (15 min), and carry the user's email as the `sub` claim.
- **Refresh tokens** are opaque, server-side-tracked UUIDs stored in the `refresh_tokens` table — this allows revocation (logout) in a way a stateless JWT alone can't support.
- **Passwords** are hashed with BCrypt (adaptive, salted per-password — never compared with plain equality).
- **Roles** use a `@ManyToMany` relationship between `User` and `Role`, seeded on startup via `DataInitializer`.

## Roadmap

- [x] Phase 1 — User registration, login, JWT auth, refresh/logout
- [ ] Phase 2 — Task CRUD with specification-based filtering and pagination
- [ ] Phase 3 — Role-based authorization on task endpoints
- [ ] SpringDoc OpenAPI / Swagger UI
- [ ] Integration tests (Testcontainers)

## Author

Built by [Prince Kumar](https://github.com/kumarprince06) as a portfolio project while transitioning into Java backend engineering.