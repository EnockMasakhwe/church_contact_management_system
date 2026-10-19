# contact_form

## Environment configuration

All application credentials and settings below are required. Add them to the
environment used to start Spring Boot (for example, IntelliJ's Run Configuration
environment variables or the terminal session that runs `./mvnw spring-boot:run`).
Do not put real values in this file or commit them.

| Variable | Local value / how to obtain it |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/contact_form` |
| `DB_USERNAME` | Your local MySQL account name (for example, `root`) |
| `DB_PASSWORD` | The password for that local MySQL account; rotate it if the previous value was exposed |
| `MAIL_USERNAME` | The Gmail account used to send verification and password-reset messages |
| `MAIL_PASSWORD` | A Gmail App Password; revoke the previously exposed one and generate a new one in Google Account → Security → 2-Step Verification → App passwords |
| `JWT_SECRET` | A newly generated Base64 key; run `openssl rand -base64 32` and retain the output privately |
| `JWT_EXPIRATION` | `86400000` (milliseconds; 24 hours, preserving the previous lifetime) |

To rotate a local MySQL password, use an authorized MySQL administrator to
change the password for the account configured as `DB_USERNAME`, then set
`DB_PASSWORD` to the new value. Update any other local tools that use that
account. Never reuse the exposed password.

For Gmail, revoke the exposed App Password in Google Account security, then
create a replacement App Password and use it for `MAIL_PASSWORD` (not your
regular Gmail password). If App Passwords is unavailable, configure another
SMTP provider and update the mail host/port settings accordingly.

Keep the same `JWT_SECRET` between app restarts; changing it invalidates all
existing login tokens. A newly generated secret will require users to log in
again. The frontend calls the API on its own origin, so it works both on
localhost and when deployed without changing API URLs.
