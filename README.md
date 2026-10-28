# Reusable Spring Boot Authentication System

A complete authentication module for Spring Boot applications. It gets rid of the repetitive auth boilerplate and gives you everything for secure user authentication in one ready-to-use package: JWT sessions, Google sign-in, passkeys, email flows, rate limiting and audit logging.

---

## Key Features

- **JWT Authentication** — Short-lived access tokens plus refresh tokens. Only a SHA-256 hash of each refresh token is stored in the database. Refresh tokens are rotated on every use (with a short grace window for concurrent requests), and replaying an old one revokes every session of that user and is audited.
- **Google Sign-In** — The client sends a Google ID token; the server verifies it (audience and verified email) and returns its own tokens. Several client IDs (web / Android / iOS) are supported.
- **Passkeys (WebAuthn)** — Register, list, delete and log in with passkeys using Spring Security's WebAuthn support. Challenges are kept in memory, so there is no HTTP session.
- **Email Verification & Password Reset** — Verification on registration, password reset, and email change (which also requires the current password). Tokens are one-time use.
- **OTP-based Flows** — Account deletion uses a 6-digit emailed code with limited attempts.
- **Account Protection** — BCrypt password hashing, temporary lockout after repeated failed logins, no account enumeration on login or forgot-password.
- **Rate Limiting** — Bucket4j (greedy refill) per endpoint and client IP, held in server memory. Use Redis when you scale out.
- **Audit Logging** — Event-driven and async, so it stays loosely coupled. Users can read their own history (`GET /api/v1/audits/me`); the full log is admin-only. Delete the `audit` folder to remove it.
- **Soft Delete** — Marks the user as deleted and removes all their refresh tokens and passkeys.
- **Customizable Email Templates** — Ready-made Thymeleaf HTML templates under `src/main/resources/templates`, easy to restyle.
- **Flexible Token Handling** — Tokens are issued to the client as JSON and sent back as `Authorization: Bearer`. `JwtFilter` also accepts an access-token cookie; remove that block if you don't use cookies, since CSRF protection is disabled.
- **Async Email Sending** — Spring's `@Async` keeps API responses fast. Switch to RabbitMQ or similar when you scale horizontally.
- **Swagger Documented** — OpenAPI docs with bearer auth. Disabled by default; project info is set in `application.properties` (`app.swagger.*`).
- **In-Memory Caching** — Caffeine caches the authenticated-user lookup. Use Redis when you run more than one instance.

---

## Tech Stack

Java 25 · Spring Boot 4.1.1 · Spring Security (JWT + WebAuthn) · Spring Data JPA / Hibernate · PostgreSQL · Flyway (included, disabled by default) · JJWT 0.12 · Google API client · Bucket4j · Caffeine · Thymeleaf + Spring Mail · MapStruct · Lombok · springdoc-openapi · Actuator

---

## Project Structure

```
com.example.authstarter
├── features
│   ├── audit      # audit logs: model, repo, service, controller
│   ├── auth       # login, register, tokens, passkeys, email flows, security config
│   ├── shared     # config, constants, DTOs, exceptions, utils
│   └── user       # current user + account deletion
├── handler        # GlobalExceptionHandler
└── AuthStarterApplication.java
```

---

## Getting Started

**Requirements:** JDK 25, Maven, a PostgreSQL database, an SMTP account (for emails) and a Google OAuth client ID (for Google sign-in).

1. Create an empty PostgreSQL database.
2. Copy the example config and fill it in:
   ```
   cp .env.example .env            # PowerShell: Copy-Item .env.example .env
   ```
3. First run only: set `DDL_AUTO=update` in `.env` so Hibernate creates the tables (use `create` for a throwaway local database, it wipes data). Afterwards use `validate`.
4. Start the app:
   - From an IDE or a run configuration that loads `.env`, just run `AuthStarterApplication`.
   - From a terminal, `mvn spring-boot:run` does **not** read `.env`. Load it into the session first (PowerShell):
     ```powershell
     Get-Content .env | Where-Object { $_ -match '^\s*[A-Za-z_][A-Za-z0-9_]*\s*=' } | ForEach-Object {
       $name, $value = $_ -split '=', 2
       [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim().Trim('"').Trim("'"), 'Process')
     }
     mvn spring-boot:run
     ```
5. Set `SWAGGER_ENABLED=true` in development to open the docs at `/swagger-ui/index.html`.

---

## Configuration

Everything is configured through environment variables (see `.env.example` for the full, commented list).

| Variable | Required | Purpose |
|---|---|---|
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | yes | PostgreSQL connection |
| `DDL_AUTO` | no (`validate`) | Hibernate schema mode: `validate`, `update`, `create` |
| `JWT_SECRET` | yes | Base64 secret, at least 32 bytes |
| `JWT_ACCESS_TOKEN_EXPIRATION`, `JWT_REFRESH_TOKEN_EXPIRATION` | no (`PT15M`, `P7D`) | Token lifetimes (ISO-8601 or `15m` / `7d`) |
| `CORS_ORIGINS_ALLOWED` | yes | Comma-separated exact origins (no `*`) |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | yes | SMTP settings |
| `MAIL_FRONTEND_URL` | yes | Base URL used in email links |
| `APP_MAIL_FROM` | no | Sender address if the SMTP username isn't one |
| `WEBAUTHN_ALLOWED_ORIGINS`, `WEBAUTHN_RP_ID` | yes | Passkey origins and relying-party domain |
| `GOOGLE_CLIENT_ID` | yes | Google OAuth client ID(s), comma-separated |
| `SWAGGER_ENABLED` | no (`false`) | Publish the API docs |
| `EXPOSED_ENDPOINTS`, `BASE_URL` | no | Actuator endpoints and base path |
| `APP_NAME` | no | Application name (also used as the passkey RP name) |

Project info shown in Swagger (title, description, version, contact) is set with the `app.swagger.*` keys in `application.properties`.

---

## API Overview

All endpoints are under `/api/v1`. **Public** endpoints need no token; everything else needs `Authorization: Bearer <access token>`.

| Area | Endpoint | Access |
|---|---|---|
| Auth | `POST /auth/register`, `/auth/login`, `/auth/google`, `/auth/refresh`, `/auth/logout` | public |
| Email | `GET /auth/verify-email`, `GET /auth/confirm-email` | public |
| Email | `POST /auth/resend-verification-email`, `/auth/change-email` | authenticated |
| Password | `POST /auth/forgot-password`, `/auth/reset-password` | public |
| Passkeys | `POST /auth/passkeys/challenge`, `/auth/passkeys/login` | public |
| Passkeys | `POST /auth/passkeys/initialize`, `/auth/passkeys/register`; `GET /auth/passkeys`; `DELETE /auth/passkeys/{id}` | authenticated |
| User | `GET /users/me`, `POST /users/me/deletion-request`, `DELETE /users/me` | authenticated |
| Audit | `GET /audits/me` | authenticated |
| Audit | `GET /audits` | admin only (closed until you add roles) |

**Responses:** success is `{ "msg": "...", "data": ... }`. Errors are `{ "msg": "Error occurred: <STATUS>", "data": ... }`; validation errors put `{ field: message }` in `data`. Token responses return `access`, `refresh` and `expiresIn` (seconds). Paged responses are 1-based.

---

## Architecture & Design Decisions

### Package-by-Feature Architecture

Structured by **feature** since that has been my preferred style after I discovered it:
* **Modularity** — Each feature contains all its related components.
* **Scalability** — Easy to add new features without touching existing ones.
* **Maintainability** — Related code lives together, reducing cognitive load.

### How Sessions Work

- Access tokens live 15 minutes by default and are never stored server-side.
- Refresh tokens are stored hashed. Each refresh issues a new pair; the old refresh token is revoked, with a few seconds of grace for concurrent requests.
- Logout deletes the refresh token and needs no access token, so it works even after the access token expired.
- Resetting a password or deleting an account ends all of that user's sessions.
- **Client storage:** on mobile keep the refresh token in secure storage (Keychain / Keystore). On the web, avoid JS-readable storage; let your Next.js server set tokens as `HttpOnly; Secure; SameSite` cookies and call this API server-side.

### Google Sign-In

The first implementation used Spring Security's OAuth client, which doesn't suit mobile apps. The current flow has the client sign in with Google, send the **Google ID token** to `/auth/google`, and get this server's own tokens back. This works the same for web and mobile.

### Asynchronous Processing

Email sending and audit writes use Spring's `@Async` with their own thread pools. It's lightweight and needs no extra infrastructure, and can be upgraded to a message broker as you grow.

### Temporal Storage

Short-lived data lives in **Caffeine (in-memory cache)**:

* **Authenticated users** — cached user lookups.
* **One-time tokens and codes** — email verification, password reset, account deletion OTPs.
* **Passkey challenges** — registration and login challenges.
* **Rate-limiting buckets** — token buckets per endpoint and client IP.

> **Note:** Caffeine is in-memory, so this data is lost on restart and not shared between instances. If you run more than one instance, move it to Redis (Bucket4j has a Redis integration too).

---

## Production Checklist

- Use HTTPS only, and a strong random `JWT_SECRET`.
- `DDL_AUTO=validate` plus real migrations (Flyway is already a dependency; enable it and add your migrations).
- Exact CORS origins; `SWAGGER_ENABLED=false`; `EXPOSED_ENDPOINTS=health,info`.
- Behind a reverse proxy, keep `server.forward-headers-strategy=native` so rate limits and audit logs see the real client IP, and check it after deploying.
- Passkeys need `WEBAUTHN_RP_ID` and `WEBAUTHN_ALLOWED_ORIGINS` to match the real domain.
- More than one instance: move Caffeine state to Redis.
- Never log tokens or request bodies on `/auth/**`.

## Known Limitations

- **No roles yet.** Admin-only endpoints (the full audit log) return 403 until you add a role to `User`, grant it in `UserPrincipal`, and include it in the JWT.
- **Deleted accounts keep their email reserved**, so the same email can't register again. Decide whether to anonymize it on delete or offer reactivation.
- **In-memory state** (see Temporal Storage) is single-instance only.