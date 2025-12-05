# Task Manager - Project Memory

## User Preferences

### Technology Stack
- **Build Tool**: Maven (always use `mvn` to create projects)
- **Version Control**: Git
- **Programming Language**: Java 21
- **UI Framework**: JavaFX with FXML
- **Database**: SQLite
- **Persistence**: MyBatis (for non-trivial SQL queries)
- **Migration Tool**: Flyway
- **Utilities**: Lombok

### Development Guidelines
1. **Dependency Versions**: ALWAYS check Maven Central for latest stable versions before adding any library (no beta, alpha, RC, GA versions). Never assume or use outdated versions.
   - Check versions by calling: `https://repo1.maven.org/maven2/{groupId}/{artifactId}/maven-metadata.xml`
   - Example: `https://repo1.maven.org/maven2/org/mybatis/mybatis/maven-metadata.xml`
   - Use WebFetch tool to fetch the metadata and extract the latest version
2. **Maven Wrapper**: Always create Maven wrapper for projects
3. **Testing**: Always test that application builds and runs before completing tasks
4. **Unit Tests**: NEVER use Thread.sleep() in tests - they are unstable and unnecessarily slow down test execution
5. **Git Commits**: Never commit changes unless explicitly requested by user
6. **Commit Messages**: Never include Claude Code attribution footer in commit messages (no "🤖 Generated with Claude Code" or "Co-Authored-By: Claude")
7. **Memory File**: ALWAYS keep this file updated with project context and user preferences CONTINUOUSLY throughout the conversation - update immediately when learning new information
8. **Implementation Workflow**: After completing a step and confirming app works (/test), IMMEDIATELY update memory.md with implementation progress BEFORE asking about next step - this ensures single commit instead of two separate commits

### Current Project Setup
- **Package**: com.piotrwalkusz.taskmanager
- **Group ID**: com.piotrwalkusz.taskmanager
- **Artifact ID**: task-manager

### Library Versions (as of 2025-12-03)
- JavaFX: 25.0.1
- MyBatis: 3.5.19
- Flyway: 11.18.0
- SQLite JDBC: 3.51.1.0
- Lombok: 1.18.42
- JUnit: 5.11.3
- Maven Compiler Plugin: 3.14.1
- JavaFX Maven Plugin: 0.0.8

## Project Status
- Basic Maven project structure created
- Maven wrapper configured
- Dependencies configured in pom.xml
- Initial commit created (05f94c2)
- Requirements documentation created (REQUIREMENTS.md)
- Technology stack switched from Hibernate to MyBatis + Flyway
- Custom slash command: `/test` - builds and runs application, auto-fixes errors

### Implementation Progress

**Completed - Core Application:**

1. **Database Layer:**
   - ✅ Flyway migration V1__Create_initial_schema.sql
   - ✅ Flyway migration V2__Add_soft_delete.sql (adds is_deleted flag)
   - ✅ task and work_session tables with proper constraints
   - ✅ Unique indexes and trigger for single active session
   - ✅ Soft delete support for tasks

2. **Model Layer:**
   - ✅ Task and WorkSession POJOs with Instant timestamps (UTC)
   - ✅ Task model includes isDeleted field
   - ✅ Lombok annotations (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)

3. **Persistence Layer (MyBatis):**
   - ✅ TaskMapper interface and XML (getCurrentTask, insertTask, rotateTask, getQueueSize)
   - ✅ TaskMapper soft delete methods (hasDeletedTask, softDeleteTask, undoDelete, cleanupDeletedTasks)
   - ✅ All queries filter out deleted tasks (WHERE is_deleted = 0)
   - ✅ WorkSessionMapper interface and XML (insertWorkSession, pauseWorkSession, hasActiveWorkSession, time calculations)
   - ✅ InstantTypeHandler for UTC timestamp conversion
   - ✅ mybatis-config.xml with SQLite configuration

4. **Service Layer:**
   - ✅ TaskService with transactional rotateTaskWithPause() method
   - ✅ TaskService soft delete methods (softDeleteTask, undoDelete, hasDeletedTask, cleanupDeletedTasks)
   - ✅ WorkSessionService with transactional toggleWorkSession() method
   - ✅ All business operations are atomic and transactional
   - ✅ Proper transaction boundaries using SqlSession
   - ✅ No race conditions - check and modify in same transaction

5. **Configuration:**
   - ✅ DatabaseConfig with Flyway initialization and MyBatis setup
   - ✅ DatabaseConfig uses dependency injection pattern (accepts dbUrl in constructor)
   - ✅ MyBatis config uses Properties to override database URL (simple solution)
   - ✅ Maven compiler plugin configured with Lombok annotation processor

6. **UI Layer:**
   - ✅ main.fxml with all required UI components (task display, time labels, buttons, input field)
   - ✅ Delete Task button and Undo button in main.fxml
   - ✅ MainController with complete business logic
   - ✅ MainController handlers for delete/undo operations
   - ✅ Undo button visibility managed dynamically (visible only when there are deleted tasks)
   - ✅ Automatic cleanup of deleted tasks before Start/Pause or Next Task
   - ✅ Timeline for real-time time updates (every second)
   - ✅ Event handlers for Add Task, Start/Pause, Next Task, Delete Task, Undo

7. **Testing:**
   - ✅ BaseServiceTest with common setup/cleanup
   - ✅ Each test gets isolated database in temp directory (JUnit @TempDir)
   - ✅ TaskServiceTest - 6 tests covering queue operations and rotation
   - ✅ WorkSessionServiceTest - 6 tests covering session management
   - ✅ All tests passing (13 tests, 0 failures, ~3.2s execution time)
   - ✅ No Thread.sleep() - tests are fast and stable

**Application Features Working:**
- ✅ Database automatically created on first run
- ✅ Add new tasks to queue
- ✅ Display current task (head of queue)
- ✅ Start/Pause work sessions (transactional toggle)
- ✅ Rotate tasks to end of queue (transactional with auto-pause)
- ✅ **Delete current task (soft delete with undo)**
- ✅ **Undo delete functionality** - restores deleted tasks
- ✅ **Automatic cleanup** - deleted tasks permanently removed when starting work or rotating
- ✅ Real-time time tracking display (updates every second)
- ✅ Queue size counter
- ✅ All business operations are thread-safe and atomic
- ✅ Comprehensive unit test coverage

**Next Steps:**
- Implement task name editing
- Implement priority flag for tasks
- UI/UX improvements
