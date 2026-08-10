# Reusable Spring Boot Authentication System

A complete, fully built, and thoroughly tested authentication module for Spring Boot applications. It does its best to get rid of repetitive boilerplate code and provides everything you need for secure user authentication in one ready-to-use package.

---

## Key **Features**

- **JWT Authentication** — Access and refresh tokens, securely persisted(only refresh tokens) in the database for proper invalidation on logout.
- **Google Login with FireBase** — Firebase Google Authentication in order to serve mobiles. Takes Google token, returns server's own token.
- **Email Verification** — On registration, during password reset etc. 
- **OTP-based Flows** — Utilizing server memory to hold new OTPs.
- **Rate Limiting** — Same vibe here... I'm using Bucket4j(Greedy Refill) plus the server's memory for this, please use Redis here.
- **Audit Logging** — Have implemented some light auditing that can be grown if needed in your project, or delete its folder to remove it. Also, this event driven just so its loosely coupled.
- **Soft Delete** — Implemented in codebase, mark users as deleted and delete all their tokens and passkeys.
- **Decoupled, Customizable Email Templates** — I've designed some ready-made HTML pages that serve as email themes with thymeleaf. Fully customizable to suit your project theme.
- **Flexible Token Handling** — Automatically supports both HTTP-only cookies and `Authorization: Bearer` tokens via a smart filter, but for this project tokens are issued to the client via JSON.
- **Async And Event Driven Email Sending** — Using Spring Boot's `@Async` annotation to keep API responses fast, you can always switch this to RabbitMQ as you scale horizontally.
- **Swagger Documented** — Project is well documented here.
- **In-Memory Caching** — Utilizing Caffeine to cache response of the fetchUser method to keep the server as stateless as possible, Make sure to use with Redis when server instance grows.
---

## Architecture & Design Decisions

### Package-by-Feature Architecture

I structured this project using the **feature packaging** style since that has been my preferred style after I discovered it. This improves:
* **Modularity** — Each feature contains all its related components.
* **Scalability** — Easy to add new features without touching existing ones.
* **Maintainability** — Related code lives together, reducing cognitive load.

### OAuth Evolution

- **Initial Implementation:** Google authentication using Spring Security OAuth Client, which was okay but kinda of not possible for mobile clients to use.
- **Current Implementation:** Firebase Google Authentication — chosen for a more streamlined and flexible OAuth flow with better cross-platform consistency.

### Asynchronous Processing

Uses Spring Boot's default `@Async` for background tasks like email sending — lightweight and simple, no need to run extra infrastructure like RabbitMQ for basic use. Since it's built with `@Async`, it can be easily upgraded to a full messaging system as your application grows.

### Temporal Storage

Uses **Caffeine (in-memory cache)** for short-lived, temporary application data:

* **Authenticated Users** — temporarily stores authenticated user/session-related data.
* **OTPs** — stores one-time passwords with automatic expiration.
* **Rate-Limiting Buckets** — maintains token buckets used to enforce request rate limits.

> **Note:** Since Caffeine is in-memory, cached data is lost when the application restarts and is not shared across multiple application instances. Simply fix by integrating Redis into th project.


## Configuration

For this, simply check the `example.env` file, fill them out then you can start the server