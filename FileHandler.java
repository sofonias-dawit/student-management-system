import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    public static List<Student> importStudentsFromFile(String filename) throws IOException {
        List<Student> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    try {
                        String firstName = parts[0].trim();
                        String lastName = parts[1].trim();
                        int age = Integer.parseInt(parts[2].trim());
                        String sex = parts[3].trim();
                        String phone = parts[4].trim();
                        String address = parts[5].trim();
                        list.add(new Student(0, firstName, lastName, age, sex, phone, address));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping line " + lineNumber + " due to invalid age format: " + line);
                    }
                } else {
                    System.err.println("Skipping line " + lineNumber + " due to incorrect number of fields (expected 6): " + line);
                }
            }
        }
        return list;
    }

    public static void exportStudentsToCSV(List<Student> students, String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            // Write the CSV header first
            bw.write("FirstName,LastName,Age,Sex,PhoneNumber,Address");
            bw.newLine();

            for (Student s : students) {
               
                String[] data = {
                    "\"" + s.firstName.replace("\"", "\"\"") + "\"",
                    "\"" + s.lastName.replace("\"", "\"\"") + "\"",
                    String.valueOf(s.age),
                    "\"" + s.sex.replace("\"", "\"\"") + "\"",
                    "\"" + s.phoneNumber.replace("\"", "\"\"") + "\"",
                    "\"" + s.address.replace("\"", "\"\"") + "\""
                };
                bw.write(String.join(",", data));
                bw.newLine();
            }
        }
    }
}