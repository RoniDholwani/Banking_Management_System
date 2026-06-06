package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BankingGUI extends JFrame {
    private static final String url = "jdbc:mysql://localhost:3306/banking_system";
    private static final String username = "root";
    private static final String password = "ronit6071";

    private Connection connection;
    private User user;
    private Accounts accounts;
    private AccountManager accountManager;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private String loggedInEmail = null;
    private long accountNumber = 0;

    public BankingGUI() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            user = new User(connection, null);
            accounts = new Accounts(connection, null);
            accountManager = new AccountManager(connection, null);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Connection Failed: " + e.getMessage());
            System.exit(1);
        }

        setTitle("Banking System");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createHomePanel(), "home");
        mainPanel.add(createRegisterPanel(), "register");
        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createDashboardPanel(), "dashboard");
        mainPanel.add(createAccountPanel(), "account");

        add(mainPanel);
        setLocationRelativeTo(null);
        cardLayout.show(mainPanel, "home");
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        JLabel label = new JLabel("*** WELCOME TO BANKING SYSTEM ***", SwingConstants.CENTER);

        JButton registerBtn = new JButton("Register");
        JButton loginBtn = new JButton("Login");

        registerBtn.addActionListener(e -> cardLayout.show(mainPanel, "register"));
        loginBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        panel.add(label);
        panel.add(registerBtn);
        panel.add(loginBtn);
        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JButton registerBtn = new JButton("Register");
        JButton backBtn = new JButton("Back");

        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);
        panel.add(registerBtn);
        panel.add(backBtn);

        registerBtn.addActionListener((ActionEvent e) -> {
            boolean ok = user.registerGUI(nameField.getText(), emailField.getText(), new String(passField.getPassword()));
            JOptionPane.showMessageDialog(this, ok ? "Registration Successful" : "Failed / Already Exists");
            if (ok) cardLayout.show(mainPanel, "home");
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "home"));

        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JTextField emailField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JButton loginBtn = new JButton("Login");
        JButton backBtn = new JButton("Back");

        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);
        panel.add(loginBtn);
        panel.add(backBtn);

        loginBtn.addActionListener(e -> {
            String email = user.loginGUI(emailField.getText(), new String(passField.getPassword()));
            if (email != null) {
                loggedInEmail = email;
                if (!accounts.account_exist(loggedInEmail)) {
                    cardLayout.show(mainPanel, "account");
                } else {
                    accountNumber = accounts.getAccount_number(loggedInEmail);
                    cardLayout.show(mainPanel, "dashboard");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Email or Password");
            }
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "home"));

        return panel;
    }

    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField initBalField = new JTextField();
        JPasswordField pinField = new JPasswordField();
        JButton createBtn = new JButton("Open Account");
        JButton backBtn = new JButton("Back");

        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Initial Balance:"));
        panel.add(initBalField);
        panel.add(new JLabel("Security Pin:"));
        panel.add(pinField);
        panel.add(createBtn);
        panel.add(backBtn);

        createBtn.addActionListener(e -> {
            try {
                accountNumber = accounts.open_accountGUI(loggedInEmail, nameField.getText(),
                        Double.parseDouble(initBalField.getText()), new String(pinField.getPassword()));
                JOptionPane.showMessageDialog(this, "Account Created! Acc#: " + accountNumber);
                cardLayout.show(mainPanel, "dashboard");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage());
            }
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        return panel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));

        JButton debitBtn = new JButton("Debit Money");
        JButton creditBtn = new JButton("Credit Money");
        JButton transferBtn = new JButton("Transfer Money");
        JButton balanceBtn = new JButton("Check Balance");
        JButton logoutBtn = new JButton("Logout");

        debitBtn.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog("Enter Amount:");
            String pin = JOptionPane.showInputDialog("Enter Pin:");
            boolean ok = accountManager.debit_moneyGUI(accountNumber, Double.parseDouble(amt), pin);
            JOptionPane.showMessageDialog(this, ok ? "Debited Successfully" : "Failed Transaction");
        });

        creditBtn.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog("Enter Amount:");
            String pin = JOptionPane.showInputDialog("Enter Pin:");
            boolean ok = accountManager.credit_moneyGUI(accountNumber, Double.parseDouble(amt), pin);
            JOptionPane.showMessageDialog(this, ok ? "Credited Successfully" : "Failed Transaction");
        });

        transferBtn.addActionListener(e -> {
            String recv = JOptionPane.showInputDialog("Enter Receiver Acc No:");
            String amt = JOptionPane.showInputDialog("Enter Amount:");
            String pin = JOptionPane.showInputDialog("Enter Pin:");
            boolean ok = accountManager.transfer_moneyGUI(accountNumber, Long.parseLong(recv),
                    Double.parseDouble(amt), pin);
            JOptionPane.showMessageDialog(this, ok ? "Transfer Successful" : "Failed Transaction");
        });

        balanceBtn.addActionListener(e -> {
            String pin = JOptionPane.showInputDialog("Enter Pin:");
            Double bal = accountManager.getBalanceGUI(accountNumber, pin);
            JOptionPane.showMessageDialog(this, bal != null ? "Balance: " + bal : "Invalid Pin");
        });

        logoutBtn.addActionListener(e -> {
            loggedInEmail = null;
            accountNumber = 0;
            cardLayout.show(mainPanel, "home");
        });

        panel.add(new JLabel("Dashboard", SwingConstants.CENTER));
        panel.add(debitBtn);
        panel.add(creditBtn);
        panel.add(transferBtn);
        panel.add(balanceBtn);
        panel.add(logoutBtn);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankingGUI().setVisible(true));
    }
}
