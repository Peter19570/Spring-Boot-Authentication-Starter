Here's your complete, merged README file with all your improvements and architectural decisions integrated:

---

# Reusable Spring Boot Authentication System

A complete, fully built, and thoroughly tested authentication module for Spring Boot applications. It eliminates repetitive boilerplate code and provides everything you need for secure user authentication in one ready-to-use package.

---

## Key Features

- **JWT Authentication** — Access and refresh tokens, securely persisted in the database with proper invalidation on logout
- **OAuth 2.0 Social Login With Firebase** — Initially implemented with Spring Security OAuth Client (Google), later migrated to Firebase Authentication for a more streamlined and flexible OAuth flow
- **Email Verification** — On registration and during password reset
- **OTP-based Flows** — For password reset and email change
- **Rate Limiting** — On login and registration endpoints (configurable with Redis)
- **Audit Logging** — For critical user actions, stored in the database
- **Soft Delete** support
- **Decoupled, Customizable Email Templates**
- **Flexible Token Handling** — Automatically supports both HTTP-only cookies and `Authorization: Bearer` tokens via a smart filter
- **Async Email Sending** — Using Spring Boot's `@Async` annotation to keep API responses fast
- **Base Entities, Repositories, DTOs, Enums** (for audit actions), and custom exceptions

---

## Included Controllers

| Controller | Endpoints |
|-----------|-----------|
| **Auth** | Register, Login, Refresh Token, Logout, Verify Email, Forgot Password, Reset Password, Change Email (request + confirmation) |
| **Audit** | Get a Paginated list of Audit-logs |
| **User** | Delete Me (self-account deletion) |

---

## Architecture & Design Decisions

### Package-by-Feature Architecture

The project is structured using a **package-by-feature** approach rather than the traditional layered architecture (controller/service/repository split by type). This improves:
- **Modularity** — Each feature contains all its related components
- **Scalability** — Easy to add new features without touching existing ones
- **Maintainability** — Related code lives together, reducing cognitive load

### OAuth Evolution

- **Initial Implementation:** Google authentication using Spring Security OAuth Client
- **Current Implementation:** Firebase Authentication — chosen for a more streamlined and flexible OAuth flow with better cross-platform consistency

### Asynchronous Processing

Uses Spring Boot's default `@Async` for background tasks like email sending — lightweight and simple, no need to run extra infrastructure like RabbitMQ or Kafka for basic use. Since it's built with `@Async`, it can be easily upgraded to a full messaging system as your application grows.

### Separation of Concerns

All authentication logic is cleanly separated into its own `authentication` folder. Clone the project, rename it if desired, and start building your business logic outside this folder while keeping the authentication system intact and reusable.

---

## Optimizations & Improvements

Throughout the development process, the following enhancements were implemented:

- **Performance Optimizations** — Improved response times and reduced database overhead
- **Bug Fixes** — Addressed edge cases in token validation and OAuth flows
- **Developer Experience** — Cleaner API design, better error messages, and simplified configuration
- **Security Hardening** — Enhanced token revocation and rate limiting protection

---

## Security Highlights

- Secure JWT implementation with token revocation
- Rate limiting protection on Login and register endpoints with Bucket4J (Upgradable)
- Firebase Integration for OAuth (Google Login — easily extensible to other providers)
- Custom exceptions ready for global exception handling
- HTTP-only cookie support for secure token storage
- Audit logging for critical user actions

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

# Firebase / Google OAuth
app.google.client.id=

# Email (SMTP)
spring.mail.host=
spring.mail.port=587
spring.mail.username=
spring.mail.password=

# CORS & Frontend
app.cors.allowed-origins=
app.frontend.url=

# Redis (Optional — for rate limiting)
spring.data.redis.host=
spring.data.redis.port=6379
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
