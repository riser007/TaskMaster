# TaskMaster - Task Tracking & Management API

Backend system for a collaborative task tracking application built with Java, Spring Boot, and PostgreSQL.

## Features

*   User Authentication (Register, Login) & Management (Profile) using JWT.
*   Project/Team Creation and Membership Management.
*   Task Management (CRUD operations).
*   Task Assignment within Projects.
*   Filtering, Sorting, and Searching Tasks.
*   Commenting on Tasks.
*   File Attachments for Tasks.
*   RESTful API Endpoints.
*   Role-based Access Control (Basic).

## Technology Stack

*   **Language:** Java 17+
*   **Framework:** Spring Boot 3.1.x
*   **Build Tool:** Gradle
*   **Database:** PostgreSQL (configurable)
*   **Data Access:** Spring Data JPA / Hibernate
*   **Security:** Spring Security, JWT
*   **API:** Spring Web (REST Controllers)
*   **Validation:** Spring Validation
*   **Testing:** JUnit 5, Mockito, Spring Boot Test

## Prerequisites

*   JDK 17 or later
*   Gradle 7.x or later
*   PostgreSQL Server (or other configured database) running
*   A tool to make API requests (e.g., Postman, curl)

## Setup and Installation

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd taskmaster-backend
    ```

2.  **Configure Database:**
    *   Create a database (e.g., `taskmaster_db` in PostgreSQL).
    *   Open `src/main/resources/application.properties`.
    *   Update the following properties with your database credentials:
        ```properties
        spring.datasource.url=jdbc:postgresql://localhost:5432/taskmaster_db
        spring.datasource.username=your_db_user
        spring.datasource.password=your_db_password
        ```

3.  **Configure JWT Secret:**
    *   In `application.properties`, **change the default `app.jwt.secret` value** to a strong, unique secret key. It is highly recommended to use environment variables or a configuration server for secrets in production.
      ```properties
      app.jwt.secret=YourSuperSecretKeyWhichShouldBeLongAndSecureAndStoredSafely
      ```

4.  **Configure File Upload Directory:**
    *   The default upload directory is `./uploads` relative to where the application runs. You can change this:
      ```properties
      file.upload-dir=/path/to/your/desired/upload/directory
      ```
    *Ensure the application has write permissions to this directory.*

5.  **Build the project:**
    ```bash
    ./gradlew build
    ```

6.  **Run the application:**
    ```bash
    ./gradlew bootRun
    ```
    Alternatively, you can run the JAR file created in `build/libs`:
    ```bash
    java -jar build/libs/taskmaster-0.0.1-SNAPSHOT.jar
    ```

The application will start on `http://localhost:8080` (or the configured port).

## API Endpoints

The base URL for all API endpoints is `/api`. Authentication is required for most endpoints (provide JWT via `Authorization: Bearer <token>` header).

*(List major endpoints here, similar to the outline in step 4 of the thought process. Include request/response examples or link to OpenAPI/Swagger documentation if generated)*

*   **Authentication:**
    *   `POST /auth/register`: Register a new user.
    *   `POST /auth/login`: Login and receive JWT.
*   **Users:**
    *   `GET /users/me`: Get current user's profile.
    *   `PUT /users/me`: Update current user's profile.
*   **Projects:**
    *   `POST /projects`: Create a new project.
    *   `GET /projects`: List projects the user is a member of.
    *   `GET /projects/{projectId}`: Get project details.
    *   `PUT /projects/{projectId}`: Update project details (owner only?).
    *   `DELETE /projects/{projectId}`: Delete project (owner only?).
    *   `POST /projects/{projectId}/members`: Add a member to the project.
    *   `DELETE /projects/{projectId}/members/{userId}`: Remove a member.
*   **Tasks:**
    *   `POST /projects/{projectId}/tasks`: Create a task within a project.
    *   `GET /users/me/tasks`: List tasks assigned to the current user.
    *   `GET /projects/{projectId}/tasks`: List tasks in a project (with filtering/sorting/searching query params: `status`, `search`, `sortBy`, `sortDir`, `page`, `size`).
    *   `GET /projects/{projectId}/tasks/{taskId}`: Get task details.
    *   `PUT /projects/{projectId}/tasks/{taskId}`: Update a task.
    *   `DELETE /projects/{projectId}/tasks/{taskId}`: Delete a task.
*   **Comments:**
    *   `POST /tasks/{taskId}/comments`: Add a comment to a task.
    *   `GET /tasks/{taskId}/comments`: List comments for a task.
    *   `DELETE /comments/{commentId}`: Delete a comment (author or project owner?).
*   **Attachments:**
    *   `POST /tasks/{taskId}/attachments`: Upload an attachment (multipart/form-data, parameter name 'file').
    *   `GET /tasks/{taskId}/attachments`: List attachments for a task.
    *   `GET /attachments/{attachmentId}/download`: Download an attachment file.
    *   `DELETE /attachments/{attachmentId}`: Delete an attachment.

## Code Quality & Best Practices

*   **Code Style:** Follows standard Java conventions.
*   **Organization:** Code is organized into layers (controller, service, repository, model, dto).
*   **Error Handling:** Centralized exception handling using `@ControllerAdvice`.
*   **Security:** Secure password hashing (BCrypt), JWT authentication, input validation.
*   **Maintainability:** Uses DTOs, Service layer for business logic, interfaces for services.

## Future Enhancements (Optional)

*   Real-time notifications via WebSockets.
*   Integration with Generative AI for task description generation.
*   More granular roles and permissions within projects.
*   Audit logging.
*   Pagination for all list endpoints.
*   Unit and Integration Tests.
*   Cloud storage for attachments (AWS S3, etc.).
*   Containerization using Docker.

