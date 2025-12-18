# Course Management System

A desktop application for managing student records built with JavaFX and SQLite. Features user authentication, CRUD operations, real-time search, and a clean, modern UI.

## Project Overview

This application provides a complete student management solution with the following core features:

- **User Authentication**: Secure registration and login with SHA-256 password hashing
- **Student Management**: Full CRUD (Create, Read, Update, Delete) operations
- **Real-time Search**: Instant filtering across all student fields with 300ms debounce
- **Data Isolation**: Each user maintains their own isolated student database
- **Modern UI**: Clean, responsive interface with visual feedback and loading indicators

### Design Choices

**Architecture**: MVC (Model-View-Controller) pattern for separation of concerns
- `models/` - Data entities (Student, Session)
- `controllers/` - UI logic and event handling
- `database/` - Data access layer
- FXML files - View definitions

**Technology Stack**:
- **JavaFX 25**: Modern UI framework with FXML for declarative layouts
- **SQLite**: Lightweight, embedded database requiring no server setup
- **Maven**: Dependency management and build automation

**Key Design Decisions**:
1. **Thread Safety**: All database operations run on background threads via `ExecutorService` to prevent UI freezing
2. **Input Validation**: Client-side validation with 255-character limits and required field checks
3. **Security**: Password hashing using SHA-256 with Base64 encoding
4. **User Experience**: Visual feedback through loading indicators, confirmation dialogs, and edit mode styling
5. **Search Optimization**: Timer-based debouncing (300ms) to reduce unnecessary database queries during typing

### Challenges Overcome

1. **Concurrent Operations**: Implemented single-thread executor to prevent race conditions in database access
2. **Edit Mode State Management**: Created a state machine to handle form modes (add/edit) with visual indicators
3. **Search Performance**: Used debounced search with parameterized queries to prevent SQL injection while maintaining responsiveness
4. **Data Isolation**: Implemented user-scoped queries ensuring each user only accesses their own data
5. **JavaFX Threading**: Properly managed Platform.runLater() calls for UI updates from background threads

## Video Demonstration

[Insert video link here]

## Algorithms and Data Structures

### Data Structures

**1. ObservableList (JavaFX Collections)**
- Used for TableView data binding
- Automatically updates UI when modified
- Provides efficient O(1) access by index

**2. HashMap (SHA-256 Internal)**
- Used in password hashing algorithm
- Provides O(1) lookup for hash computation

**3. SQLite B-Tree Index**
- Database uses B-tree for primary key indexing
- Provides O(log n) search complexity
- Automatic index on `(student_id, user_id)` composite key

### Algorithms

**1. SHA-256 Password Hashing**
```java
private static String hashPassword(String password) {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] hash = md.digest(password.getBytes());
    return Base64.getEncoder().encodeToString(hash);
}
```
- **Complexity**: O(n) where n is password length
- **Purpose**: One-way cryptographic hash for secure password storage
- **Output**: 44-character Base64 string

**2. Debounced Search**
```java
protected void searchStudent() {
    if (searchTimer != null) searchTimer.cancel();
    searchTimer = new Timer(true);
    searchTimer.schedule(new TimerTask() {
        public void run() {
            // Execute search after 300ms delay
        }
    }, 300);
}
```
- **Complexity**: O(1) for scheduling, O(m log n) for search where m is result size
- **Purpose**: Reduces database load by waiting for user to stop typing
- **Pattern**: Debounce pattern with 300ms delay

**3. Wildcard Search Query**
```sql
SELECT * FROM students 
WHERE user_id = ? AND (
    LOWER(CAST(student_id AS TEXT)) LIKE ? OR 
    LOWER(name) LIKE ? OR 
    LOWER(surname) LIKE ? OR ...
)
```
- **Complexity**: O(n) table scan with LIKE operator
- **Optimization**: User ID filtering reduces search space
- **Pattern**: Case-insensitive partial matching across multiple fields

**4. Sorting Algorithm**
- JavaFX TableView uses TimSort (hybrid merge-insertion sort)
- **Complexity**: O(n log n) average and worst case
- **Stability**: Preserves relative order of equal elements

## Database Schema

```sql
-- Users table
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL
);

-- Students table with foreign key constraint
CREATE TABLE students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    faculty TEXT,
    department TEXT,
    student_group TEXT,
    user_id INTEGER,
    UNIQUE(student_id, user_id),
    FOREIGN KEY(user_id) REFERENCES users(id)
);
```

**Indexing Strategy**:
- Primary key index on `id` (automatic)
- Unique composite index on `(student_id, user_id)` prevents duplicate students per user
- Foreign key index on `user_id` for efficient user-based queries

## Improvements and Future Enhancements

### Implemented Improvements

1. **Asynchronous Operations**: All database calls use background threads
2. **Input Validation**: Field length limits, required field checks, numeric validation
3. **Error Handling**: Comprehensive try-catch blocks with user-friendly error messages
4. **Visual Feedback**: Loading indicators, edit mode styling, confirmation dialogs
5. **Search Optimization**: Debounced search reduces database load
6. **Data Integrity**: Foreign keys, unique constraints, prepared statements prevent SQL injection

### Potential Enhancements

**1. Export/Import Functionality**
```java
// Export to CSV
public static void exportToCSV(List<Student> students, String filepath) {
    try (PrintWriter writer = new PrintWriter(filepath)) {
        writer.println("ID,Name,Surname,Faculty,Department,Group");
        for (Student s : students) {
            writer.printf("%d,%s,%s,%s,%s,%s%n",
                s.getId(), s.getName(), s.getSurname(),
                s.getFaculty(), s.getDepartment(), s.getGroup());
        }
    }
}

// Import from CSV
public static List<Student> importFromCSV(String filepath) {
    List<Student> students = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
        reader.readLine(); // Skip header
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            students.add(new Student(
                Integer.parseInt(parts[0]), parts[1], parts[2],
                parts[3], parts[4], parts[5]
            ));
        }
    }
    return students;
}
```

**2. Advanced Search with Filters**
- Date range filtering (enrollment date)
- Multi-field AND/OR queries
- Saved search templates

**3. Batch Operations**
- Import multiple students from Excel/CSV
- Bulk update/delete with confirmation
- Data validation during import

**4. Reports Generation**
- PDF export using Apache PDFBox
- Student statistics dashboard
- Faculty/department summaries

**5. Audit Logging**
- Track all CRUD operations with timestamps
- User activity history
- Rollback capability

## Screenshots

### Login Screen
![Login](docs/screenshots/login.png)
*Clean authentication interface with validation*

### Registration
![Register](docs/screenshots/register.png)
*New user registration with password confirmation*

### Main Dashboard
![Dashboard](docs/screenshots/dashboard.png)
*Student management interface with real-time search*

### Edit Mode
![Edit](docs/screenshots/edit.png)
*In-place editing with visual indicators*

## Installation and Usage

### Prerequisites
- Java 23 or higher
- Maven 3.8+

### Building the Project
```bash
# Clone repository
git clone [repository-url]
cd cms

# Build with Maven
mvn clean install

# Run application
mvn javafx:run
```

### First Time Setup
1. Launch application
2. Click "Create new account"
3. Enter username (min 3 chars) and password (min 4 chars)
4. Login with credentials
5. Start adding students

### Database Location
- SQLite database file: `courses.db` (created automatically in project root)
- Location can be modified in `Database.java` by changing `URL` constant

## Project Structure

```
cms/
├── src/main/java/com/example/cms/
│   ├── HelloApplication.java          # Main entry point
│   ├── controllers/
│   │   ├── CourseController.java      # Student CRUD logic
│   │   ├── LoginController.java       # Authentication
│   │   └── RegisterController.java    # User registration
│   ├── models/
│   │   ├── Student.java               # Student entity
│   │   └── Session.java               # Session management
│   └── database/
│       └── Database.java              # Data access layer
├── src/main/resources/com/example/cms/
│   ├── course.fxml                    # Main dashboard UI
│   ├── login.fxml                     # Login UI
│   └── register.fxml                  # Registration UI
├── pom.xml                            # Maven configuration
└── README.md
```

## Technical Notes

### Thread Management
All database operations use a single-thread executor to prevent concurrent modification issues. The executor is properly shut down during logout/cleanup to prevent resource leaks.

### Memory Considerations
- ObservableList holds all students in memory for current user
- For large datasets (>10,000 students), consider implementing pagination
- Current implementation suitable for typical classroom sizes (50-500 students)

### Security Notes
- Passwords are hashed with SHA-256 before storage
- SQL injection prevented through PreparedStatement
- User data isolation enforced at database level
- Note: For production use, consider bcrypt/argon2 instead of SHA-256, and add salt

### Performance Characteristics
- Login: O(1) with username index
- Add Student: O(1) insert operation
- Search: O(n) table scan, optimized with debounce
- Delete: O(log n) with primary key index
- Update: O(log n) with primary key index

## License

[Insert license information]

## Contributors

[Insert contributor information]
