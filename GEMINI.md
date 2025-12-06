# Gemini Code Assistant Context: Task Manager

This document provides a comprehensive overview of the Task Manager project for the Gemini Code Assistant, enabling it to understand the project's purpose, architecture, and development conventions.

## 1. Project Overview

**Task Manager** is a desktop application built with **JavaFX** designed to help users manage their tasks in a queue-based, "one-at-a-time" manner. The core concept is inspired by operating system thread scheduling, aiming to reduce decision fatigue by presenting only the current task from a round-robin queue.

### Core Technologies:
- **Language**: Java 21
- **Framework**: OpenJFX (JavaFX) for the GUI
- **Build Tool**: Apache Maven
- **Database**: SQLite
- **Data Mapper**: MyBatis for interacting with the SQLite database
- **DB Migration**: Flyway for managing database schema evolution
- **Utilities**: Lombok to reduce boilerplate code

### Architecture:
The application follows a simple MVC-like pattern typical for JavaFX:
- **`App.java`**: The main application entry point that sets up the stage and scene.
- **`main.fxml`**: The FXML file that defines the UI layout and components.
- **`MainController.java`**: The controller that contains all the UI logic and orchestrates actions.
- **Service Layer (`TaskService`, `WorkSessionService`)**: Handles business logic for managing tasks and work sessions.
- **Data Layer (`TaskMapper`, `WorkSessionMapper`)**: MyBatis mappers that execute SQL queries against the SQLite database.
- **Database (`task-manager.db`)**: A local SQLite database file. The schema is managed by Flyway migration scripts located in `src/main/resources/db/migration`.

## 2. Building and Running

The project is managed by Maven and uses the Maven Wrapper (`mvnw`).

### Key Commands:
- **Run the application**:
  ```sh
  ./mvnw javafx:run
  ```
- **Run tests**:
  ```sh
  ./mvnw test
  ```
- **Build the project** (compiles and packages into a JAR):
  ```sh
  ./mvnw package
  ```
- **Create a native application image** (using jpackage):
  After running `mvn package`, a platform-specific application image is created in the `target/dist` directory.

## 3. Development Conventions

### Code Style:
- The codebase follows standard Java conventions.
- Lombok is used for models (`@Data`, `@Builder`) to avoid manual getters, setters, and constructors.

### Database:
- Database schema changes **must** be managed via Flyway migration scripts.
- To add a new schema change, create a new SQL file in `src/main/resources/db/migration/` with a versioned name (e.g., `V6__Your_change_description.sql`).
- All timestamps are stored as TEXT in ISO-8601 format (UTC) and handled in Java using `java.time.Instant`.

### Core Logic:
- The application enforces a **single active work session** across all tasks at any given time, enforced by a database trigger.
- The task queue is managed via a `queue_order` integer column. Rotating a task involves moving it to the end of the queue by assigning it the highest `queue_order`.

### UI and State:
- The UI is defined declaratively in `src/main/resources/fxml/main.fxml`.
- All UI logic is contained within `MainController.java`.
- Application state (like the current task and work session) is managed within the `MainController`.
- On application close, the `onApplicationClose()` method in the controller is called to ensure any active work session is saved.

## 4. AI Assistant Guidelines

This section contains guidelines for the AI assistant when working on this project.

### General Workflow:
- **Verify Changes**: After making changes, always test that the application builds and runs successfully using the commands in the "Building and Running" section.
- **Git Commits**: Only commit changes when explicitly requested by the user.
- **Commit Messages**: Do not include any AI attribution footers (e.g., "Generated with...") in commit messages.

### Testing:
- When writing unit tests, **do not** use `Thread.sleep()`. This makes tests unstable and slow.