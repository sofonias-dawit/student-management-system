// Course.java
public class Course {
    int id;
    String name;
    String description;

    public Course(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // This is crucial for displaying the course name in a JList or JComboBox
    @Override
    public String toString() {
        return this.name;
    }
}