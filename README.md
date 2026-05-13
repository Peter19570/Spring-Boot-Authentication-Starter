# Reusable Spring Boot Authentication System

A complete, fully built, and thoroughly tested authentication module for Spring Boot applications. It eliminates repetitive boilerplate code and provides everything you need for secure user authentication in one ready-to-use package.

---

## Key Features

- **JWT Authentication** — Access and refresh tokens, securely persisted in the database with proper invalidation on logout
- **OAuth 2.0 Social Login With Firebase** — Currently configured for Google, easily extensible to other providers
- **Email Verification** — On registration and during password reset
- **OTP-based Flows** — For password reset and email change
- **Rate Limiting** — On login and registration endpoints, you can let redis handle this one
- **Audit Logging** — For critical user actions, stored in the database
- **Soft Delete** support
- **Decoupled, Customizable Email Templates**
- **Flexible Token Handling** — Automatically supports both HTTP-only cookies and `Authorization: Bearer` tokens via a smart filter
- **Async Email Sending** — Using Spring Boot's `@Async` annotation to keep API responses fast
- **Base Entities, Repositories, DTOs, Enums** (for roles and audit actions), and custom exceptions

---

## Included Controllers

| Controller | Endpoints |
|-----------|-----------|
| **Auth** | Register, Login, Refresh Token, Logout, Verify Email, Forgot Password, Reset Password, Change Email (request + confirmation) |
| **Audit** | Get a Paginated list of Audit-logs |
| **User** | Delete Me (self-account deletion) |

---

## Architecture & Extensibility

The module uses Spring Boot's default `@Async` for background tasks like email sending — lightweight and simple, no need to run extra infrastructure like RabbitMQ or Kafka for basic use. Since it's built with `@Async`, it can be easily upgraded to a full messaging system as your application grows.

All authentication logic is cleanly separated into its own `authentication` folder. Clone the project, rename it if desired, and start building your business logic outside this folder while keeping the authentication system intact and reusable.

---

## Security Highlights

- Secure JWT implementation with token revocation
- Rate limiting protection on sensitive endpoints
- Firebase Integration for OAuth(Google Login)
- Custom exceptions ready for global exception handling

---

## Configuration

Set the following in `application.properties` (or via environment variables):

```properties
# Database
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=

# JWT
jwt.token.secret.key=
jwt.token.access.token.expiration=PT15M
jwt.token.refresh.token.expiration=P7D

# Google Client ID
app.google.client.id=

# Email (SMTP)
spring.mail.host=
spring.mail.port=587
spring.mail.username=
spring.mail.password=

# CORS & Frontend
app.cors.allowed-origins=
app.frontend.url=
```

Once configured, the full authentication system is ready to go.

---

## Getting Started

```bash
# 1. Clone the repository
git clone <repo-url>

# 2. Rename the project (optional)

# 3. Update configuration values in application.properties

# 4. Run the application
./mvnw spring-boot:run
```

No more building JWT authentication, OAuth, email verification, or security flows from scratch on every project. This module gives you production-ready authentication out of the box — simple, clean, and extensible.

> Perfect as a solid foundation for your Spring Boot applications.
