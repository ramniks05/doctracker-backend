# DocuTrack Backend (Spring Boot)

Production-ready backend starter for **DocuTrack** (Document & Warranty Manager).

## Tech

- Spring Boot
- PostgreSQL
- Spring Data JPA
- DTO pattern + ModelMapper
- Local file uploads (`multipart/form-data`)

## Quick start

1. Start PostgreSQL

```bash
docker compose up -d
```

2. Run the app

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

## Swagger

- UI: `http://localhost:8080/swagger-ui/index.html` (and `/swagger-ui.html` redirects to it)
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## OTP auth

- Send OTP: `POST /api/auth/send-otp`
- Resend OTP: `POST /api/auth/resend-otp`
- Verify OTP: `POST /api/auth/verify-otp`
- Update profile: `PUT /api/auth/profile/{userId}`

## Auth (production)

After `verify-otp`, use the returned JWT:

- Header: `Authorization: Bearer <accessToken>`

## File uploads

- Upload: `POST /api/upload` (multipart key: `file`)
- Files served from: `GET /files/{filename}`

