import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
//import javax.swing.event.ListSelectionEvent;
//import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// =================================================================================
// LOGIN DIALOG CLASS
// =================================================================================
class LoginDialog extends JDialog {
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JButton btnLogin, btnCancel;
    private boolean succeeded;
    private static final Color COLOR_PRIMARY = new Color(0, 102, 204);
    private static final Color COLOR_BACKGROUND = new Color(240, 245, 250);
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        JLabel lbUsername = new JLabel("Username: ");
        lbUsername.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(lbUsername, gbc);
        tfUsername = new JTextField(20);
        tfUsername.setFont(FONT_LABEL);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(tfUsername, gbc);
        JLabel lbPassword = new JLabel("Password: ");
        lbPassword.setFont(FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(lbPassword, gbc);
        pfPassword = new JPasswordField();
        pfPassword.setFont(FONT_LABEL);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(pfPassword, gbc);
        btnLogin = new JButton("Login");
        btnLogin.setFont(FONT_BUTTON);
        btnLogin.setBackground(COLOR_PRIMARY);
        btnLogin.setForeground(COLOR_TEXT_LIGHT);
        btnCancel = new JButton("Cancel");
        btnCancel.setFont(FONT_BUTTON);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.setBackground(COLOR_BACKGROUND);
        bp.add(btnLogin);
        bp.add(btnCancel);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        panel.add(bp, gbc);
        btnLogin.addActionListener(e -> {
            if (authenticate(getUsername(), getPassword())) {
                succeeded = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(LoginDialog.this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
                pfPassword.setText("");
                succeeded = false;
            }
        });
        btnCancel.addActionListener(e -> {
            succeeded = false;
            dispose();
        });
        pfPassword.addActionListener(e -> btnLogin.doClick());
        getContentPane().add(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public String getUsername() { return tfUsername.getText().trim(); }
    public String getPassword() { return new String(pfPassword.getPassword()); }
    public boolean isSucceeded() { return succeeded; }
    private boolean authenticate(String username, String password) { return username.equals("admin") && password.equals("123"); }
}

// =================================================================================
// MAIN APPLICATION CLASS
// =================================================================================

public class StudentManagementApp extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfFirstName, tfLastName, tfAge, tfCourse, tfPhoneNumber, tfAddress, tfSearch;
    private JComboBox<String> comboSex;
    private JButton btnAdd, btnUpdate, btnDelete, btnSearch, btnRefresh, btnClear;

    private int selectedStudentId = -1;

    private static final Color COLOR_PRIMARY = new Color(0, 102, 204);
    private static final Color COLOR_SECONDARY = new Color(70, 130, 180);
    private static final Color COLOR_BACKGROUND = new Color(240, 245, 250);
    private static final Color COLOR_PANEL_BACKGROUND = Color.WHITE;
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;
    private static final Color COLOR_TEXT_DARK = new Color(50, 50, 50);
    private static final Color COLOR_BUTTON_HOVER = new Color(0, 82, 184);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TEXT_FIELD = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 13);


    public StudentManagementApp() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        setTitle("AMiT Student Management System");
        setSize(1200, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); //center the window
        getContentPane().setBackground(COLOR_BACKGROUND);

        setJMenuBar(createMenuBar());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setOpaque(false);

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createFormPanel(), BorderLayout.WEST);
        mainPanel.add(createTableAndSearchPanel(), BorderLayout.CENTER);

        add(mainPanel);

        createTableIfNotExists();
        importOnStartup();
        loadStudents();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(COLOR_PANEL_BACKGROUND);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(FONT_LABEL);
        JMenuItem importItem = new JMenuItem("Import from TXT");
        importItem.setFont(FONT_LABEL);
        importItem.addActionListener(e -> importFromFile());
        JMenuItem exportItem = new JMenuItem("Export to TXT");
        exportItem.setFont(FONT_LABEL);
        exportItem.addActionListener(e -> exportToFile());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setFont(FONT_LABEL);
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(importItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        return menuBar;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(COLOR_PRIMARY);
        JLabel lblTitle = new JLabel("Student Management System");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_LIGHT);
        headerPanel.add(lblTitle);
        headerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        return headerPanel;
    }

    private JPanel createFormPanel() {
        JPanel formContainerPanel = new JPanel(new BorderLayout(10, 10));
        formContainerPanel.setBackground(COLOR_PANEL_BACKGROUND);
        formContainerPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 10, 10, 10)
        ));
        formContainerPanel.setPreferredSize(new Dimension(350, 0));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_PRIMARY, 1),
                " Student Details ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_LABEL, COLOR_PRIMARY);
        fieldsPanel.setBorder(BorderFactory.createCompoundBorder(titledBorder, new EmptyBorder(10, 5, 10, 5)));

        tfFirstName = addFormField(fieldsPanel, gbc, "First Name:", 0);
        tfLastName = addFormField(fieldsPanel, gbc, "Last Name:", 1);
        tfAge = addFormField(fieldsPanel, gbc, "Age:", 2);
        
        gbc.gridx = 0; gbc.gridy = 3;
        fieldsPanel.add(createStyledLabel("Sex:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        comboSex = new JComboBox<>(new String[]{"Male", "Female"});
        comboSex.setFont(FONT_TEXT_FIELD);
        fieldsPanel.add(comboSex, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        tfCourse = addFormField(fieldsPanel, gbc, "Course:", 4);
        tfPhoneNumber = addFormField(fieldsPanel, gbc, "Phone Number:", 5);
        tfAddress = addFormField(fieldsPanel, gbc, "Address:", 6);

        formContainerPanel.add(fieldsPanel, BorderLayout.NORTH);

        JPanel formActionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        formActionPanel.setOpaque(false);
        btnAdd = createStyledButton("Add Student");
        btnAdd.addActionListener(e -> addStudent());
        btnUpdate = createStyledButton("Update Student");
        btnUpdate.addActionListener(e -> updateStudent());
        btnClear = createStyledButton("Clear Fields");
        btnClear.setBackground(new Color(220, 53, 69));
        btnClear.addActionListener(e -> clearFields());
        formActionPanel.add(btnAdd);
        formActionPanel.add(btnUpdate);
        formActionPanel.add(btnClear);

        formContainerPanel.add(formActionPanel, BorderLayout.CENTER);
        return formContainerPanel;
    }

    private JTextField addFormField(JPanel panel, GridBagConstraints gbc, String label, int yPos) {
        gbc.gridx = 0;
        gbc.gridy = yPos;
        panel.add(createStyledLabel(label), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField textField = createStyledTextField();
        panel.add(textField, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        return textField;
    }

    private JPanel createTableAndSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel lblSearch = createStyledLabel("Search (ID, Name, or Course):");
        tfSearch = createStyledTextField();
        tfSearch.setPreferredSize(new Dimension(200, tfSearch.getPreferredSize().height));
        btnSearch = createStyledButton("Search");
        btnSearch.addActionListener(e -> searchStudent());
        tfSearch.addActionListener(e -> searchStudent());
        searchPanel.add(lblSearch);
        searchPanel.add(tfSearch);
        searchPanel.add(btnSearch);
        panel.add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"ID", "First Name", "Last Name", "Age", "Sex", "Course", "Phone", "Address"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(25);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                populateFieldsFromSelectedRow();
            }
        });

        table.setGridColor(Color.LIGHT_GRAY);
        table.setSelectionBackground(COLOR_SECONDARY);
        table.setSelectionForeground(COLOR_TEXT_LIGHT);
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(FONT_TABLE_HEADER);
        tableHeader.setBackground(COLOR_PRIMARY);
        tableHeader.setForeground(COLOR_TEXT_LIGHT);
        tableHeader.setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(COLOR_PANEL_BACKGROUND);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel tableActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        tableActionsPanel.setOpaque(false);
        btnDelete = createStyledButton("Delete Selected");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.addActionListener(e -> deleteStudent());
        btnRefresh = createStyledButton("Refresh List");
        btnRefresh.addActionListener(e -> loadStudents());
        tableActionsPanel.add(btnDelete);
        tableActionsPanel.add(btnRefresh);
        panel.add(tableActionsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_TEXT_DARK);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(15);
        textField.setFont(FONT_TEXT_FIELD);
        textField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
        return textField;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(COLOR_TEXT_LIGHT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(COLOR_BUTTON_HOVER);
            }

            public void mouseExited(MouseEvent evt) {
                if (button == btnClear || button == btnDelete) {
                    button.setBackground(new Color(220, 53, 69));
                } else {
                    button.setBackground(COLOR_PRIMARY);
                }
            }
        });
        return button;
    }

    private void populateFieldsFromSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            selectedStudentId = (int) tableModel.getValueAt(selectedRow, 0);
            tfFirstName.setText(tableModel.getValueAt(selectedRow, 1).toString());
            tfLastName.setText(tableModel.getValueAt(selectedRow, 2).toString());
            tfAge.setText(tableModel.getValueAt(selectedRow, 3).toString());
            comboSex.setSelectedItem(tableModel.getValueAt(selectedRow, 4).toString());
            tfCourse.setText(tableModel.getValueAt(selectedRow, 5).toString());
            tfPhoneNumber.setText(tableModel.getValueAt(selectedRow, 6).toString());
            tfAddress.setText(tableModel.getValueAt(selectedRow, 7).toString());
            
            btnUpdate.setEnabled(true);
            btnDelete.setEnabled(true);
        } else {
            selectedStudentId = -1;
            btnUpdate.setEnabled(false);
            btnDelete.setEnabled(false);
        }
    }

    private void clearFields() {
        tfFirstName.setText("");
        tfLastName.setText("");
        tfAge.setText("");
        comboSex.setSelectedIndex(0);
        tfCourse.setText("");
        tfPhoneNumber.setText("");
        tfAddress.setText("");
        tfSearch.setText("");
        table.clearSelection();
        selectedStudentId = -1;
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        tfFirstName.requestFocus();
    }

    private void createTableIfNotExists() {
        try (Connection conn = DB.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS students ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "first_name VARCHAR(100) NOT NULL,"
                    + "last_name VARCHAR(100) NOT NULL,"
                    + "age INT,"
                    + "sex VARCHAR(10),"
                    + "course VARCHAR(100),"
                    + "phone_number VARCHAR(20),"
                    + "address VARCHAR(255),"
                    + "UNIQUE KEY unique_student (first_name, last_name, age, course)"
                    + ")");
            System.out.println("Table 'students' checked/created successfully.");
        } catch (SQLException ex) {
            showError("Database Error: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private void loadStudents() {
        try {
            List<Student> students = StudentDAO.getAllStudents();
            tableModel.setRowCount(0);
            for (Student s : students) {
                tableModel.addRow(new Object[]{s.id, s.firstName, s.lastName, s.age, s.sex, s.course, s.phoneNumber, s.address});
            }
            clearFields();
        } catch (SQLException ex) {
            showError("Error loading students: " + ex.getMessage());
        }
    }

    private boolean validateInputs() {
        if (tfFirstName.getText().trim().isEmpty()) { showError("First Name cannot be empty."); tfFirstName.requestFocus(); return false; }
        if (tfLastName.getText().trim().isEmpty()) { showError("Last Name cannot be empty."); tfLastName.requestFocus(); return false; }
        if (tfCourse.getText().trim().isEmpty()) { showError("Course cannot be empty."); tfCourse.requestFocus(); return false; }
        if (tfPhoneNumber.getText().trim().isEmpty()) { showError("Phone Number cannot be empty."); tfPhoneNumber.requestFocus(); return false; }
        if (tfAddress.getText().trim().isEmpty()) { showError("Address cannot be empty."); tfAddress.requestFocus(); return false; }
        
        try {
            if (tfAge.getText().trim().isEmpty()) { showError("Age cannot be empty."); tfAge.requestFocus(); return false; }
            int age = Integer.parseInt(tfAge.getText().trim());
            if (age <= 0 || age > 120) { showError("Please enter a valid age."); tfAge.requestFocus(); return false; }
        } catch (NumberFormatException e) {
            showError("Age must be a valid number."); tfAge.requestFocus(); return false;
        }
        return true;
    }

    private void addStudent() {
        if (!validateInputs()) return;
        try {
            String firstName = tfFirstName.getText().trim();
            String lastName = tfLastName.getText().trim();
            int age = Integer.parseInt(tfAge.getText().trim());
            String sex = (String) comboSex.getSelectedItem();
            String course = tfCourse.getText().trim();
            String phoneNumber = tfPhoneNumber.getText().trim();
            String address = tfAddress.getText().trim();

            Student s = new Student(0, firstName, lastName, age, sex, course, phoneNumber, address);

            if (StudentDAO.studentExists(s)) {
                JOptionPane.showMessageDialog(this, "A student with these details already exists.", "Duplicate Record", JOptionPane.WARNING_MESSAGE);
                return;
            }

            StudentDAO.addStudent(s);
            loadStudents();
            JOptionPane.showMessageDialog(this, "Student added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) {
                showError("Error adding student: This student already exists.");
            } else {
                showError("Error adding student: " + ex.getMessage());
            }
        } catch (Exception ex) {
            showError("An unexpected error occurred: " + ex.getMessage());
        }
    }

    private void updateStudent() {
        if (selectedStudentId == -1) {
            showError("Please select a student from the table to update.");
            return;
        }
        if (!validateInputs()) return;

        try {
            String firstName = tfFirstName.getText().trim();
            String lastName = tfLastName.getText().trim();
            int age = Integer.parseInt(tfAge.getText().trim());
            String sex = (String) comboSex.getSelectedItem();
            String course = tfCourse.getText().trim();
            String phoneNumber = tfPhoneNumber.getText().trim();
            String address = tfAddress.getText().trim();

            Student s = new Student(selectedStudentId, firstName, lastName, age, sex, course, phoneNumber, address);
            StudentDAO.updateStudent(s);
            loadStudents();
            JOptionPane.showMessageDialog(this, "Student updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            showError("Error updating student: " + ex.getMessage());
        } catch (Exception ex) {
            showError("An unexpected error occurred: " + ex.getMessage());
        }
    }

    private void deleteStudent() {
        if (selectedStudentId == -1) {
            showError("Please select a student from the table to delete.");
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student " + tfFirstName.getText() + " " + tfLastName.getText() + " (ID: " + selectedStudentId + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                StudentDAO.deleteStudent(selectedStudentId);
                loadStudents();
                JOptionPane.showMessageDialog(this, "Student deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                showError("Error deleting student: " + ex.getMessage());
            }
        }
    }

    private void searchStudent() {
        String keyword = tfSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadStudents();
            return;
        }
        try {
            List<Student> students = StudentDAO.searchStudent(keyword);
            tableModel.setRowCount(0);
            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students found matching '" + keyword + "'.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                for (Student s : students) {
                    tableModel.addRow(new Object[]{s.id, s.firstName, s.lastName, s.age, s.sex, s.course, s.phoneNumber, s.address});
                }
            }
            clearFields();
        } catch (SQLException ex) {
            showError("Error searching students: " + ex.getMessage());
        }
    }

    private void importFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select TXT file to import");
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToImport = fileChooser.getSelectedFile();
            try {
                List<Student> studentsFromFile = FileHandler.importFromFile(fileToImport.getAbsolutePath());
                int countAdded = 0;
                int countSkipped = 0;
                for (Student s : studentsFromFile) {
                    if (!StudentDAO.studentExists(s)) {
                        StudentDAO.addStudent(s);
                        countAdded++;
                    } else {
                        countSkipped++;
                    }
                }
                loadStudents();
                JOptionPane.showMessageDialog(this, "Import completed.\nAdded: " + countAdded + " new records.\nSkipped (duplicates): " + countSkipped + " records.", "Import Result", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | SQLException ex) {
                showError("Error during import: " + ex.getMessage());
            }
        }
    }

    private void exportToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save students to TXT file");
        fileChooser.setSelectedFile(new File("students_export.txt"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".txt");
            }
            try {
                List<Student> students = StudentDAO.getAllStudents();
                FileHandler.exportToFile(fileToSave.getAbsolutePath(), students);
                JOptionPane.showMessageDialog(this, "Exported successfully to " + fileToSave.getName(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | SQLException ex) {
                showError("Error during export: " + ex.getMessage());
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void importOnStartup() {
        final String STARTUP_IMPORT_FILE = "students.txt";
        File importFile = new File(STARTUP_IMPORT_FILE);

        if (!importFile.exists()) {
            System.out.println("Startup import file '" + STARTUP_IMPORT_FILE + "' not found. Skipping import.");
            return;
        }

        System.out.println("Found '" + STARTUP_IMPORT_FILE + "'. Starting automatic import...");
        try {
            List<Student> studentsFromFile = FileHandler.importFromFile(importFile.getAbsolutePath());
            int countAdded = 0;
            int countSkipped = 0;
            for (Student s : studentsFromFile) {
                if (!StudentDAO.studentExists(s)) {
                    StudentDAO.addStudent(s);
                    countAdded++;
                } else {
                    countSkipped++;
                }
            }
            System.out.println("Startup import completed. Added: " + countAdded + ", Skipped (duplicates): " + countSkipped);
        } catch (IOException | SQLException ex) {
            System.err.println("Error during startup import: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        LoginDialog loginDialog = new LoginDialog(null);
    loginDialog.setVisible(true);

        if (loginDialog.isSucceeded()) {
            SwingUtilities.invokeLater(() -> {
                StudentManagementApp app = new StudentManagementApp();
                app.setVisible(true);
            });
        } else {
            System.exit(0);
        }
    }

    // =====================================================================
    // ===== Supporting Classes =====
    // =====================================================================

    static class Student {
        int id, age;
        String firstName, lastName, sex, course, phoneNumber, address;

        public Student(int id, String firstName, String lastName, int age, String sex, String course, String phoneNumber, String address) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.sex = sex;
            this.course = course;
            this.phoneNumber = phoneNumber;
            this.address = address;
        }
    }

    static class StudentDAO {
        public static void addStudent(Student s) throws SQLException {
            String sql = "INSERT INTO students(first_name, last_name, age, sex, course, phone_number, address) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, s.firstName);
                stmt.setString(2, s.lastName);
                stmt.setInt(3, s.age);
                stmt.setString(4, s.sex);
                stmt.setString(5, s.course);
                stmt.setString(6, s.phoneNumber);
                stmt.setString(7, s.address);
                stmt.executeUpdate();
            }
        }

        public static void updateStudent(Student s) throws SQLException {
            String sql = "UPDATE students SET first_name=?, last_name=?, age=?, sex=?, course=?, phone_number=?, address=? WHERE id=?";
            try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, s.firstName);
                stmt.setString(2, s.lastName);
                stmt.setInt(3, s.age);
                stmt.setString(4, s.sex);
                stmt.setString(5, s.course);
                stmt.setString(6, s.phoneNumber);
                stmt.setString(7, s.address);
                stmt.setInt(8, s.id);
                stmt.executeUpdate();
            }
        }

        public static void deleteStudent(int id) throws SQLException {
            String sql = "DELETE FROM students WHERE id=?";
            try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
        }

        public static List<Student> getAllStudents() throws SQLException {
            List<Student> list = new ArrayList<>();
            String sql = "SELECT * FROM students ORDER BY first_name ASC";
            try (Connection conn = DB.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),
                            rs.getInt("age"), rs.getString("sex"), rs.getString("course"),
                            rs.getString("phone_number"), rs.getString("address")));
                }
            }
            return list;
        }

        public static List<Student> searchStudent(String keyword) throws SQLException {
            List<Student> list = new ArrayList<>();
            String sql = "SELECT * FROM students WHERE first_name LIKE ? OR last_name LIKE ? OR course LIKE ? OR CAST(id AS CHAR) LIKE ?";
            try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                String likeKeyword = "%" + keyword + "%";
                stmt.setString(1, likeKeyword);
                stmt.setString(2, likeKeyword);
                stmt.setString(3, likeKeyword);
                stmt.setString(4, keyword + "%");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                     list.add(new Student(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),
                            rs.getInt("age"), rs.getString("sex"), rs.getString("course"),
                            rs.getString("phone_number"), rs.getString("address")));
                }
            }
            return list;
        }

        public static boolean studentExists(Student s) throws SQLException {
            String sql = "SELECT COUNT(*) FROM students WHERE first_name=? AND last_name=? AND age=? AND course=?";
            try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, s.firstName);
                stmt.setString(2, s.lastName);
                stmt.setInt(3, s.age);
                stmt.setString(4, s.course);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    static class FileHandler {
        public static List<Student> importFromFile(String filename) throws IOException {
            List<Student> list = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line;
                int lineNumber = 0;
                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length == 7) {
                        try {
                            String firstName = parts[0].trim();
                            String lastName = parts[1].trim();
                            int age = Integer.parseInt(parts[2].trim());
                            String sex = parts[3].trim();
                            String course = parts[4].trim();
                            String phone = parts[5].trim();
                            String address = parts[6].trim();
                            list.add(new Student(0, firstName, lastName, age, sex, course, phone, address));
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping line " + lineNumber + " due to invalid age format: " + line);
                        }
                    } else {
                        System.err.println("Skipping line " + lineNumber + " due to incorrect number of fields (expected 7): " + line);
                    }
                }
            }
            return list;
        }

        public static void exportToFile(String filename, List<Student> students) throws IOException {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
                for (Student s : students) {
                    bw.write(s.firstName + "," + s.lastName + "," + s.age + "," + s.sex + "," + s.course + "," + s.phoneNumber + "," + s.address);
                    bw.newLine();
                }
            }
        }
    }

    static class DB {
        private static final String URL = "jdbc:mysql://localhost:3306/amitdb?useSSL=false&serverTimezone=UTC";
        private static final String USER = "root";
        private static final String PASSWORD = "";
        public static Connection getConnection() throws SQLException {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found.", e);
            }
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }
}