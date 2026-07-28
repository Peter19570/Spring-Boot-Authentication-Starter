# Project Structure

```
example/
└── authstarter
    ├── features
    │   ├── audit
    │   │   ├── controller
    │   │   │   └── AuditController.java
    │   │   ├── dto
    │   │   │   ├── AuditRequest.java
    │   │   │   └── AuditResponse.java
    │   │   ├── enums
    │   │   │   └── AuditAction.java
    │   │   ├── listeners
    │   │   │   └── AuditListener.java
    │   │   ├── mapper
    │   │   │   └── AuditMapper.java
    │   │   ├── model
    │   │   │   └── AuditLog.java
    │   │   ├── repo
    │   │   │   └── AuditRepo.java
    │   │   └── service
    │   │       └── AuditService.java
    │   ├── auth
    │   │   ├── config
    │   │   │   ├── async
    │   │   │   │   └── AsyncConfig.java
    │   │   │   ├── cache
    │   │   │   │   └── CacheConfig.java
    │   │   │   ├── cors
    │   │   │   │   └── CorsConfig.java
    │   │   │   ├── jwt
    │   │   │   │   ├── JwtFilter.java
    │   │   │   │   └── JwtService.java
    │   │   │   ├── oauth
    │   │   │   │   └── GoogleConfig.java
    │   │   │   ├── passkey
    │   │   │   │   ├── PasskeyConfig.java
    │   │   │   │   ├── PasskeyUserEntityConfig.java
    │   │   │   │   └── WebAuthnConfig.java
    │   │   │   ├── ratelimit
    │   │   │   │   └── RateLimitFilter.java
    │   │   │   ├── security
    │   │   │   │   └── SecurityConfig.java
    │   │   │   └── userservice
    │   │   │       └── CustomUserDetailsService.java
    │   │   ├── constants
    │   │   │   ├── CacheConstants.java
    │   │   │   ├── CorsConstants.java
    │   │   │   ├── RateLimitConstants.java
    │   │   │   └── SecurityConstants.java
    │   │   ├── controller
    │   │   │   └── AuthController.java
    │   │   ├── dto
    │   │   │   ├── request
    │   │   │   │   ├── AccountDeletionRequest.java
    │   │   │   │   ├── AuthRequest.java
    │   │   │   │   ├── EmailChangeRequest.java
    │   │   │   │   ├── ForgotPasswordRequest.java
    │   │   │   │   ├── GoogleRequest.java
    │   │   │   │   ├── PasskeyLoginRequest.java
    │   │   │   │   ├── PasskeyRegistrationRequest.java
    │   │   │   │   ├── RefreshTokenRequest.java
    │   │   │   │   └── ResetPasswordRequest.java
    │   │   │   └── response
    │   │   │       ├── AuthResponse.java
    │   │   │       ├── NameParts.java
    │   │   │       ├── PasskeyOptionsResponse.java
    │   │   │       └── TokenResponse.java
    │   │   ├── exceptions
    │   │   │   ├── AlreadyExistException.java
    │   │   │   ├── AuthenticationException.java
    │   │   │   ├── MessageException.java
    │   │   │   ├── NotFoundException.java
    │   │   │   └── ValidationException.java
    │   │   ├── listeners
    │   │   │   └── email
    │   │   │       └── VerificationEmail.java
    │   │   ├── mapper
    │   │   │   ├── AuthMapper.java
    │   │   │   └── PasskeyMapper.java
    │   │   ├── model
    │   │   │   ├── EmailVerificationToken.java
    │   │   │   ├── Passkey.java
    │   │   │   ├── PasswordResetToken.java
    │   │   │   └── RefreshToken.java
    │   │   ├── repo
    │   │   │   ├── EmailVerificationTokenRepo.java
    │   │   │   ├── PasskeyRepo.java
    │   │   │   ├── PasswordResetTokenRepo.java
    │   │   │   └── RefreshTokenRepo.java
    │   │   └── service
    │   │       ├── helpers
    │   │       │   └── AuthHelper.java
    │   │       ├── notification
    │   │       │   ├── EmailService.java
    │   │       │   └── OtpService.java
    │   │       └── AuthService.java
    │   ├── shared
    │   │   ├── config
    │   │   │   └── SwaggerConfig.java
    │   │   ├── dto
    │   │   │   ├── ApiResponse.java
    │   │   │   ├── CustomUserPrincipal.java
    │   │   │   └── PageResponse.java
    │   │   └── model
    │   │       └── BaseEntity.java
    │   └── user
    │       ├── controller
    │       │   └── UserController.java
    │       ├── dto
    │       │   └── response
    │       │       ├── UserDetailsResponse.java
    │       │       └── UserResponse.java
    │       ├── mapper
    │       │   └── UserMapper.java
    │       ├── model
    │       │   └── User.java
    │       ├── repo
    │       │   └── UserRepo.java
    │       └── service
    │           └── UserService.java
    ├── handler
    │   └── GlobalExceptionHandler.java
    └── AuthStarterApplication.java
```
