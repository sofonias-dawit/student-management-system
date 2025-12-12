<<<<<<< HEAD
// StudentManagementApp.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

public class StudentManagementApp extends JFrame {

    // --- CardLayout for main content switching ---
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private static final String STUDENTS_PANEL = "Students";
    private static final String COURSES_PANEL = "Courses";
    private static final String ENROLLMENT_PANEL = "Enrollment";

    // --- UI Components ---
    private DefaultTableModel studentTableModel;
    private JTextField tfFirstName, tfLastName, tfAge, tfPhoneNumber, tfAddress, tfMainSearch;
    private JComboBox<String> comboSex;
    private DefaultTableModel courseTableModel;
    private JTextField tfCourseName;
    private JTextArea taCourseDescription;
    // NEW: Components for the scalable enrollment page
    private JTable enrollStudentTable;
    private DefaultTableModel enrollStudentTableModel;
    private JTextField tfEnrollStudentSearch;
    private JLabel enrollmentStatusLabel;
    private DefaultListModel<Course> enrolledListModel;
    private DefaultListModel<Course> availableListModel;

    // --- State Variables ---
    private int selectedStudentId = -1;
    private int selectedCourseId = -1;
    private int enrollSelectedStudentId = -1;
    private String enrollSelectedStudentName = "";

    private static final Color COLOR_HEADER = new Color(0, 102, 204);
    private static final Color COLOR_SIDEBAR = new Color(45, 52, 54);
    private static final Color COLOR_SIDEBAR_HOVER = new Color(99, 110, 114);
    private static final Color COLOR_CONTENT_BACKGROUND = new Color(240, 245, 250);
    private static final Color COLOR_PRIMARY_BUTTON = new Color(0, 123, 255);
    private static final Color COLOR_DANGER_BUTTON = new Color(220, 53, 69);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_NAV_ITEM = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TITLE_BORDER = new Font("Segoe UI", Font.BOLD, 16);

    public StudentManagementApp() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception e) { System.err.println("Nimbus L&F not found."); }

        setTitle("AMiT Student & Course Management System");
        setSize(1366, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setJMenuBar(createAppMenuBar());

    add(createHeaderPanel(), BorderLayout.NORTH);
    add(createSideNavPanel(), BorderLayout.WEST);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createSideNavPanel(), BorderLayout.WEST);
        mainContentPanel = createMainContentPanel();
        add(mainContentPanel, BorderLayout.CENTER);

        createAllTables();
        importOnStartup();
        loadStudents();
        loadCourses();
        loadStudentsForEnrollment();
    }

// In StudentManagementApp.java (add this new method)

private JMenuBar createAppMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    menuBar.setFont(FONT_LABEL);

    // --- File Menu ---
    JMenu fileMenu = new JMenu("File");
    JMenuItem importItem = new JMenuItem("Import Students from CSV...");
    JMenuItem exportItem = new JMenuItem("Export Students to CSV...");
    JMenuItem exitItem = new JMenuItem("Exit");

    importItem.addActionListener(e -> importStudents());
    exportItem.addActionListener(e -> exportStudents());
    exitItem.addActionListener(e -> System.exit(0));

    fileMenu.add(importItem);
    fileMenu.add(exportItem);
    fileMenu.addSeparator(); // Adds a line between menu items
    fileMenu.add(exitItem);

    menuBar.add(fileMenu);
    return menuBar;
}
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        headerPanel.setBackground(COLOR_HEADER);
        JLabel titleLabel = new JLabel("Student Management System");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        return headerPanel;
    }

    private JPanel createSideNavPanel() {
        JPanel sideNavPanel = new JPanel();
        sideNavPanel.setLayout(new BoxLayout(sideNavPanel, BoxLayout.Y_AXIS));
        sideNavPanel.setBackground(COLOR_SIDEBAR);
        sideNavPanel.setPreferredSize(new Dimension(240, 0));
        sideNavPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));

        JLabel navHeader = new JLabel("Navigation");
        navHeader.setFont(FONT_HEADER.deriveFont(20f));
        navHeader.setForeground(Color.WHITE);
        navHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        navHeader.setBorder(new EmptyBorder(20, 0, 20, 0));
        sideNavPanel.add(navHeader);
        sideNavPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnStudents = createNavButton("Students", "icons/students.png");
        JButton btnCourses = createNavButton("Courses", "icons/courses.png");
        JButton btnEnrollment = createNavButton("Enrollment", "icons/enrollment.png");

        btnStudents.addActionListener(e -> cardLayout.show(mainContentPanel, STUDENTS_PANEL));
        btnCourses.addActionListener(e -> cardLayout.show(mainContentPanel, COURSES_PANEL));
        btnEnrollment.addActionListener(e -> cardLayout.show(mainContentPanel, ENROLLMENT_PANEL));

        sideNavPanel.add(btnStudents);
        sideNavPanel.add(btnCourses);
        sideNavPanel.add(btnEnrollment);
        sideNavPanel.add(Box.createVerticalGlue());

        return sideNavPanel;
    }

    private JPanel createMainContentPanel() {
        cardLayout = new CardLayout();
        JPanel panel = new JPanel(cardLayout);
        panel.setBackground(COLOR_CONTENT_BACKGROUND);
        panel.add(createStudentPanel(), STUDENTS_PANEL);
        panel.add(createCoursePanel(), COURSES_PANEL);
        panel.add(createEnrollmentPanel(), ENROLLMENT_PANEL);
        return panel;
    }

    private JPanel createStudentPanel() {
        JPanel studentPanel = new JPanel(new BorderLayout(10, 10));
        studentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        studentPanel.setOpaque(false);
        studentPanel.add(createStudentFormPanel(), BorderLayout.WEST);
        studentPanel.add(createStudentTablePanel(), BorderLayout.CENTER);
        return studentPanel;
    }

    private JPanel createCoursePanel() {
        JPanel coursePanel = new JPanel(new BorderLayout(10, 10));
        coursePanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        coursePanel.setOpaque(false);
        coursePanel.add(createCourseManagementPanel(), BorderLayout.CENTER);
        return coursePanel;
    }

    private JPanel createEnrollmentPanel() {
        JPanel enrollmentPanel = new JPanel(new BorderLayout(10, 10));
        enrollmentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        enrollmentPanel.setOpaque(false);
        enrollmentPanel.add(createEnrollmentManagementPanel(), BorderLayout.CENTER);
        return enrollmentPanel;
    }

private JPanel createStudentFormPanel() {
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setPreferredSize(new Dimension(350, 0));
    formPanel.setBorder(createTitledBorder("Student Details"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    // --- Form Fields (This part is correct) ---
    tfFirstName = addFormField(formPanel, gbc, "First Name:", 0);
    tfLastName = addFormField(formPanel, gbc, "Last Name:", 1);
    tfAge = addFormField(formPanel, gbc, "Age:", 2);
    
    gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Sex:"), gbc);
    gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
    comboSex = new JComboBox<>(new String[]{"Male", "Female"});
    formPanel.add(comboSex, gbc);
    
    tfPhoneNumber = addFormField(formPanel, gbc, "Phone Number:", 4);
    tfAddress = addFormField(formPanel, gbc, "Address:", 5);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

    // 2. Create ALL THREE buttons and assign their actions
    JButton btnAddStudent = createStyledButton("Add Student", COLOR_PRIMARY_BUTTON, e -> addStudent());
    JButton btnUpdateStudent = createStyledButton("Update Student", COLOR_PRIMARY_BUTTON, e -> updateStudent());
    JButton btnClearStudentForm = createStyledButton("Clear", new Color(108, 117, 125), e -> clearStudentForm());

    // 3. Add ALL THREE buttons to the panel
    buttonPanel.add(btnAddStudent);
    buttonPanel.add(btnUpdateStudent); // <-- THIS LINE ENSURES THE BUTTON IS VISIBLE
    buttonPanel.add(btnClearStudentForm);

    // 4. Add the button panel to the main form
    gbc.gridy = 6;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.CENTER;
    formPanel.add(buttonPanel, gbc);

    return formPanel;
}

private JPanel createStudentTablePanel() {
    JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    searchPanel.add(new JLabel("Search Students:"));
    tfMainSearch = new JTextField(20);
    searchPanel.add(tfMainSearch);
    tfMainSearch.addActionListener(e -> loadStudents());
    tablePanel.add(searchPanel, BorderLayout.NORTH);

    studentTableModel = new DefaultTableModel(new String[]{"ID", "First Name", "Last Name", "Age", "Sex", "Phone", "Address"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable studentTable = new JTable(studentTableModel);
    studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // This listener populates the form when a row is clicked
    studentTable.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting() && studentTable.getSelectedRow() != -1) {
            int modelRow = studentTable.convertRowIndexToModel(studentTable.getSelectedRow());

            // Capture the ID of the selected student
            selectedStudentId = (int) studentTableModel.getValueAt(modelRow, 0);

            // Populate the form fields with data from the selected row
            tfFirstName.setText((String) studentTableModel.getValueAt(modelRow, 1));
            tfLastName.setText((String) studentTableModel.getValueAt(modelRow, 2));
            tfAge.setText(String.valueOf(studentTableModel.getValueAt(modelRow, 3)));
            comboSex.setSelectedItem(studentTableModel.getValueAt(modelRow, 4));
            tfPhoneNumber.setText((String) studentTableModel.getValueAt(modelRow, 5));
            tfAddress.setText((String) studentTableModel.getValueAt(modelRow, 6));
        }
    });
    tablePanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

    JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottomButtonPanel.add(createStyledButton("Export to CSV", new Color(23, 162, 184), e -> exportStudents()));
    bottomButtonPanel.add(createStyledButton("Delete Selected Student", COLOR_DANGER_BUTTON, e -> deleteStudent()));
    bottomButtonPanel.add(createStyledButton("Refresh List", COLOR_PRIMARY_BUTTON, e -> loadStudents()));
    tablePanel.add(bottomButtonPanel, BorderLayout.SOUTH);
    return tablePanel;
}

    private JPanel createCourseManagementPanel() {
        JPanel coursePanel = new JPanel(new BorderLayout(10, 10));
        coursePanel.setBorder(createTitledBorder("Course Management"));
        courseTableModel = new DefaultTableModel(new String[]{"ID", "Course Name", "Description"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable courseTable = new JTable(courseTableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && courseTable.getSelectedRow() != -1) {
                int row = courseTable.convertRowIndexToModel(courseTable.getSelectedRow());
                selectedCourseId = (int) courseTableModel.getValueAt(row, 0);
                tfCourseName.setText((String) courseTableModel.getValueAt(row, 1));
                taCourseDescription.setText((String) courseTableModel.getValueAt(row, 2));
            }
        });
        coursePanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JPanel courseFormPanel = new JPanel(new GridBagLayout());
        courseFormPanel.setPreferredSize(new Dimension(300, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0; gbc.gridwidth = 2; courseFormPanel.add(new JLabel("Course Name:"), gbc);
        gbc.gridy = 1; tfCourseName = new JTextField(20); courseFormPanel.add(tfCourseName, gbc);
        gbc.gridy = 2; courseFormPanel.add(new JLabel("Description:"), gbc);
        gbc.gridy = 3; taCourseDescription = new JTextArea(5, 20);
        taCourseDescription.setLineWrap(true); taCourseDescription.setWrapStyleWord(true);
        courseFormPanel.add(new JScrollPane(taCourseDescription), gbc);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createStyledButton("Add", COLOR_PRIMARY_BUTTON, e -> addCourse()));
        buttonPanel.add(createStyledButton("Update", COLOR_PRIMARY_BUTTON, e -> updateCourse()));
        buttonPanel.add(createStyledButton("Delete", COLOR_DANGER_BUTTON, e -> deleteCourse()));
        gbc.gridy = 4; courseFormPanel.add(buttonPanel, gbc);
        coursePanel.add(courseFormPanel, BorderLayout.EAST);
        return coursePanel;
    }

    private JPanel createEnrollmentManagementPanel() {
        JPanel enrollmentPanel = new JPanel(new BorderLayout(10, 10));
        enrollmentPanel.setBorder(createTitledBorder("Student Enrollment"));

        // LEFT SIDE: Searchable student list
        JPanel studentSelectionPanel = new JPanel(new BorderLayout(5, 5));
        studentSelectionPanel.setPreferredSize(new Dimension(350, 0));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search Student:"));
        tfEnrollStudentSearch = new JTextField(15);
        searchPanel.add(tfEnrollStudentSearch);
        studentSelectionPanel.add(searchPanel, BorderLayout.NORTH);

        enrollStudentTableModel = new DefaultTableModel(new String[]{"ID", "Name"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        enrollStudentTable = new JTable(enrollStudentTableModel);
        enrollStudentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentSelectionPanel.add(new JScrollPane(enrollStudentTable), BorderLayout.CENTER);

        // Add a sorter to the search table
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(enrollStudentTableModel);
        enrollStudentTable.setRowSorter(sorter);
        tfEnrollStudentSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = tfEnrollStudentSearch.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    // (?i) makes the search case-insensitive
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        enrollStudentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && enrollStudentTable.getSelectedRow() != -1) {
                int modelRow = enrollStudentTable.convertRowIndexToModel(enrollStudentTable.getSelectedRow());
                enrollSelectedStudentId = (int) enrollStudentTableModel.getValueAt(modelRow, 0);
                enrollSelectedStudentName = (String) enrollStudentTableModel.getValueAt(modelRow, 1);
                updateEnrollmentLists();
            }
        });
        
        // RIGHT SIDE: Course lists and buttons
        JPanel courseSelectionPanel = new JPanel(new BorderLayout(10, 10));
        enrollmentStatusLabel = new JLabel("Select a student from the list on the left.", SwingConstants.CENTER);
        enrollmentStatusLabel.setFont(FONT_LABEL);
        courseSelectionPanel.add(enrollmentStatusLabel, BorderLayout.NORTH);

        availableListModel = new DefaultListModel<>();
        JList<Course> availableList = new JList<>(availableListModel);
        JPanel availablePanel = new JPanel(new BorderLayout());
        availablePanel.setBorder(BorderFactory.createTitledBorder("Available Courses"));
        availablePanel.add(new JScrollPane(availableList), BorderLayout.CENTER);

        enrolledListModel = new DefaultListModel<>();
        JList<Course> enrolledList = new JList<>(enrolledListModel);
        JPanel enrolledPanel = new JPanel(new BorderLayout());
        enrolledPanel.setBorder(BorderFactory.createTitledBorder("Enrolled Courses"));
        enrolledPanel.add(new JScrollPane(enrolledList), BorderLayout.CENTER);

        JPanel buttonWrapper = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.add(createStyledButton("<< Enroll", COLOR_PRIMARY_BUTTON, e -> enrollSelectedStudent(availableList)));
        buttonPanel.add(createStyledButton("Unenroll >>", COLOR_DANGER_BUTTON, e -> unenrollSelectedStudent(enrolledList)));
        buttonWrapper.add(buttonPanel);

        JSplitPane listsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, availablePanel, enrolledPanel);
        listsSplitPane.setResizeWeight(0.5);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(listsSplitPane, BorderLayout.CENTER);
        centerPanel.add(buttonWrapper, BorderLayout.EAST);
        courseSelectionPanel.add(centerPanel, BorderLayout.CENTER);

        // Main Split Pane for the entire page
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, studentSelectionPanel, courseSelectionPanel);
        mainSplitPane.setResizeWeight(0.3);
        enrollmentPanel.add(mainSplitPane, BorderLayout.CENTER);
        return enrollmentPanel;
    }
    
    // --- Action Methods ---

    // In StudentManagementApp.java

private void addStudent() {
    // V V V V V V V V V V V V V V V V V V V V V V V V V
    //            ADD THIS CHECK AT THE TOP
    // V V V V V V V V V V V V V V V V V V V V V V V V V
    if (selectedStudentId != -1) {
        showError("A student is already selected. Please click 'Update' to modify this student, or 'Clear' to add a new one.");
        return; // Stop the method here
    }
    // ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^
    //          END OF THE NEW CODE BLOCK
    // ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^

    // The rest of the method remains the same
    if (!validateStudentInputs()) return;

    Student student = new Student(0,
        tfFirstName.getText().trim(), tfLastName.getText().trim(),
        Integer.parseInt(tfAge.getText().trim()), (String) comboSex.getSelectedItem(),
        tfPhoneNumber.getText().trim(), tfAddress.getText().trim()
    );
    try {
        if (StudentDAO.studentExists(student)) {
            showError("A student with this name and age already exists.");
            return;
        }
        StudentDAO.addStudent(student);
        loadStudents(); // This also reloads the enrollment student list
        clearStudentForm();
    } catch (SQLException e) {
        showError("Database error adding student: " + e.getMessage());
    }
}
    // In StudentManagementApp.java

private void updateStudent() {
    // Check if a student has been selected from the table first
    if (selectedStudentId == -1) {
        showError("Please select a student from the table to update.");
        return;
    }

    // Validate the data currently in the form fields
    if (!validateStudentInputs()) {
        return; // Stop if validation fails
    }

    // Create a new Student object with the data from the form fields, using the captured ID
    Student studentToUpdate = new Student(
        selectedStudentId,
        tfFirstName.getText().trim(),
        tfLastName.getText().trim(),
        Integer.parseInt(tfAge.getText().trim()),
        (String) comboSex.getSelectedItem(),
        tfPhoneNumber.getText().trim(),
        tfAddress.getText().trim()
    );

    try {
        // Call the DAO method to perform the database UPDATE
        StudentDAO.updateStudent(studentToUpdate);
        
        // Reload the student table from the database to show the changes
        loadStudents();
        
        // Let the user know it was successful
        JOptionPane.showMessageDialog(this, "Student updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

    } catch (SQLException e) {
        showError("Database error while updating student: " + e.getMessage());
        e.printStackTrace(); // Also print the full error to the console for debugging
    }
}
    
    private void deleteStudent() {
        if (selectedStudentId == -1) { showError("Please select a student to delete."); return; }
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this student?\nThis will also remove all their enrollments.", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                StudentDAO.deleteStudent(selectedStudentId);
                loadStudents();
                clearStudentForm();
                selectedStudentId = -1;
            } catch (SQLException e) { showError("Database error deleting student: " + e.getMessage()); }
        }
    }
    
    private void addCourse() {
        String name = tfCourseName.getText().trim();
        if (name.isEmpty()) { showError("Course Name cannot be empty."); return; }
        try {
            CourseDAO.addCourse(new Course(0, name, taCourseDescription.getText().trim()));
            loadCourses();
            tfCourseName.setText(""); taCourseDescription.setText("");
        } catch (SQLException e) { showError("Database error adding course: " + e.getMessage()); }
    }

    private void updateCourse() {
        if (selectedCourseId == -1) { showError("Please select a course to update."); return; }
        String name = tfCourseName.getText().trim();
        if (name.isEmpty()) { showError("Course Name cannot be empty."); return; }
        try {
            CourseDAO.updateCourse(new Course(selectedCourseId, name, taCourseDescription.getText().trim()));
            loadCourses();
        } catch (SQLException e) { showError("Database error updating course: " + e.getMessage()); }
    }
    
    private void deleteCourse() {
        if (selectedCourseId == -1) { showError("Please select a course to delete."); return; }
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this course?\nThis will unenroll all students from it.", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                CourseDAO.deleteCourse(selectedCourseId);
                loadCourses();
                tfCourseName.setText(""); taCourseDescription.setText("");
                selectedCourseId = -1;
            } catch (SQLException e) { showError("Database error deleting course: " + e.getMessage()); }
        }
    }
    
    private void enrollSelectedStudent(JList<Course> availableList) {
        Course toEnroll = availableList.getSelectedValue();
        if (enrollSelectedStudentId == -1 || toEnroll == null) { showError("Please select a student and an available course to enroll."); return; }
        try {
            EnrollmentDAO.enrollStudent(enrollSelectedStudentId, toEnroll.id);
            updateEnrollmentLists();
        } catch (SQLException e) { showError("Database error enrolling student: " + e.getMessage()); }
    }
    
    private void unenrollSelectedStudent(JList<Course> enrolledList) {
        Course toUnenroll = enrolledList.getSelectedValue();
        if (enrollSelectedStudentId == -1 || toUnenroll == null) { showError("Please select a student and an enrolled course to unenroll."); return; }
        try {
            EnrollmentDAO.unenrollStudent(enrollSelectedStudentId, toUnenroll.id);
            updateEnrollmentLists();
        } catch (SQLException e) { showError("Database error unenrolling student: " + e.getMessage()); }
    }

    // --- Data Loading and UI Update Methods ---

    private void loadStudents() {
        try {
            List<Student> students = StudentDAO.searchStudents(tfMainSearch != null ? tfMainSearch.getText().trim() : "");
            studentTableModel.setRowCount(0);
            for (Student s : students) {
                studentTableModel.addRow(new Object[]{s.id, s.firstName, s.lastName, s.age, s.sex, s.phoneNumber, s.address});
            }
            loadStudentsForEnrollment();
        } catch (SQLException e) { showError("Error loading students: " + e.getMessage()); }
    }
    
    private void loadStudentsForEnrollment() {
        try {
            List<Student> students = StudentDAO.searchStudents(""); // Get all students
            enrollStudentTableModel.setRowCount(0);
            for (Student s : students) {
                enrollStudentTableModel.addRow(new Object[]{s.id, s.firstName + " " + s.lastName});
            }
        } catch (SQLException e) { showError("Error loading students for enrollment tab: " + e.getMessage()); }
    }

    private void loadCourses() {
        try {
            List<Course> courses = CourseDAO.getAllCourses();
            courseTableModel.setRowCount(0);
            for (Course c : courses) {
                courseTableModel.addRow(new Object[]{c.id, c.name, c.description});
            }
            updateEnrollmentLists();
        } catch (SQLException e) { showError("Error loading courses: " + e.getMessage()); }
    }

    private void updateEnrollmentLists() {
        if (enrollSelectedStudentId == -1) {
            enrollmentStatusLabel.setText("Select a student from the list on the left.");
            enrolledListModel.clear();
            availableListModel.clear();
            return;
        }
        
        enrollmentStatusLabel.setText("Managing enrollments for: " + enrollSelectedStudentName);
        
        try {
            List<Course> allCourses = CourseDAO.getAllCourses();
            List<Course> enrolledCourses = EnrollmentDAO.getEnrolledCoursesForStudent(enrollSelectedStudentId);
            enrolledListModel.clear();
            enrolledCourses.forEach(enrolledListModel::addElement);
            
            List<Integer> enrolledIds = enrolledCourses.stream().map(c -> c.id).collect(Collectors.toList());
            List<Course> availableCourses = allCourses.stream().filter(c -> !enrolledIds.contains(c.id)).collect(Collectors.toList());
            availableListModel.clear();
            availableCourses.forEach(availableListModel::addElement);
            
        } catch (SQLException e) { showError("Error updating enrollment lists: " + e.getMessage()); }
    }
    

private void clearStudentForm() {
    // Clear all the text fields and reset the combo box
    tfFirstName.setText("");
    tfLastName.setText("");
    tfAge.setText("");
    comboSex.setSelectedIndex(0);
    tfPhoneNumber.setText("");
    tfAddress.setText("");

    // A more robust way to clear the table selection would be to make the JTable a class member.
    // For now, this just resets the state variable which is the most important part.
    
    // Reset the selected student ID to -1 (nothing selected)
    selectedStudentId = -1;
}
    
    private boolean validateStudentInputs() {
        if (tfFirstName.getText().trim().isEmpty() || tfLastName.getText().trim().isEmpty()) { showError("First and Last Name cannot be empty."); return false; }
        try { Integer.parseInt(tfAge.getText().trim()); }
        catch (NumberFormatException e) { showError("Age must be a valid number."); return false; }
        return true;
    }

    private JTextField addFormField(JPanel p, GridBagConstraints g, String l, int y) {
        g.gridx = 0; g.gridy = y; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel(l), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
        JTextField tf = new JTextField(15);
        p.add(tf, g);
        g.weightx = 0;
        return tf;
    }
    
    private TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), " " + title + " ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_TITLE_BORDER, Color.DARK_GRAY);
    }
    
    private JButton createNavButton(String text, String iconPath) {
        ImageIcon icon = new ImageIcon(new ImageIcon(iconPath).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JButton button = new JButton(text, icon);
        button.setFont(FONT_NAV_ITEM);
        button.setForeground(Color.WHITE);
        button.setBackground(COLOR_SIDEBAR);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(15, 25, 15, 25));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height + 10));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(COLOR_SIDEBAR_HOVER); }
            public void mouseExited(MouseEvent e) { button.setBackground(COLOR_SIDEBAR); }
        });
        return button;
    }

    private JButton createStyledButton(String text, Color bgColor, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(FONT_LABEL);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        return button;
    }

    private void showError(String message) { JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE); }


private void importStudents() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Import Students from CSV file");
    int userSelection = fileChooser.showOpenDialog(this);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File fileToImport = fileChooser.getSelectedFile();
        try {
            List<Student> studentsFromFile = FileHandler.importStudentsFromFile(fileToImport.getAbsolutePath());
=======
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
>>>>>>> c4c4bf50ef4773e8f246d049ab10552215c61efe
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
<<<<<<< HEAD
            loadStudents(); // Refresh the student table to show new data
            JOptionPane.showMessageDialog(this,
                    "Import completed.\nAdded: " + countAdded + " new students.\nSkipped (duplicates): " + countSkipped,
                    "Import Result", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | SQLException ex) {
            showError("Error during import: " + ex.getMessage());
        }
    }
}

private void exportStudents() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Export Students to CSV");
    fileChooser.setSelectedFile(new File("students_export.csv"));

    int userSelection = fileChooser.showSaveDialog(this);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File fileToSave = fileChooser.getSelectedFile();
        String filePath = fileToSave.getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".csv")) {
            fileToSave = new File(filePath + ".csv");
        }

        try {
            List<Student> allStudents = StudentDAO.searchStudents(""); // Get all students
            FileHandler.exportStudentsToCSV(allStudents, fileToSave.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Students exported successfully to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | SQLException ex) {
            showError("Error exporting students: " + ex.getMessage());
        }
    }
}


    private void createAllTables() {
        try (Connection conn = DB.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS students (id INT PRIMARY KEY AUTO_INCREMENT, first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100) NOT NULL, age INT, sex VARCHAR(10), phone_number VARCHAR(20), address VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS courses (course_id INT PRIMARY KEY AUTO_INCREMENT, course_name VARCHAR(100) NOT NULL UNIQUE, course_description TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS enrollments (enrollment_id INT PRIMARY KEY AUTO_INCREMENT, student_id INT, course_id INT, UNIQUE(student_id, course_id), FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE, FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE)");
            System.out.println("All tables checked/created successfully.");
        } catch (SQLException e) {
            showError("Fatal Database Error: " + e.getMessage()); e.printStackTrace(); System.exit(1);
        }
    }

    private void importOnStartup() {
        try {
            List<Student> studentsFromFile = FileHandler.importStudentsFromFile("students.csv");
            int countAdded = 0;
            for (Student s : studentsFromFile) {
                if (!StudentDAO.studentExists(s)) {
                    StudentDAO.addStudent(s);
                    countAdded++;
                }
            }
            if (countAdded > 0) System.out.println("Imported " + countAdded + " new students from students.csv.");
        } catch (IOException e) {
            System.out.println("Startup import file 'students.csv' not found. Skipping.");
        } catch (Exception e) {
            System.err.println("Error during startup import: " + e.getMessage());
=======
            System.out.println("Startup import completed. Added: " + countAdded + ", Skipped (duplicates): " + countSkipped);
        } catch (IOException | SQLException ex) {
            System.err.println("Error during startup import: " + ex.getMessage());
>>>>>>> c4c4bf50ef4773e8f246d049ab10552215c61efe
        }
    }

    public static void main(String[] args) {
        LoginDialog loginDialog = new LoginDialog(null);
<<<<<<< HEAD
        loginDialog.setVisible(true);
        if (loginDialog.isSucceeded()) {
            SwingUtilities.invokeLater(() -> new StudentManagementApp().setVisible(true));
        } else {
           System.exit(0);
=======
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
>>>>>>> c4c4bf50ef4773e8f246d049ab10552215c61efe
        }
    }
}