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
4. **Git Commits**: Never commit changes unless explicitly requested by user
5. **Commit Messages**: Never include Claude Code attribution footer in commit messages (no "🤖 Generated with Claude Code" or "Co-Authored-By: Claude")
6. **Memory File**: ALWAYS keep this file updated with project context and user preferences CONTINUOUSLY throughout the conversation - update immediately when learning new information
7. **Implementation Workflow**: After completing a step and confirming app works (/test), IMMEDIATELY update memory.md with implementation progress BEFORE asking about next step - this ensures single commit instead of two separate commits

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
**Completed (Commit: d2fc3b3):**
- ✅ Database schema (Flyway migration V1__Create_initial_schema.sql)
  - task table with id, name, queue_order, created_at
  - work_session table with id, task_id, start_time, end_time
  - Unique index on queue_order
  - Unique index on active sessions (WHERE end_time IS NULL)
  - Trigger to enforce single active work session globally
- ✅ Model classes: Task and WorkSession
  - Using Instant for timezone-independent timestamps (UTC)
  - Lombok annotations for boilerplate code

**Next Steps:**
- MyBatis mapper interfaces and XML
- Service layer (TaskService, WorkSessionService)
- Database configuration and MyBatis setup
- UI implementation (FXML)
- Controller implementation
- Unit tests
