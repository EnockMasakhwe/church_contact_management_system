# Church Contact

Church Contact is a web application that helps church members reach elders and church administrators without first needing to make an in-person visit. Members can send prayer requests, bereavement notices, and general messages online, or request a visitation at a preferred time. Church administrators can review and manage submissions and appointments from a protected dashboard.

The application provides a digital channel for initiating contact and coordinating visits; it does not replace urgent in-person care or provide a real-time chat service.

## Features

- Member registration, email verification, sign-in, and password reset.
- JWT-based authentication with member, administrator, and super-administrator roles.
- Message submission with four categories: prayer request, visitation request, bereavement, and general.
- A seven-day, daytime appointment calendar for selecting a requested visitation time.
- Member dashboard for viewing their own messages and appointment requests.
- Administrator dashboard for viewing, filtering, and managing messages, appointments, users, and summary statistics.
- Email notifications for account verification, password resets, new submissions to administrators, and appointment status changes to members.
- Public availability calendar that exposes appointment time and status, but not the member's identity or message.

## Technology

| Area | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5 |
| API and web server | Spring MVC |
| Persistence | Spring Data JPA, Hibernate, MySQL |
| Authentication and authorization | Spring Security, JWT (JJWT), BCrypt |
| Validation | Jakarta Bean Validation |
| Email | Spring Boot Mail and SMTP |
| Frontend | Static HTML, CSS, and JavaScript served by Spring Boot |
| Build and tests | Maven, Spring Boot Test, JUnit |

## How it works

1. A member registers with a username, email address, and password. Registration sends a verification link; the account must be verified before sign-in.
2. A signed-in member submits a message. The message is associated with their account and appears in their dashboard.
3. For a visitation request, the member also provides a location and selects a preferred time. The server checks for an overlapping appointment and creates a pending appointment.
4. Administrators review submissions in the admin dashboard, update message or appointment statuses, and manage accounts. Super-administrators can also change account roles.
5. The system emails administrators about new submissions. Members receive email when an appointment status changes. Message status changes are visible in the member dashboard but do not send a separate email.

Appointment availability is presented in the browser for the next seven days, from 08:00 through 18:00. The backend validates that requested times are in the future and checks for conflicts when a request is submitted. A displayed slot is not a final booking: the appointment remains pending until an administrator approves or rejects it. The calendar marks pending and approved appointments as unavailable; the current backend conflict check considers all existing appointments, including rejected and completed ones.

## Requirements

- Java 21 or later.
- MySQL 8.x or a compatible MySQL server.
- Maven 3.9+ (or use the included Maven Wrapper).
- An SMTP account for verification, password reset, and notification emails.

## Local setup

### 1. Create a database

Create an empty MySQL database. For example:

```sql
CREATE DATABASE church_contact
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 2. Configure the application

The application reads the following settings from environment variables. Set them in your shell or IDE run configuration before starting it; the project does not load a local `.env` file automatically.

| Variable | Required | Description |
| --- | --- | --- |
| `DB_URL` | Yes | JDBC URL, for example `jdbc:mysql://localhost:3306/church_contact` |
| `DB_USERNAME` | Yes | MySQL account with access to the database |
| `DB_PASSWORD` | Yes | Password for that MySQL account |
| `MAIL_USERNAME` | Yes | SMTP username and sender address |
| `MAIL_PASSWORD` | Yes | SMTP password or provider-issued app password |
| `JWT_SECRET` | Yes | Base64-encoded signing key; use at least 32 random bytes for HS256 |
| `JWT_EXPIRATION` | Yes | JWT lifetime in milliseconds; for example `86400000` for 24 hours |
| `APP_BASE_URL` | No | Public base URL used in emailed links; defaults to `http://localhost:8080` |
| `SUPER_ADMIN_EMAIL` | No | Email address granted the super-administrator role at login; the source configuration currently provides a default, so set this explicitly for your deployment |

Example setup in a Bash shell (replace database and email values with your own):

```bash
export DB_URL='jdbc:mysql://localhost:3306/church_contact'
export DB_USERNAME='church_contact_app'
export DB_PASSWORD='replace-with-a-database-password'
export MAIL_USERNAME='your-smtp-account@example.com'
export MAIL_PASSWORD='replace-with-an-smtp-app-password'
export JWT_SECRET="$(openssl rand -base64 32)"
export JWT_EXPIRATION='86400000'
export APP_BASE_URL='http://localhost:8080'
export SUPER_ADMIN_EMAIL='trusted-admin@example.com'
```

Keep credentials out of source control and use a different, securely generated JWT secret in each environment. The configured super-administrator email must belong to a verified account that can sign in.

### 3. Run the application

From the project root:

```bash
./mvnw spring-boot:run
```

The server listens on port `8080`. Open `http://localhost:8080/login.html` to sign in or use the registration link. Other pages are served at `/register.html`, `/reset-password.html`, `/user.html`, `/message.html`, and `/admin.html`.

On first startup, Hibernate creates or updates tables using the configured MySQL database. This is convenient for local development; review the production configuration guidance before deploying.

### 4. Build and run tests

```bash
./mvnw test
```

Build the executable Spring Boot archive with:

```bash
./mvnw package
```

The packaged application is written to `target/`. Run it with `java -jar target/contact_form-0.0.1-SNAPSHOT.jar` after configuring the same environment variables.

## User roles

| Role | Capabilities |
| --- | --- |
| `ROLE_USER` | Submit messages and visitation requests; view their own messages and appointments |
| `ROLE_ADMIN` | View and manage messages and appointments; view and delete user accounts |
| `ROLE_SUPER_ADMIN` | Administrator capabilities, plus change user roles |

Newly registered accounts receive `ROLE_USER`. The configured `SUPER_ADMIN_EMAIL` is treated as the super-administrator during login. Role checks are enforced by Spring Security on the API, not only by hiding dashboard controls.

## API reference

All endpoints are served from the same host as the frontend. Protected endpoints require the JWT to be sent using the HTTP Bearer authentication scheme. API request and response bodies use JSON unless noted otherwise.

### Authentication

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Register an account |
| `GET` | `/api/auth/verify?token=...` | Public | Verify an account using the emailed token |
| `POST` | `/api/auth/login` | Public | Sign in and receive a JWT, username, and role |
| `POST` | `/api/auth/forgot-password` | Public | Email a password-reset link |
| `POST` | `/api/auth/reset-password` | Public | Set a new password using a reset token |

Registration and login accept `email` and `password`; registration also accepts `username`. Password reset accepts `token` and `newPassword`. Verification tokens expire after 24 hours and password-reset tokens after 30 minutes.

### Member and public endpoints

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/messages` | Signed-in member, admin, or super-admin | Submit a message or visitation request |
| `GET` | `/api/user/messages` | Signed-in account | List the signed-in member's messages |
| `GET` | `/api/user/appointments` | Signed-in account | List the signed-in member's appointments |
| `GET` | `/api/appointments/public` | Public | List active appointment times and statuses without member details |

Example message body:

```json
{
  "phone": "+254712345678",
  "type": "PRAYER_REQUEST",
  "message": "Please keep my family in your prayers.",
  "location": null,
  "preferredDateTime": null
}
```

Supported `type` values are `PRAYER_REQUEST`, `VISITATION_REQUEST`, `BEREAVEMENT`, and `GENERAL`. Phone numbers must use the Kenyan format `+254` followed by nine digits or start with `0` and contain nine further digits. The message is required and limited to 1,000 characters. For `VISITATION_REQUEST`, provide a `location` and a future `preferredDateTime` formatted as `yyyy-MM-dd'T'HH:mm:ss`; those fields are optional for other types.

### Administrator endpoints

All `/api/admin/**` endpoints require an administrator or super-administrator, except changing user roles, which requires a super-administrator.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/admin/messages` | List all messages |
| `PUT` | `/api/admin/messages/{id}/status` | Set a message status; JSON body: `{"status":"READ"}` |
| `DELETE` | `/api/admin/messages/{id}` | Delete a message |
| `GET` | `/api/admin/appointments` | List appointments with member details |
| `PUT` | `/api/admin/appointments/{id}/status?status=APPROVED` | Set an appointment status |
| `GET` | `/api/admin/users` | List registered accounts |
| `DELETE` | `/api/admin/users/{id}` | Delete an account and its associated messages and appointments |
| `PUT` | `/api/admin/users/{id}/role` | Change a user's role; super-admin only; JSON body: `{"role":"ROLE_ADMIN"}` |
| `GET` | `/api/admin/stats` | Get message and appointment counts |

Message statuses are `NEW`, `READ`, `IN_PROGRESS`, `RESPONDED`, and `CLOSED`. Appointment statuses are `PENDING`, `APPROVED`, `REJECTED`, and `COMPLETED`. Updating an appointment also updates its associated message status for approval, rejection, and completion.

## Project layout

```text
src/
├── main/
│   ├── java/com/eliarojr/contact_form/
│   │   ├── config/       # Security, CORS, and application configuration
│   │   ├── controller/   # HTTP API endpoints
│   │   ├── dto/          # API request and response models
│   │   ├── entity/       # JPA entities and status/role enums
│   │   ├── exception/    # Application exceptions and API error handling
│   │   ├── repository/   # Spring Data persistence interfaces
│   │   ├── security/     # JWT authentication support
│   │   └── service/      # Application and email services
│   └── resources/
│       ├── application.yaml
│       └── static/       # HTML, CSS, and JavaScript frontend
└── test/                 # Spring Boot tests
```

## Configuration and deployment notes

- The current configuration enables Hibernate schema updates (`ddl-auto: update`) and SQL logging (`show-sql: true`). Use managed database migrations and disable SQL output for production.
- Configure `APP_BASE_URL` to the externally reachable HTTPS URL before sending verification or password-reset emails. Ensure SMTP credentials and TLS settings are appropriate for the email provider.
- The current CORS configuration allows `http://localhost:8080`. If the frontend is served from a different origin, explicitly configure only the trusted origin(s) required by that deployment.
- Use HTTPS, protect the database and email credentials, regularly back up the database, and restrict administrator accounts to trusted church staff. Message content, contact details, and visitation locations can be sensitive pastoral information.
- The application stores the JWT in browser local storage. Use trusted devices, avoid shared/public computers, and sign out after use.
- The public appointment endpoint exposes scheduled times and statuses, though it omits member names, email addresses, and message content.
- Do not use this service as an emergency response channel; members needing urgent help should contact local emergency services or a trusted person directly.

## License

No license is currently specified in this repository. Add a license file and update this section before distributing the project if an open-source license is intended.
