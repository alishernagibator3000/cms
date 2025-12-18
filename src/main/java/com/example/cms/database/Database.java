package com.example.cms.database;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.example.cms.models.Student;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;

public class Database {

    private static final String URL = "jdbc:sqlite:courses.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void createTables() {
        String usersSql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            );
            """;

        String studentsSql = """
            CREATE TABLE IF NOT EXISTS students (
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
            """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(usersSql);
            stmt.execute(studentsSql);
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public static boolean registerUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users(username, password) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return false;
            }
            throw e;
        }
    }

    public static boolean validateUser(String username, String password) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static int getUserId(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                else return -1;
            }
        }
    }

    public static boolean studentIdExists(int studentId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM students WHERE student_id = ? AND user_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.getInt("count") > 0;
            }
        }
    }

    public static void addStudentForUser(Student student, int userId) throws SQLException {
        if (studentIdExists(student.getId(), userId)) {
            throw new SQLException("Student with ID " + student.getId() + " already exists");
        }

        String sql = "INSERT INTO students(student_id, name, surname, faculty, department, student_group, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, student.getId());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getSurname());
            pstmt.setString(4, nullIfEmpty(student.getFaculty()));
            pstmt.setString(5, nullIfEmpty(student.getDepartment()));
            pstmt.setString(6, nullIfEmpty(student.getGroup()));
            pstmt.setInt(7, userId);
            pstmt.executeUpdate();
        }
    }

    public static ObservableList<Student> getAllStudentsForUser(int userId) throws SQLException {
        ObservableList<Student> list = FXCollections.observableArrayList();
        String sql = "SELECT student_id, name, surname, faculty, department, student_group FROM students WHERE user_id = ? ORDER BY student_id";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(
                            rs.getInt("student_id"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            emptyIfNull(rs.getString("faculty")),
                            emptyIfNull(rs.getString("department")),
                            emptyIfNull(rs.getString("student_group"))
                    ));
                }
            }
        }
        return list;
    }

    public static int deleteStudentForUser(int studentId, int userId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ? AND user_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        }
    }

    public static int updateStudentForUser(Student student, int userId) throws SQLException {
        String sql = "UPDATE students SET name = ?, surname = ?, faculty = ?, department = ?, student_group = ? WHERE student_id = ? AND user_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getSurname());
            pstmt.setString(3, nullIfEmpty(student.getFaculty()));
            pstmt.setString(4, nullIfEmpty(student.getDepartment()));
            pstmt.setString(5, nullIfEmpty(student.getGroup()));
            pstmt.setInt(6, student.getId());
            pstmt.setInt(7, userId);
            return pstmt.executeUpdate();
        }
    }

    public static ObservableList<Student> searchStudentsForUser(String text, int userId) throws SQLException {
        ObservableList<Student> list = FXCollections.observableArrayList();
        String sql = "SELECT student_id, name, surname, faculty, department, student_group FROM students WHERE user_id = ? AND (" +
                "LOWER(CAST(student_id AS TEXT)) LIKE ? OR LOWER(name) LIKE ? OR LOWER(surname) LIKE ? OR LOWER(COALESCE(faculty, '')) LIKE ? OR LOWER(COALESCE(department, '')) LIKE ? OR LOWER(COALESCE(student_group, '')) LIKE ?) ORDER BY student_id";
        String q = "%" + text.toLowerCase() + "%";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, q);
            pstmt.setString(3, q);
            pstmt.setString(4, q);
            pstmt.setString(5, q);
            pstmt.setString(6, q);
            pstmt.setString(7, q);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(
                            rs.getInt("student_id"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            emptyIfNull(rs.getString("faculty")),
                            emptyIfNull(rs.getString("department")),
                            emptyIfNull(rs.getString("student_group"))
                    ));
                }
            }
        }
        return list;
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private static String nullIfEmpty(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value;
    }
}