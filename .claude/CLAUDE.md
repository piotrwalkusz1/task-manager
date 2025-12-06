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

### Library Versions (as of 2025-12-05)
- JavaFX: 25.0.1
- MyBatis: 3.5.19
- Flyway: 11.18.0
- SQLite JDBC: 3.51.1.0
- Lombok: 1.18.42
- JUnit: 5.11.3
- Maven Compiler Plugin: 3.14.1
- JavaFX Maven Plugin: 0.0.8
- jpackage-maven-plugin: 1.7.1
- maven-dependency-plugin: 3.8.1

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
   - ✅ TaskMapper updateTaskName method for editing task names
   - ✅ All queries filter out deleted tasks (WHERE is_deleted = 0)
   - ✅ WorkSessionMapper interface and XML (insertWorkSession, pauseWorkSession, hasActiveWorkSession, getActiveWorkSession)
   - ✅ WorkSessionMapper time calculations (getDailyTimeSeconds, getTotalTimeSeconds) - returns only completed sessions
   - ✅ InstantTypeHandler for UTC timestamp conversion
   - ✅ mybatis-config.xml with SQLite configuration

4. **Service Layer:**
   - ✅ TaskService with transactional rotateTaskWithPause() method
   - ✅ TaskService transactional softDeleteTask() - pauses active session before soft delete
   - ✅ TaskService soft delete methods (undoDelete, hasDeletedTask, cleanupDeletedTasks)
   - ✅ TaskService updateTaskName method for editing task names
   - ✅ WorkSessionService with transactional toggleWorkSession() method
   - ✅ All business operations are atomic and transactional
   - ✅ Proper transaction boundaries using SqlSession
   - ✅ No race conditions - check and modify in same transaction

5. **Configuration:**
   - ✅ DatabaseConfig with Flyway initialization and MyBatis setup
   - ✅ DatabaseConfig uses dependency injection pattern (accepts dbUrl in constructor)
   - ✅ DatabaseConfig enables SQLite foreign keys via URL parameter (?foreign_keys=on)
   - ✅ MyBatis config uses Properties to override database URL (simple solution)
   - ✅ Maven compiler plugin configured with Lombok annotation processor

6. **UI Layer:**
   - ✅ main.fxml with all required UI components (task display, time labels, buttons, input field)
   - ✅ Delete Task button and Undo button in main.fxml
   - ✅ Task name editing UI with StackPane containing TextField (read-only) and TextField (edit mode)
   - ✅ Compact UI layout optimized for minimal space usage
   - ✅ Icon-only buttons with Unicode symbols (▶/⏸ for Start/Pause, ⤵ for Next Task, ✓ for Done, ↩ for Undo)
   - ✅ Optimized font sizes (18px for task name, 13px for time/queue info, 14px for input)
   - ✅ Compact spacing (10px main, 8px between buttons) and padding (15px main, 12px task section)
   - ✅ MainController with complete business logic
   - ✅ MainController handlers for delete/undo operations
   - ✅ MainController task name editing with double-click handler
   - ✅ Hand cursor removed from "No tasks in queue" label (dynamic cursor in refreshUI)
   - ✅ Left-aligned task name (StackPane alignment="CENTER_LEFT")
   - ✅ Event filter to save edits when clicking outside TextField
   - ✅ Event filter to remove focus from new task field when clicking elsewhere
   - ✅ isDescendant() helper to allow text selection within TextField
   - ✅ Undo button visibility managed dynamically (visible only when there are deleted tasks)
   - ✅ Automatic cleanup of deleted tasks before Start/Pause, Next Task, AND Delete Task
   - ✅ Bug fixed: cleanup now prevents accumulation of multiple soft-deleted tasks
   - ✅ Timeline for real-time time updates (every second)
   - ✅ Event handlers for Add Task, Start/Pause, Next Task, Delete Task, Undo, Edit Task Name
   - ✅ **Selectable text labels** - all text fields (task name, time, queue size) are selectable and copyable
   - ✅ **Text selection preservation** - equality checks before setText() to prevent selection reset
   - ✅ **Focus management** - focusTraversable="false" on read-only fields prevents unwanted focus/selection
   - ✅ **Optimized time tracking** - active session cached in memory, time calculated locally without DB queries
   - ✅ **Smooth timer updates** - no more skipped seconds or 2-second jumps, updates every second precisely

7. **Testing:**
   - ✅ BaseServiceTest with common setup/cleanup
   - ✅ Each test gets isolated database in temp directory (JUnit @TempDir)
   - ✅ TaskServiceTest - 9 tests covering queue operations, rotation, task name editing, and soft delete with session pause
   - ✅ WorkSessionServiceTest - 6 tests covering session management
   - ✅ All tests passing (16 tests, 0 failures, ~2.8s execution time)
   - ✅ No Thread.sleep() - tests are fast and stable

**Application Features Working:**
- ✅ Database automatically created on first run
- ✅ Add new tasks to queue (via + button or Enter key)
- ✅ Display current task (head of queue)
- ✅ Start/Pause work sessions (transactional toggle)
- ✅ Rotate tasks to end of queue (transactional with auto-pause)
- ✅ **Edit task name** - double-click to edit, Enter to save, Escape to cancel, click outside to save
- ✅ **Delete current task (soft delete with undo)**
- ✅ **Undo delete functionality** - restores deleted tasks
- ✅ **Automatic cleanup** - deleted tasks permanently removed before any operation (Start/Pause, Next Task, Delete Task)
- ✅ **Real-time time tracking** - smooth updates every second without database queries
- ✅ **Efficient time calculation** - active session cached in memory, completed time from DB only on state changes
- ✅ Queue size counter
- ✅ **Compact, icon-based UI** - minimal space usage with clear visual feedback
- ✅ **Selectable and copyable text** - all labels can be selected and copied without entering edit mode
- ✅ **Smart focus management** - new task field loses focus after adding task or clicking elsewhere
- ✅ **Preserved text selection** - selection doesn't reset during timer updates
- ✅ All business operations are thread-safe and atomic
- ✅ Comprehensive unit test coverage

8. **Release/Distribution (Portable Apps):**
   - ✅ jpackage-maven-plugin configured for creating portable applications
   - ✅ Type: APP_IMAGE (no installer, fully portable)
   - ✅ Automatic dependency copying (maven-dependency-plugin)
   - ✅ JavaFX modules loaded via --module-path and --add-modules in javaOptions
   - ✅ Custom runtime with embedded JRE (via jlink)
   - ✅ Tested locally on Linux - application runs successfully
   - ✅ Build command: `./mvnw clean package jpackage:jpackage`
   - ✅ Output: `target/dist/TaskManager/` (portable folder with bin/TaskManager launcher)
   - ✅ Works on both Linux and Windows (same APP_IMAGE configuration)
   - ✅ Version management: pom.xml version is placeholder (1.0.0), actual version from git tag
   - ✅ JDK requirement: Java 25 (Temurin recommended for jlink/jpackage support)

**Release Workflow:**
- Version in pom.xml: 0.0.0-SNAPSHOT (placeholder, never changed in repo)
- Actual release version comes from git tag (e.g., v1.0.0)
- GitHub Actions workflow (.github/workflows/release.yml):
  - Trigger: push tag matching `v*`
  - Parallel builds: Windows (windows-latest) and Linux (ubuntu-latest)
  - JDK: 25 (Temurin distribution)
  - Build command: `./mvnw clean package jpackage:jpackage`
  - Outputs:
    - Windows: TaskManager-{version}-windows-x64.zip
    - Linux: TaskManager-{version}-linux-x64.tar.gz
  - Checksums: SHA256 for each archive
  - Metadata: version.json with download URLs and checksums
  - Release: automatic GitHub Release with all artifacts
- Distribution: portable archives on GitHub Releases (no installers)
- Future: DIY auto-updater to check GitHub Releases API

9. **CI/CD (GitHub Actions):**
   - ✅ Release workflow configured (.github/workflows/release.yml)
   - ✅ Automatic builds on tag push (v*)
   - ✅ Parallel Windows and Linux builds
   - ✅ Portable archives (ZIP for Windows, tar.gz for Linux)
   - ✅ SHA256 checksums
   - ✅ version.json metadata for auto-updater
   - ✅ Automatic GitHub Release creation

**Next Steps:**
- Implement DIY auto-updater
- Implement priority flag for tasks
- Further UI/UX refinements based on user feedback
