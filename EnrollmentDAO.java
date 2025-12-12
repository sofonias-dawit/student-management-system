// EnrollmentDAO.java
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnrollmentDAO {

    public static void enrollStudent(int studentId, int courseId) throws SQLException {
        String sql = "INSERT INTO enrollments(student_id, course_id) VALUES (?, ?)";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            stmt.executeUpdate();
        }
    }

    public static void unenrollStudent(int studentId, int courseId) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            stmt.executeUpdate();
        }
    }

    public static List<Course> getEnrolledCoursesForStudent(int studentId) throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.course_id, c.course_name, c.course_description " +
                     "FROM courses c " +
                     "JOIN enrollments e ON c.course_id = e.course_id " +
                     "WHERE e.student_id = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Course(
                        rs.getInt("course_id"),
                        rs.getString("course_name"),
                        rs.getString("course_description")
                ));
            }
        }
        return list;
    }

    // MODIFIED METHOD: Returns a Map for easy tree building. Key = Course Name, Value = List of Student Names.
    public static Map<String, List<String>> getEnrollmentsGroupedByCourse() throws SQLException {
        // Using LinkedHashMap to maintain the order of courses as they are retrieved from the DB
        Map<String, List<String>> enrollmentMap = new LinkedHashMap<>();
        String sql = "SELECT c.course_name, s.first_name, s.last_name " +
                     "FROM enrollments e " +
                     "JOIN students s ON e.student_id = s.id " +
                     "JOIN courses c ON e.course_id = c.course_id " +
                     "ORDER BY c.course_name, s.last_name, s.first_name";
                     
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String courseName = rs.getString("course_name");
                String studentName = rs.getString("first_name") + " " + rs.getString("last_name");

                // If the course is not yet in the map, add it with a new list
                enrollmentMap.computeIfAbsent(courseName, k -> new ArrayList<>()).add(studentName);
            }
        }
        return enrollmentMap;
    }
}