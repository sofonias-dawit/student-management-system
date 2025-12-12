// CourseDAO.java
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public static void addCourse(Course course) throws SQLException {
        String sql = "INSERT INTO courses(course_name, course_description) VALUES (?, ?)";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.name);
            stmt.setString(2, course.description);
            stmt.executeUpdate();
        }
    }

    public static void updateCourse(Course course) throws SQLException {
        String sql = "UPDATE courses SET course_name=?, course_description=? WHERE course_id=?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.name);
            stmt.setString(2, course.description);
            stmt.setInt(3, course.id);
            stmt.executeUpdate();
        }
    }

    public static void deleteCourse(int courseId) throws SQLException {
        // Also delete associated enrollments
        String deleteEnrollmentsSql = "DELETE FROM enrollments WHERE course_id = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(deleteEnrollmentsSql)) {
            stmt.setInt(1, courseId);
            stmt.executeUpdate();
        }

        String deleteCourseSql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(deleteCourseSql)) {
            stmt.setInt(1, courseId);
            stmt.executeUpdate();
        }
    }

    public static List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_name ASC";
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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
}