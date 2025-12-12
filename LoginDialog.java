// LoginDialog.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private boolean succeeded;

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username: "), gbc);
        tfUsername = new JTextField(20);
        gbc.gridx = 1; panel.add(tfUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password: "), gbc);
        pfPassword = new JPasswordField();
        gbc.gridx = 1; panel.add(pfPassword, gbc);

        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(e -> {
            if (authenticate(getUsername(), getPassword())) {
                succeeded = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(LoginDialog.this, "Invalid username or password.", "Login Error", JOptionPane.ERROR_MESSAGE);
                pfPassword.setText("");
            }
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JPanel bp = new JPanel();
        bp.add(btnLogin);
        bp.add(btnCancel);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(bp, gbc);

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