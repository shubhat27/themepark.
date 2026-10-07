# Adventure World

Adventure World is a Spring Boot theme park and resort site. Guests can explore a database-backed ride catalog, check live ticket pricing, reserve a park date, and find or cancel reservations. Operator access is protected by Spring Security and the operations dashboard reads saved bookings from the database.

Reservations are pay-at-park. Online payment processing and outbound email are not configured.

## Run locally

Requirements: Java 25, MySQL 8, and a MySQL database named `themepark`.

Create the database once:

```sql
CREATE DATABASE themepark CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Set your local database and operator credentials in PowerShell. The fallback values in `application.properties` are for development only.

```powershell
$env:PARK_DB_URL = "jdbc:mysql://localhost:3306/themepark"
$env:PARK_DB_USERNAME = "root"
$env:PARK_DB_PASSWORD = "your-local-mysql-password"
$env:PARK_ADMIN_USERNAME = "parkadmin"
$env:PARK_ADMIN_PASSWORD = "choose-a-strong-password"
```

Run the tests and start the site:

```powershell
./mvnw test
./mvnw spring-boot:run
```

Open `http://localhost:8081`. Hibernate creates or updates the development schema and inserts the starter experience catalog on first startup. Tests use an isolated H2 in-memory database and do not require MySQL.

## Guest bookings

Choose a visit date, pass, group size, and optional fast passes. Prices and the group discount are calculated by the server. A reservation is saved in MySQL and returns an `AW-XXXXXXXX` reference. Guests can retrieve it with the same email address and cancel it before the visit date. Guest accounts are not required.

## Operator access

Sign in at `/login` with `PARK_ADMIN_USERNAME` and `PARK_ADMIN_PASSWORD`. The `/admin` dashboard and `/api/admin/bookings` endpoint require the operator role. Sign out from the dashboard. Set strong environment credentials before deployment; do not use development fallbacks in production.

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/experiences` | Read the ride and zone catalog |
| `GET` | `/api/ticket-options` | Read available passes and rates |
| `POST` | `/api/booking-quotes` | Calculate a server-side estimate |
| `POST` | `/api/bookings` | Create a reservation |
| `POST` | `/api/bookings/lookup` | Find a reservation by reference and email |
| `POST` | `/api/bookings/{reference}/cancel` | Cancel an upcoming reservation |
| `GET` | `/api/admin/bookings` | List reservations (operator login required) |

Write requests use Spring Security CSRF protection. The rendered pages expose the token as `_csrf` and its header name as `_csrf_header`.