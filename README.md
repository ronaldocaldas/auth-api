# Auth API - Authentication System with OAuth2, JWT and Refresh Token

## Description

This project is an authentication API built with Java 21, Spring Boot 3.2.x, and Kotlin, implementing:

- OAuth2 login (Google)
- JWT (JSON Web Token) generation and validation
- Refresh Token for access token renewal
- Support for multiple sessions per user
- Integration with PostgreSQL database
- Automatic API documentation with Swagger (SpringDoc OpenAPI)

---

## Technologies

- Java 21
- Kotlin 1.9
- Spring Boot 3.2.x
- Spring Security with OAuth2 Client
- JSON Web Token (jjwt)
- PostgreSQL (via Docker)
- Swagger/OpenAPI with SpringDoc

---

## How to run locally

### Prerequisites

- Docker and Docker Compose installed
- JDK 21 installed
- Gradle installed or use Gradle Wrapper (`./gradlew`)

---

### 1. Run PostgreSQL database with Docker

From the project root, run:

```bash
docker-compose up -d
