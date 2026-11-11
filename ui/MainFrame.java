package ui;

import ui.components.PrimaryButton;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentDeck = new JPanel(cardLayout);
    private final StudentFacultyPortal studentPortal;
    private final ManagementPortal managementPortal;

    private PrimaryButton navStudentBtn;
    private PrimaryButton navMgmtBtn;

    public MainFrame() {
        setTitle("Campora | Campus Redressal & Grievance Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        studentPortal = new StudentFacultyPortal();
        managementPortal = new ManagementPortal();

        initLayout();
    }

    private void initLayout() {
        setLayout(new BorderLayout());

        // Header Navigation Bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.SURFACE_WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(12, 24, 12, 24)
        ));

        // Brand logo
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logoIcon = new JLabel("📋");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JPanel brandText = new JPanel(new GridLayout(2, 1));
        brandText.setOpaque(false);
        JLabel brandName = new JLabel("CAMPORA");
        brandName.setFont(UITheme.FONT_TITLE_BOLD);
        brandName.setForeground(UITheme.TEXT_MAIN);

        JLabel brandSub = new JLabel("Campus Redressal Platform (Java Swing GUI)");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        brandSub.setForeground(UITheme.PURPLE_PRIMARY);

        brandText.add(brandName);
        brandText.add(brandSub);

        brandPanel.add(logoIcon);
        brandPanel.add(brandText);
        header.add(brandPanel, BorderLayout.WEST);

        // Center Switcher Buttons
        JPanel navSwitcher = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        navSwitcher.setOpaque(false);

        navStudentBtn = new PrimaryButton("Student Feed", UITheme.PURPLE_PRIMARY, UITheme.PURPLE_HOVER, Color.WHITE, 8);
        navMgmtBtn = new PrimaryButton("Management Triage", UITheme.SUBTLE_BG, UITheme.BORDER_COLOR, UITheme.TEXT_MAIN, 8);

        navStudentBtn.setPreferredSize(new Dimension(130, 32));
        navMgmtBtn.setPreferredSize(new Dimension(150, 32));

        navStudentBtn.addActionListener(e -> switchView("STUDENT"));
        navMgmtBtn.addActionListener(e -> switchView("MANAGEMENT"));

        navSwitcher.add(navStudentBtn);
        navSwitcher.add(navMgmtBtn);
        header.add(navSwitcher, BorderLayout.CENTER);

        // Right connection indicator
        JLabel dbStatus = new JLabel("● MySQL Cloud Online");
        dbStatus.setFont(UITheme.FONT_MONO_SMALL);
        dbStatus.setForeground(UITheme.TEXT_RESOLVED);
        header.add(dbStatus, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Deck container for switching views
        contentDeck.add(studentPortal, "STUDENT");
        contentDeck.add(managementPortal, "MANAGEMENT");
        add(contentDeck, BorderLayout.CENTER);
    }

    private void switchView(String view) {
        cardLayout.show(contentDeck, view);
        if ("STUDENT".equals(view)) {
            navStudentBtn.setBackground(UITheme.PURPLE_PRIMARY);
            navStudentBtn.setForeground(Color.WHITE);
            studentPortal.refreshFeed();
        } else {
            managementPortal.refreshTriage();
        }
    }
}
