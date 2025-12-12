public class EnrollmentDetail {
    private final String studentName;
    private final String courseName;

    public EnrollmentDetail(String studentName, String courseName) {
        this.studentName = studentName;
        this.courseName = courseName;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getCourseName() {
        return courseName;
    }
}