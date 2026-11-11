package ui;

import backend.model.*;
import backend.storage.report_repository;
import ui.components.PrimaryButton;
import ui.components.RoundedPanel;
import ui.components.TicketCard;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class StudentFacultyPortal extends JPanel {
    private final report_repository repo = report_repository.getInstance();
    private JPanel feedContainer;
    private JLabel totalCountLabel;
    private JLabel safetyCountLabel;
    private JLabel mgmtCountLabel;
    private JLabel suggestCountLabel;
    private String activeFilter = "ALL";

    public StudentFacultyPortal() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        initUI();
        refreshFeed();
    }

    private void initUI() {
        // --- TOP TOOLBAR & STATS HEADER ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(16, 24, 8, 24));

        // Row 1: Title & Submit Button
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Campus Grievance & Feedback Feed");
        pageTitle.setFont(UITheme.FONT_HEADER_BOLD);
        pageTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel pageSubtitle = new JLabel("Browse reported concerns, upvote critical issues, or register a new report.");
        pageSubtitle.setFont(UITheme.FONT_CAPTION);
        pageSubtitle.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);

        PrimaryButton submitBtn = new PrimaryButton("+ Submit New Issue");
        submitBtn.setPreferredSize(new Dimension(170, 36));
        submitBtn.addActionListener(e -> openSubmissionDialog());

        headerRow.add(titleBlock, BorderLayout.WEST);
        headerRow.add(submitBtn, BorderLayout.EAST);
        topContainer.add(headerRow);
        topContainer.add(Box.createVerticalStrut(14));

        // Row 2: Metric Stat Cards
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statsRow.setOpaque(false);

        totalCountLabel = new JLabel("0");
        safetyCountLabel = new JLabel("0");
        mgmtCountLabel = new JLabel("0");
        suggestCountLabel = new JLabel("0");

        statsRow.add(createStatCard("Total Submissions", totalCountLabel, UITheme.PURPLE_PRIMARY));
        statsRow.add(createStatCard("Safety Alerts", safetyCountLabel, UITheme.TRACK_SAFETY));
        statsRow.add(createStatCard("Facilities Tickets", mgmtCountLabel, UITheme.TRACK_MGMT));
        statsRow.add(createStatCard("Campus Ideas", suggestCountLabel, UITheme.TRACK_SUGGEST));

        topContainer.add(statsRow);
        topContainer.add(Box.createVerticalStrut(14));

        // Row 3: Filter Buttons
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);

        filterRow.add(createFilterButton("All Tickets", "ALL"));
        filterRow.add(createFilterButton("Safety Concerns", "safety"));
        filterRow.add(createFilterButton("Facilities Maintenance", "management"));
        filterRow.add(createFilterButton("Student Suggestions", "suggestion"));

        topContainer.add(filterRow);
        add(topContainer, BorderLayout.NORTH);

        // --- CENTER: Scrollable Ticket Cards Feed ---
        feedContainer = new JPanel();
        feedContainer.setLayout(new BoxLayout(feedContainer, BoxLayout.Y_AXIS));
        feedContainer.setBackground(UITheme.BG_MAIN);
        feedContainer.setBorder(new EmptyBorder(8, 24, 24, 24));

        JScrollPane scrollPane = new JScrollPane(feedContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UITheme.BG_MAIN);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent) {
        RoundedPanel card = new RoundedPanel(12, UITheme.SURFACE_WHITE, UITheme.BORDER_COLOR, 1);
        card.setLayout(new BorderLayout(8, 2));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_CAPTION);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(accent);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.SOUTH);
        return card;
    }

    private JButton createFilterButton(String text, String domainKey) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY_REGULAR);
        btn.setFocusPainted(false);
        btn.setBackground(UITheme.SURFACE_WHITE);
        btn.setForeground(UITheme.TEXT_MAIN);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            activeFilter = domainKey;
            refreshFeed();
        });
        return btn;
    }

    public void refreshFeed() {
        feedContainer.removeAll();

        List<report> reports = "ALL".equals(activeFilter)
                ? repo.findAll()
                : repo.findByDomain(activeFilter);

        // Update Stat Counters
        totalCountLabel.setText(String.valueOf(repo.findAll().size()));
        safetyCountLabel.setText(String.valueOf(repo.countByDomain("safety")));
        mgmtCountLabel.setText(String.valueOf(repo.countByDomain("management")));
        suggestCountLabel.setText(String.valueOf(repo.countByDomain("suggestion")));

        if (reports.isEmpty()) {
            JLabel empty = new JLabel("No tickets recorded in this category.");
            empty.setFont(UITheme.FONT_BODY_REGULAR);
            empty.setForeground(UITheme.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            feedContainer.add(Box.createVerticalStrut(40));
            feedContainer.add(empty);
        } else {
            for (report r : reports) {
                feedContainer.add(new TicketCard(r, this::refreshFeed));
                feedContainer.add(Box.createVerticalStrut(10));
            }
        }

        feedContainer.revalidate();
        feedContainer.repaint();
    }

    private void openSubmissionDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Submit Campus Grievance", true);
        dialog.setSize(520, 560);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.BG_MAIN);
        dialog.setLayout(new BorderLayout(12, 12));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        form.setOpaque(false);

        JComboBox<String> trackBox = new JComboBox<>(new String[]{"Facilities Maintenance", "Safety Alert", "Campus Suggestion / Idea"});
        JTextField titleField = new JTextField();
        JTextField locationField = new JTextField();
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);

        JTextField categoryField = new JTextField("General");
        JCheckBox anonCheck = new JCheckBox("Submit Anonymously");
        anonCheck.setOpaque(false);
        anonCheck.setFont(UITheme.FONT_BODY_REGULAR);

        JComboBox<String> roleBox = new JComboBox<>(new String[]{"STUDENT", "FACULTY"});
        JTextField idField = new JTextField();

        anonCheck.addActionListener(e -> {
            boolean isAnon = anonCheck.isSelected();
            roleBox.setEnabled(!isAnon);
            idField.setEnabled(!isAnon);
        });

        form.add(new JLabel("Grievance Track:"));
        form.add(trackBox);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Title / Summary:"));
        form.add(titleField);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Specific Campus Location:"));
        form.add(locationField);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Detailed Description:"));
        form.add(descScroll);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Category / Sub-Domain:"));
        form.add(categoryField);
        form.add(Box.createVerticalStrut(10));
        form.add(anonCheck);
        form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Reporter Affiliation:"));
        form.add(roleBox);
        form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("University ID or Name:"));
        form.add(idField);

        PrimaryButton submitBtn = new PrimaryButton("Register Ticket");
        submitBtn.addActionListener(ev -> {
            String title = titleField.getText().trim();
            String loc = locationField.getText().trim();
            String desc = descArea.getText().trim();
            String cat = categoryField.getText().trim();
            boolean isAnon = anonCheck.isSelected();

            if (title.isEmpty() || loc.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Title and Location are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String repType = isAnon ? "ANONYMOUS" : (String) roleBox.getSelectedItem();
            String repId = isAnon ? "ANONYMOUS" : (idField.getText().trim().isEmpty() ? repType : idField.getText().trim());

            report r;
            int trackIdx = trackBox.getSelectedIndex();
            if (trackIdx == 1) {
                r = new safety(title, desc, loc, isAnon, repType, repId, cat.isEmpty() ? "Safety Hazard" : cat);
            } else if (trackIdx == 2) {
                r = new suggestion(title, desc, loc, isAnon, repType, repId, cat.isEmpty() ? "Campus Initiative" : cat);
            } else {
                r = new management(title, desc, loc, isAnon, repType, repId, cat.isEmpty() ? "Facilities Maintenance" : cat);
            }

            repo.save(r);
            dialog.dispose();
            refreshFeed();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(submitBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
