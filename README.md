# Reusable Spring Boot Authentication System

A complete, fully built, and thoroughly tested authentication module for Spring Boot applications. It doesits best to get rid of repetitive boilerplate code and provides everything you need for secure user authentication in one ready-to-use package.

---

## Key **Features**

- **JWT Authentication** — Access and refresh tokens, securely persisted in the database with proper invalidation on logout
- **Google Login with FireBase** — Initially implemented with Spring Security OAuth Client (Google), later migrated to Firebase Authentication so its easier to serve mobile clients as well
- **Email Verification** — On registration and during password reset
- **OTP-based Flows** — Using a cheap(In-memory instead of redis) and light snippet to handle this, it can be replaced or enhanced further. 
- **Rate Limiting** — Same vibe here.. I'm using Bucket4j plus the server's memory for this.., please use Redis here.
- **Audit Logging** — Have implemented some light auditing that can be grown if needed in your project, or delete its folder to remove it. Also this event driven just so its loosely coupled
- **Soft Delete** — Implemented in codebase
- **Decoupled, Customizable Email Templates** — Ive designed some ready made html pages that send emails (based on the event) with the help of thymeleaf
- **Flexible Token Handling** — Automatically supports both HTTP-only cookies and `Authorization: Bearer` tokens via a smart filter, but for this project tokens are issued to the client via json, simply tweak the methods that use the `createAuthResponse` or `createTokenResponse` private methods to send cookies rather.
- **Async And Event Driven Email Sending** — Using Spring Boot's `@Async` annotation to keep API responses fast, you can always switch this to RabbitMQ to ensure mails reach users
- **Swagger Documented** — Project is well documented here

---

## Architecture & Design Decisions

### Package-by-Feature Architecture

I structed this project using the **feature packaging** style since that has been my prefered style after i discovered it. This improves:
- **Modularity** — Each feature contains all its related components
- **Scalability** — Easy to add new features without touching existing ones
- **Maintainability** — Related code lives together, reducing cognitive load

### OAuth Evolution

- **Initial Implementation:** Google authentication using Spring Security OAuth Client, which was okay but kinda of not possible for mobile clients to use
- **Current Implementation:** Firebase Authentication — chosen for a more streamlined and flexible OAuth flow with better cross-platform consistency

### Asynchronous Processing

Uses Spring Boot's default `@Async` for background tasks like email sending — lightweight and simple, no need to run extra infrastructure like RabbitMQ for basic use. Since it's built with `@Async`, it can be easily upgraded to a full messaging system as your application grows.


## Configuration

For this, simply check the `example.env` file, fill them out then you can start the server