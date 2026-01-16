# Course Management System

A modern desktop application for managing student records built with JavaFX and SQLite.

## 📋 Project Overview

The Course Management System is a JavaFX-based desktop application that provides a complete solution for managing student information. The system features user authentication, CRUD operations for student records, real-time search functionality, and a clean, intuitive user interface.

### Key Features

- **User Authentication**: Secure registration and login with SHA-256 password hashing
- **Student Management**: Full CRUD operations (Create, Read, Update, Delete)
- **Real-time Search**: Debounced search across all student fields
- **Multi-user Support**: Each user has their own isolated student records
- **Responsive UI**: Modern design with loading indicators and form validation
- **Data Persistence**: SQLite database for reliable data storage
- 
## 🏗️ Design Choices

### Architecture

The project follows a **layered architecture** pattern:

```
├── controllers/     # UI logic and user interactions
├── models/          # Data models (Student, Session)
├── database/        # Data access layer
└── resources/       # FXML layouts and assets
```

### Key Design Decisions

1. **JavaFX FXML**: Separation of UI design (FXML) from business logic (Controllers)
2. **SQLite Database**: Lightweight, embedded database requiring no separate server
3. **Password Security**: SHA-256 hashing with Base64 encoding for secure password storage
4. **Multi-threading**: ExecutorService for background database operations to prevent UI freezing
5. **Session Management**: Static Session class to maintain user state across screens
6. **Input Validation**: Client-side validation with length limits and required field checks

### Challenges Faced

1. **Concurrent Operations**: Implemented ExecutorService with daemon threads to handle database operations without blocking the UI thread
2. **Search Performance**: Added 300ms debounce timer to prevent excessive database queries during typing
3. **Edit Mode State**: Created a sophisticated edit mode system with visual feedback and state management
4. **User Isolation**: Ensured each user can only access their own student records through database-level filtering

## 🔧 Algorithms and Data Structures

### Data Structures

| Structure | Usage | Justification |
|-----------|-------|---------------|
| **ObservableList** | Student records in TableView | Automatic UI updates when data changes |
| **HashMap (implicit)** | Database result set mapping | Efficient column name to value mapping |
| **ExecutorService Queue** | Background task management | Thread-safe operation queuing |

### Algorithms

#### 1. **Password Hashing Algorithm**
```java
SHA-256 → Base64 Encoding
```
- **Time Complexity**: O(n) where n is password length
- **Purpose**: Secure one-way password encryption

#### 2. **Search Algorithm**
```sql
LIKE query with LOWER() function across multiple fields
```
- **Time Complexity**: O(n) where n is number of records
- **Optimization**: Debouncing (300ms delay) to reduce query frequency

#### 3. **Sorting Algorithm**
- **Built-in**: JavaFX TableView uses TimSort (hybrid merge-insertion sort)
- **Time Complexity**: O(n log n)
- **Space Complexity**: O(n)

#### 4. **Input Validation**
```java
Real-time length limiting + format validation
```
- **Time Complexity**: O(1) per keystroke
- **Prevents**: SQL injection and buffer overflow

## 🚀 Potential Improvements

### Feature Enhancements

1. **Export Functionality**
   - CSV export: `File → Export → Select location`
   - PDF reports with student statistics
   - Excel format support using Apache POI

2. **Import Functionality**
   - Bulk student import from CSV/Excel
   - Data validation during import
   - Duplicate detection and merge options

3. **Advanced Search**
   - Filter by specific fields (faculty, department)
   - Date range filtering (enrollment dates)
   - Saved search queries

4. **Data Analytics**
   - Student distribution charts by faculty
   - Department statistics
   - Export analytics reports

### Technical Improvements

1. **Database Migration System**: Version-controlled schema updates
2. **Connection Pooling**: Reuse database connections for better performance
3. **Caching Layer**: In-memory cache for frequently accessed data
4. **Audit Logging**: Track all data modifications with timestamps
5. **Role-Based Access**: Admin, Teacher, and Viewer roles

### File I/O Usage Examples

#### CSV Export Implementation
```java
public void exportToCSV(String filepath) {
    try (PrintWriter writer = new PrintWriter(filepath)) {
        writer.println("ID,Name,Surname,Faculty,Department,Group");
        for (Student s : students) {
            writer.printf("%d,%s,%s,%s,%s,%s%n",
                s.getId(), s.getName(), s.getSurname(),
                s.getFaculty(), s.getDepartment(), s.getGroup());
        }
    }
}
```

#### CSV Import Implementation
```java
public void importFromCSV(String filepath) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
        String line = reader.readLine(); // Skip header
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            Student student = new Student(
                Integer.parseInt(parts[0]), parts[1], parts[2],
                parts[3], parts[4], parts[5]
            );
            Database.addStudentForUser(student, userId);
        }
    }
}
```

## 📸 Screenshots

### Login Screen
<img width="1920" height="1080" alt="Снимок экрана (56)" src="https://github.com/user-attachments/assets/29e7c1a3-7cd9-4f37-9cd4-0bd810b16955" />
*Secure authentication with password hashing*

### Registration
<img width="1920" height="1080" alt="Снимок экрана (58)" src="https://github.com/user-attachments/assets/c710005f-d921-4740-ae42-34c4ad415cec" />
*New user registration with validation*

### Main Dashboard
<img width="1920" height="1080" alt="Снимок экрана (59)" src="https://github.com/user-attachments/assets/0d986391-6a12-45c4-914d-0b642d8b0d4e" />
*Student management interface with search and CRUD operations*

### Edit Mode
<img width="1920" height="1080" alt="Снимок экрана (60)" src="https://github.com/user-attachments/assets/e3fb8db9-9993-49f3-a17d-f0c28f7c498a" />
*Inline editing with visual feedback*

## 🛠️ Technology Stack

- **Java 23**: Latest LTS version with modern language features
- **JavaFX 25**: Rich client GUI framework
- **SQLite 3.45**: Embedded relational database
- **Maven**: Dependency management and build automation

## 📦 Installation & Setup

### Prerequisites

- Java JDK 23 or higher
- Maven 3.8+

### Build & Run

```bash
# Clone the repository
git clone [your-repo-url]
cd cms

# Compile the project
mvn clean compile

# Run the application
mvn javafx:run
```

### Create Executable JAR

```bash
mvn clean package
java -jar target/cms-1.0-SNAPSHOT.jar
```

## 📁 Project Structure

```
cms/
├── src/main/
│   ├── java/com/example/cms/
│   │   ├── controllers/        # UI Controllers
│   │   │   ├── LoginController.java
│   │   │   ├── RegisterController.java
│   │   │   └── CourseController.java
│   │   ├── models/            # Data Models
│   │   │   ├── Student.java
│   │   │   └── Session.java
│   │   ├── database/          # Database Layer
│   │   │   └── Database.java
│   │   └── HelloApplication.java  # Main Entry Point
│   └── resources/
│       ├── com/example/cms/   # FXML Files
│       │   ├── login.fxml
│       │   ├── register.fxml
│       │   └── course.fxml
│       └── edupage.png        # Application Icon
├── pom.xml                    # Maven Configuration
└── courses.db                 # SQLite Database (auto-created)
```

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL  -- SHA-256 hashed
);
```

### Students Table
```sql
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

## 🔐 Security Features

1. **Password Hashing**: SHA-256 with Base64 encoding
2. **SQL Injection Prevention**: Prepared statements throughout
3. **User Isolation**: Row-level security via user_id foreign key
4. **Input Sanitization**: Field length limits and type validation
5. **Session Management**: Secure session state without cookies

## 🧪 Testing Approach

### Manual Test Cases

1. **Authentication Flow**
   - Register new user → Login → Verify access
   - Invalid credentials → Error message
   - Duplicate username → Registration failure

2. **Student CRUD Operations**
   - Add student → Verify in table
   - Edit student → Verify changes
   - Delete student → Verify removal
   - Search student → Verify results

3. **Data Validation**
   - Empty required fields → Error
   - Invalid ID format → Error
   - Field length limits → Truncation
   - Duplicate student ID → Error

## 📝 Additional Notes

### Performance Considerations

- **Database Indexing**: Student_id and user_id are indexed for fast lookups
- **Connection Management**: Each operation opens/closes connections to prevent leaks
- **UI Thread Safety**: Platform.runLater() used for all UI updates from background threads

### Code Quality

- **Error Handling**: Try-catch blocks with user-friendly error messages
- **Resource Management**: Try-with-resources for automatic cleanup
- **Code Organization**: Clear separation of concerns across layers
- **Naming Conventions**: Descriptive variable and method names

### Future Scalability

The current architecture supports easy extension:
- Add new fields to Student model
- Implement new controllers for additional screens
- Extend Database class with new operations
- Integrate with external APIs or services

## 👨‍💻 Author

**Abakirov Alisher SCA-24A**

## 📄 License

This project is created for educational purposes as part of BAP & OSE at IT & Business College, AIU.

---

**Note**: This is an academic project demonstrating JavaFX application development, database integration, and software engineering principles.
