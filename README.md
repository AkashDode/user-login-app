# User Login Application

A simple three-page user authentication app: **Login → Register → Welcome**, built with Spring Boot, Thymeleaf, Bootstrap, and MySQL (AWS RDS).

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.3.4 |
| Frontend | HTML, CSS, Bootstrap 5.3 (via CDN), Thymeleaf |
| Database | MySQL 8.x (AWS RDS) |
| Build Tool | Maven (wrapper included) |
| Server | Tomcat (embedded in Spring Boot) |

## Pages

| Page | URL | Contents |
|---|---|---|
| **Login** | `/login` | Username, Password, Login button |
| **Register** | `/register` | Username, Email, Password, Register button |
| **Welcome** | `/welcome` | "Welcome {username}", Logout button |

Route behavior:
- `/` redirects to `/login` (or straight to `/welcome` if already signed in)
- `/welcome` redirects back to `/login` if there's no active session
- `/logout` invalidates the session and returns to `/login`

## Database

One table, exactly as specified:

```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    email VARCHAR(100),
    password VARCHAR(100)
);
```

The full script (with `UNIQUE` constraints on username/email) is in [`sql/schema.sql`](./sql/schema.sql). You can run it manually, **or** just start the app — `spring.jpa.hibernate.ddl-auto=update` creates the table automatically on first run.

### A note on the password column

Passwords are stored as **BCrypt hashes**, not plain text. A BCrypt hash is 60 characters, so it fits `VARCHAR(100)` comfortably.

This was a deliberate deviation from a literal reading of the spec: storing plain-text passwords means anyone with read access to the database (a leaked backup, a SQL injection, a curious colleague) instantly owns every user account, and since people reuse passwords, their other accounts too. The hashing is three lines of code (`BCryptPasswordEncoder`), so there's no real cost to doing it correctly.

## Project Structure

```
user-login-app/
│
├── src/
│   ├── main/
│   │   ├── java/com/loginapp/
│   │   │   ├── UserLoginApplication.java      # entry point
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java         # all 3 pages + logout
│   │   │   ├── service/
│   │   │   │   └── UserService.java            # register + authenticate
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java         # Spring Data JPA
│   │   │   ├── entity/
│   │   │   │   └── User.java                   # maps to `users` table
│   │   │   ├── dto/
│   │   │   │   ├── LoginForm.java              # form binding + validation
│   │   │   │   └── RegisterForm.java
│   │   │   └── config/
│   │   │       └── AppConfig.java              # BCryptPasswordEncoder bean
│   │   │
│   │   └── resources/
│   │       ├── static/css/styles.css           # custom styling over Bootstrap
│   │       ├── templates/
│   │       │   ├── login.html                  # Page 1
│   │       │   ├── register.html               # Page 2
│   │       │   └── welcome.html                # Page 3
│   │       └── application.properties
│   │
│   └── test/
│       ├── java/com/loginapp/
│       │   ├── service/UserServiceTest.java        # Mockito unit tests
│       │   └── controller/AuthControllerTest.java  # MockMvc web-layer tests
│       └── resources/application.properties        # points tests at H2
│
├── sql/schema.sql
├── pom.xml
├── mvnw / mvnw.cmd
├── .gitignore
└── README.md
```

## Running Locally

**1. Start MySQL** (local or AWS RDS) and note the connection details.

**2. Run the app:**
```bash
./mvnw spring-boot:run
```
(Windows: `mvnw.cmd spring-boot:run`)

**3. Open** http://localhost:8080 — you'll land on the login page.

**4. Register an account**, then sign in with it.

## Connecting to AWS RDS

All database settings read from environment variables, so nothing needs to be hardcoded:

```bash
export DB_HOST=your-instance.abcd1234.us-east-1.rds.amazonaws.com
export DB_PORT=3306
export DB_NAME=loginapp_db
export DB_USERNAME=admin
export DB_PASSWORD=your-rds-password

./mvnw spring-boot:run
```

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST` | `localhost` | RDS endpoint |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `loginapp_db` | Database name |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | `root` | Database password |
| `SERVER_PORT` | `8080` | Tomcat port |
| `THYMELEAF_CACHE` | `false` | Set `true` in production |

**RDS checklist:** make sure the instance's security group allows inbound TCP on 3306 from wherever the app runs, and create the `loginapp_db` database (or run `sql/schema.sql`) before first launch.

## Running Tests

```bash
./mvnw test
```

Tests use an in-memory H2 database, so they run without MySQL or RDS being available.

Coverage:
- `UserServiceTest` — verifies the raw password is never saved (only the hash), and that authentication correctly rejects wrong passwords and unknown usernames
- `AuthControllerTest` — MockMvc tests for all three pages: successful/failed login, duplicate username and invalid email at registration, blank-field validation, and session guarding on `/welcome` and `/logout`

## Building a Deployable JAR

```bash
./mvnw clean package
java -jar target/user-login-app.jar
```

## Possible Next Steps

This is intentionally a minimal app. If you want to take it further:
- Swap the session-attribute approach for full **Spring Security** (gives you CSRF protection, "remember me", role-based access, and password-reset flows out of the box)
- Add a "forgot password" email flow
- Add rate limiting on the login endpoint to slow down brute-force attempts
