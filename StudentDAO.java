// StudentDAO.java
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public static void addStudent(Student s) throws SQLException {
        String sql = "INSERT INTO students(first_name, last_name, age, sex, phone_number, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, s.firstName);
            stmt.setString(2, s.lastName);
            stmt.setInt(3, s.age);
            stmt.setString(4, s.sex);
            stmt.setString(5, s.phoneNumber);
            stmt.setString(6, s.address);
            stmt.executeUpdate();
        }
    }

    public static void updateStudent(Student s) throws SQLException {
        String sql = "UPDATE students SET first_name=?, last_name=?, age=?, sex=?, phone_number=?, address=? WHERE id=?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, s.firstName);
            stmt.setString(2, s.lastName);
            stmt.setInt(3, s.age);
            stmt.setString(4, s.sex);
            stmt.setString(5, s.phoneNumber);
            stmt.setString(6, s.address);
            stmt.setInt(7, s.id);
            stmt.executeUpdate();
        }
    }

    public static void deleteStudent(int studentId) throws SQLException {
        // Important: Also delete associated enrollments to maintain data integrity
        String deleteEnrollmentsSql = "DELETE FROM enrollments WHERE student_id = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(deleteEnrollmentsSql)) {
            stmt.setInt(1, studentId);
            stmt.executeUpdate();
        }

        String deleteStudentSql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(deleteStudentSql)) {
            stmt.setInt(1, studentId);
            stmt.executeUpdate();
        }
    }

    public static List<Student> searchStudents(String keyword) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE first_name LIKE ? OR last_name LIKE ? OR CAST(id AS CHAR) LIKE ? ORDER BY first_name ASC";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            String likeKeyword = "%" + keyword + "%";
            stmt.setString(1, likeKeyword);
            stmt.setString(2, likeKeyword);
            stmt.setString(3, keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Student(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("age"),
                        rs.getString("sex"),
                        rs.getString("phone_number"),
                        rs.getString("address")
                ));
            }
        }
        return list;
    }
    
    public static boolean studentExists(Student s) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE first_name=? AND last_name=? AND age=?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, s.firstName);
            stmt.setString(2, s.lastName);
            stmt.setInt(3, s.age);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}