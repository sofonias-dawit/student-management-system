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
        }
    }

    public static void main(String[] args) {
        LoginDialog loginDialog = new LoginDialog(null);
        loginDialog.setVisible(true);
        if (loginDialog.isSucceeded()) {
            SwingUtilities.invokeLater(() -> new StudentManagementApp().setVisible(true));
        } else {
           System.exit(0);
        }
    }
}