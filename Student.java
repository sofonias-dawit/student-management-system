// Student.java
public class Student {
    int id;
    int age;
    String firstName;
    String lastName;
    String sex;
    String phoneNumber;
    String address;

    public Student(int id, String firstName, String lastName, int age, String sex, String phoneNumber, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.sex = sex;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // This helps display the student's name nicely in UI components
    @Override
    public String toString() {
        return id + ": " + firstName + " " + lastName;
    }
}