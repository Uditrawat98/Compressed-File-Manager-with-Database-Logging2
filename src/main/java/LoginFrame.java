//To run the Project open Command Prompt and run:
// cd C:\Users\HP\OneDrive\Desktop\compression run_with_password.bat
// or from folder run_with_password.bat

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    // MySQL - password from -Ddb.password=xxx, or env var DB_PASSWORD, or empty
    private static final String DB_URL = System.getProperty(
            "db.url",
            "jdbc:mysql://localhost:3306/compressiondb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    );
    private static final String DB_USER = System.getProperty("db.user", "root");
    private static final String DB_PASSWORD = getDbPassword();

    private static String getDbPassword() {
        String p = System.getProperty("db.password");
        if (p != null && !p.isEmpty()) return p;
        p = System.getenv("DB_PASSWORD");
        return (p != null) ? p : "";
    }

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;

    public LoginFrame() {
        initComponents();
        setTitle("Login - Huffman Encoding App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(31, 40, 51));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Huffman Encoder Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainPanel.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(18);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(18);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        loginButton = new JButton("Login");
        cancelButton = new JButton("Cancel");

        styleButton(loginButton);
        styleButton(cancelButton);

        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainPanel.add(buttonPanel, gbc);

        getContentPane().add(mainPanel);
        pack();

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Serif", Font.ITALIC, 16));
        button.setBackground(new Color(0, 140, 186));
        button.setForeground(Color.WHITE);
        button.setFocusable(false);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (authenticateUser(username, password)) {
            JOptionPane.showMessageDialog(this,
                    "Login successful. Welcome " + username + "!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Open main application window after successful login
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new EncoderGUI().setVisible(true);
                }
            });

            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean authenticateUser(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            rs = stmt.executeQuery();
            return rs.next();
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "MySQL JDBC driver not found.\nAdd mysql-connector-j to the classpath.",
                    "Driver Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to connect to database:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException ignored) {}
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignored) {}
            try {
                if (conn != null) conn.close();
            } catch (SQLException ignored) {}
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
