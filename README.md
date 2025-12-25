# Banking Application — Software Testing Project

## Project Structure

```
banking-app/
├── pom.xml                                    # Maven configuration file
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── banking/
│   │   │           └── app/
│   │   │               ├── BankingApplication.java      # Main application class
│   │   │               └── controller/
│   │   │                   └── HomeController.java      # Sample controller
│   │   └── resources/
│   │       ├── application.properties         # Application configuration
│   │       ├── static/                        # Static files (CSS, JS, images)
│   │       │   ├── css/
│   │       │   │   └── style.css             # CSS files go here
│   │       │   ├── js/
│   │       │   │   └── main.js               # JavaScript files go here
│   │       │   └── images/                   # Image files go here
│   │       └── templates/                     # Thymeleaf HTML templates
│   │           └── index.html                # Sample template
│   └── test/
│       └── java/                             # Test files go here
└── README.md                                  # This file
```

## Where to Put Your Files

### Static Files (HTML/CSS/JS/Images)
All static files should be placed in `src/main/resources/static/`:

- **CSS files**: `src/main/resources/static/css/`
- **JavaScript files**: `src/main/resources/static/js/`
- **Images**: `src/main/resources/static/images/`
- **Other static files** (fonts, etc.): `src/main/resources/static/`

### Thymeleaf Templates
Dynamic HTML files (Thymeleaf templates) should be placed in:
- `src/main/resources/templates/`

**Important Difference:**
- **Static HTML**: If you have pure HTML files that don't need server-side processing, put them in `src/main/resources/static/`
- **Thymeleaf Templates**: If you need dynamic content (data from controllers), use Thymeleaf templates in `src/main/resources/templates/`

### Accessing Static Files in Templates
In your Thymeleaf templates, reference static files using the `@{...}` syntax:

```html
<!-- CSS -->
<link rel="stylesheet" th:href="@{/css/style.css}">

<!-- JavaScript -->
<script th:src="@{/js/main.js}"></script>

<!-- Images -->
<img th:src="@{/images/logo.png}" alt="Logo">
```

## Dependencies Included

The `pom.xml` includes:
- **Spring Boot Web**: For building web applications and REST APIs
- **Thymeleaf**: Template engine for server-side HTML rendering
- **Spring Boot DevTools**: For automatic restart during development (optional)
- **Spring Boot Test**: For testing your application

## Running the Application

1. Make sure you have Java 17+ and Maven installed
2. Navigate to the project directory
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Open your browser and go to: `http://localhost:8080`

## Building the Application

To create a JAR file:
```bash
mvn clean package
```

The JAR file will be created in the `target/` directory.

## Next Steps

1. Add your controllers in `src/main/java/com/banking/app/controller/`
2. Create your service layer in `src/main/java/com/banking/app/service/`
3. Add your models/entities in `src/main/java/com/banking/app/model/`
4. Create Thymeleaf templates in `src/main/resources/templates/`
5. Add your CSS/JS files in `src/main/resources/static/`

## Common Additions for Banking Applications

You might want to add these dependencies later:
- **Spring Data JPA**: For database access
- **H2/MySQL/PostgreSQL**: Database drivers
- **Spring Security**: For authentication and authorization
- **Validation**: For form validation
- **Lombok**: To reduce boilerplate code
