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
    │   │   │   ├── cors
    │   │   │   │   └── CorsConfig.java
    │   │   │   ├── jwt
    │   │   │   │   ├── JwtFilter.java
    │   │   │   │   └── JwtService.java
    │   │   │   ├── oauth
    │   │   │   │   └── GoogleConfig.java
    │   │   │   ├── passkey
    │   │   │   │   ├── PasskeyConfig.java
    │   │   │   │   ├── PasskeyUserConfig.java
    │   │   │   │   └── WebAuthnConfig.java
    │   │   │   ├── ratelimit
    │   │   │   │   └── RateLimitFilter.java
    │   │   │   ├── security
    │   │   │   │   └── SecurityConfig.java
    │   │   │   └── userservice
    │   │   │       └── CustomUserDetailsService.java
    │   │   ├── constants
    │   │   │   ├── AsyncConstants.java
    │   │   │   ├── CorsConstants.java
    │   │   │   ├── JwtConstants.java
    │   │   │   ├── RateLimitConstants.java
    │   │   │   └── SecurityConstants.java
    │   │   ├── controller
    │   │   │   └── AuthController.java
    │   │   ├── dto
    │   │   │   ├── internal
    │   │   │   │   ├── JwtClaims.java
    │   │   │   │   ├── NameParts.java
    │   │   │   │   └── Verification.java
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
    │   │   │       ├── PasskeyOptionsResponse.java
    │   │   │       ├── PasskeyResponse.java
    │   │   │       └── TokenResponse.java
    │   │   ├── exceptions
    │   │   │   ├── AlreadyExistException.java
    │   │   │   ├── AuthenticationException.java
    │   │   │   ├── MessageException.java
    │   │   │   ├── NotFoundException.java
    │   │   │   └── ValidationException.java
    │   │   ├── mapper
    │   │   │   ├── AuthMapper.java
    │   │   │   └── PasskeyMapper.java
    │   │   ├── model
    │   │   │   ├── Passkey.java
    │   │   │   └── RefreshToken.java
    │   │   ├── repo
    │   │   │   ├── PasskeyRepo.java
    │   │   │   └── RefreshTokenRepo.java
    │   │   └── service
    │   │       ├── helpers
    │   │       │   └── AuthHelper.java
    │   │       ├── memory
    │   │       │   ├── EVTService.java
    │   │       │   ├── OTPService.java
    │   │       │   └── PRTService.java
    │   │       ├── notification
    │   │       │   └── EmailService.java
    │   │       └── AuthService.java
    │   ├── shared
    │   │   ├── config
    │   │   │   ├── app
    │   │   │   │   └── AppConfig.java
    │   │   │   ├── cache
    │   │   │   │   └── CacheConfig.java
    │   │   │   └── swagger
    │   │   │       └── SwaggerConfig.java
    │   │   ├── constants
    │   │   │   ├── CacheConstants.java
    │   │   │   ├── ClientConstants.java
    │   │   │   └── PageConstants.java
    │   │   ├── dto
    │   │   │   ├── ApiResponse.java
    │   │   │   ├── ClientInfo.java
    │   │   │   ├── PageResponse.java
    │   │   │   └── UserPrincipal.java
    │   │   ├── exceptions
    │   │   │   └── AppException.java
    │   │   ├── model
    │   │   │   └── BaseEntity.java
    │   │   └── service
    │   │       └── ClientService.java
    │   └── user
    │       ├── controller
    │       │   └── UserController.java
    │       ├── dto
    │       │   └── response
    │       │       ├── UserDetailedResponse.java
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
