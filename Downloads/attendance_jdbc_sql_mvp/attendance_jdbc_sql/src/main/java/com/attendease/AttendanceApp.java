package com.attendease;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class AttendanceApp {
    private static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("      ATTENDEASE - ATTENDANCE APP");
        System.out.println("======================================");

        try (Connection ignored = Database.getConnection()) {
            System.out.println("Database connection successful.");
        } catch (SQLException e) {
            System.out.println("Could not connect to MySQL: " + e.getMessage());
            System.out.println("Check MySQL is running, schema.sql is imported, and DB credentials are correct.");
            return;
        }

        while (true) {
            System.out.println("\n--------------- MENU ----------------");
            System.out.println("1. Add student");
            System.out.println("2. List students");
            System.out.println("3. Mark/update attendance");
            System.out.println("4. View a student's attendance records");
            System.out.println("5. Calculate attendance percentage");
            System.out.println("6. View attendance for a date");
            System.out.println("7. Delete student");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = input.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addStudent();
                    case "2" -> listStudents();
                    case "3" -> markAttendance();
                    case "4" -> viewStudentRecords();
                    case "5" -> calculatePercentage();
                    case "6" -> viewDateAttendance();
                    case "7" -> deleteStudent();
                    case "0" -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please choose 0-7.");
                }
            } catch (SQLException e) {
                System.out.println("Database operation failed: " + e.getMessage());
            }
        }
    }

    private static void addStudent() throws SQLException {
        String studentId = readRequired("Enter student/register ID: ");
        String name = readRequired("Enter student name: ");

        String sql = "INSERT INTO students (student_id, name) VALUES (?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, name);
            ps.executeUpdate();
            System.out.println("Student added successfully.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("That student ID already exists.");
        }
    }

    private static void listStudents() throws SQLException {
        String sql = "SELECT id, student_id, name FROM students ORDER BY name";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nID | Register Number | Student Name");
            System.out.println("----------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%d | %s | %s%n",
                        rs.getInt("id"), rs.getString("student_id"), rs.getString("name"));
            }
            if (!found) System.out.println("No students found.");
        }
    }

    private static void markAttendance() throws SQLException {
        listStudents();
        int studentPk = readInt("Enter the numeric ID from the first column: ");
        LocalDate date = readDate("Enter date (YYYY-MM-DD): ");
        String status;
        while (true) {
            status = readRequired("Status (P = Present, A = Absent): ").toUpperCase();
            if (status.equals("P") || status.equals("A")) break;
            System.out.println("Enter P or A.");
        }
        status = status.equals("P") ? "Present" : "Absent";

        String sql = """
                INSERT INTO attendance (student_pk, attendance_date, status)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE status = VALUES(status)
                """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentPk);
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, status);
            int rows = ps.executeUpdate();
            System.out.println("Attendance saved (" + status + ") for " + date + ".");
        }
    }

    private static void viewStudentRecords() throws SQLException {
        String studentId = readRequired("Enter student/register ID: ");
        String studentSql = "SELECT id, student_id, name FROM students WHERE student_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement studentPs = con.prepareStatement(studentSql)) {
            studentPs.setString(1, studentId);
            try (ResultSet student = studentPs.executeQuery()) {
                if (!student.next()) {
                    System.out.println("Student not found.");
                    return;
                }
                int pk = student.getInt("id");
                System.out.println("\n" + student.getString("name") + " (" + student.getString("student_id") + ")");
                String recordsSql = "SELECT attendance_date, status FROM attendance WHERE student_pk = ? ORDER BY attendance_date DESC";
                try (PreparedStatement recordsPs = con.prepareStatement(recordsSql)) {
                    recordsPs.setInt(1, pk);
                    try (ResultSet rs = recordsPs.executeQuery()) {
                        System.out.println("Date         | Status");
                        System.out.println("-------------------------");
                        boolean found = false;
                        while (rs.next()) {
                            found = true;
                            System.out.printf("%s | %s%n", rs.getDate("attendance_date"), rs.getString("status"));
                        }
                        if (!found) System.out.println("No attendance records yet.");
                    }
                }
            }
        }
    }

    private static void calculatePercentage() throws SQLException {
        String studentId = readRequired("Enter student/register ID: ");
        String sql = """
                SELECT s.name, s.student_id,
                       COUNT(a.id) AS total_marked,
                       COALESCE(SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END), 0) AS present_days
                FROM students s
                LEFT JOIN attendance a ON a.student_pk = s.id
                WHERE s.student_id = ?
                GROUP BY s.id, s.name, s.student_id
                """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Student not found.");
                    return;
                }
                int total = rs.getInt("total_marked");
                int present = rs.getInt("present_days");
                double percentage = total == 0 ? 0.0 : present * 100.0 / total;
                System.out.println("\nStudent: " + rs.getString("name"));
                System.out.println("Register number: " + rs.getString("student_id"));
                System.out.println("Present: " + present);
                System.out.println("Total marked days: " + total);
                System.out.printf("Attendance percentage: %.2f%%%n", percentage);
                if (total == 0) System.out.println("(No attendance has been marked yet.)");
            }
        }
    }

    private static void viewDateAttendance() throws SQLException {
        LocalDate date = readDate("Enter date (YYYY-MM-DD): ");
        String sql = """
                SELECT s.student_id, s.name, a.status
                FROM students s
                LEFT JOIN attendance a ON a.student_pk = s.id AND a.attendance_date = ?
                ORDER BY s.name
                """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\nAttendance for " + date);
                System.out.println("Register Number | Student Name | Status");
                System.out.println("-----------------------------------------------");
                while (rs.next()) {
                    String status = rs.getString("status");
                    System.out.printf("%s | %s | %s%n",
                            rs.getString("student_id"), rs.getString("name"),
                            status == null ? "Not marked" : status);
                }
            }
        }
    }

    private static void deleteStudent() throws SQLException {
        String studentId = readRequired("Enter student/register ID to delete: ");
        System.out.print("This also deletes their attendance history. Confirm (yes/no): ");
        if (!input.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Delete cancelled.");
            return;
        }
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "Student deleted." : "Student not found.");
        }
    }

    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = input.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = input.nextLine().trim();
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                System.out.println("Use YYYY-MM-DD format, for example 2026-10-27.");
            }
        }
    }
}
