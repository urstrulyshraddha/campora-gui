package ui;

import backend.model.report;
import backend.storage.report_repository;
import ui.components.PrimaryButton;
import ui.components.RoundedPanel;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManagementPortal extends JPanel {
    private final report_repository repo = report_repository.getInstance();
    private static final String PASSCODE = "campora2026";
    private boolean isAuthenticated = false;

    private JPanel authGatePanel;
    private JPanel mainContentPanel;
    private JTable triageTable;
    private DefaultTableModel tableModel;
    private JLabel totalSubmissionsLabel;
    private JLabel pendingLabel;
    private JLabel inProgressLabel;
    private JLabel resolvedLabel;

    public ManagementPortal() {
        setLayout(new CardLayout());
        setBackground(UITheme.BG_MAIN);

        buildAuthGate();
        buildDashboard();

        add(authGatePanel, "GATE");
        add(mainContentPanel, "DASHBOARD");

        showGate();
    }

    private void showGate() {
        CardLayout cl = (CardLayout) getLayout();
        cl.show(this, "GATE");
    }

    private void showDashboard() {
        CardLayout cl = (CardLayout) getLayout();
        cl.show(this, "DASHBOARD");
        refreshTriage();
    }

    private void buildAuthGate() {
        authGatePanel = new JPanel(new GridBagLayout());
        authGatePanel.setBackground(UITheme.BG_MAIN);

        RoundedPanel card = new RoundedPanel(20, UITheme.SURFACE_WHITE, UITheme.BORDER_COLOR, 1);
        card.setPreferredSize(new Dimension(380, 290));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel iconLabel = new JLabel("🔒", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Management Access Gate");
        title.setFont(UITheme.FONT_TITLE_BOLD);
        title.setForeground(UITheme.TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Authentication required for administrative triage.");
        desc.setFont(UITheme.FONT_CAPTION);
        desc.setForeground(UITheme.TEXT_MUTED);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField passField = new JPasswordField();
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        passField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel errLabel = new JLabel("Invalid passcode. Access denied.");
        errLabel.setFont(UITheme.FONT_CAPTION);
        errLabel.setForeground(UITheme.TEXT_REJECTED);
        errLabel.setVisible(false);
        errLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        PrimaryButton unlockBtn = new PrimaryButton("Verify Passcode");
        unlockBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        unlockBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        unlockBtn.addActionListener(e -> {
            String entered = new String(passField.getPassword()).trim();
            if (PASSCODE.equals(entered)) {
                isAuthenticated = true;
                errLabel.setVisible(false);
                passField.setText("");
                showDashboard();
            } else {
                errLabel.setVisible(true);
            }
        });

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(desc);
        card.add(Box.createVerticalStrut(18));
        card.add(passField);
        card.add(Box.createVerticalStrut(6));
        card.add(errLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(unlockBtn);

        authGatePanel.add(card);
    }

    private void buildDashboard() {
        mainContentPanel = new JPanel(new BorderLayout(0, 12));
        mainContentPanel.setBackground(UITheme.BG_MAIN);
        mainContentPanel.setBorder(new EmptyBorder(16, 24, 24, 24));

        // Top Metrics Strip
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel headTitle = new JLabel("Grievance Resolution & Triage Console");
        headTitle.setFont(UITheme.FONT_HEADER_BOLD);
        headTitle.setForeground(UITheme.TEXT_MAIN);

        JButton lockBtn = new JButton("Lock Console");
        lockBtn.setFont(UITheme.FONT_CAPTION);
        lockBtn.setForeground(UITheme.TEXT_REJECTED);
        lockBtn.setContentAreaFilled(false);
        lockBtn.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        lockBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lockBtn.addActionListener(e -> {
            isAuthenticated = false;
            showGate();
        });

        headerRow.add(headTitle, BorderLayout.WEST);
        headerRow.add(lockBtn, BorderLayout.EAST);
        topPanel.add(headerRow);
        topPanel.add(Box.createVerticalStrut(12));

        // 4 Stat Cards
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statsRow.setOpaque(false);

        totalSubmissionsLabel = new JLabel("0");
        pendingLabel = new JLabel("0");
        inProgressLabel = new JLabel("0");
        resolvedLabel = new JLabel("0");

        statsRow.add(createMiniStat("Total Submissions", totalSubmissionsLabel, UITheme.TEXT_MAIN));
        statsRow.add(createMiniStat("Pending Action", pendingLabel, UITheme.TEXT_PENDING));
        statsRow.add(createMiniStat("In Progress", inProgressLabel, UITheme.TEXT_PROGRESS));
        statsRow.add(createMiniStat("Resolved Issues", resolvedLabel, UITheme.TEXT_RESOLVED));

        topPanel.add(statsRow);
        mainContentPanel.add(topPanel, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"Ticket ID", "Track", "Title", "Location", "Reporter", "Urgency", "Upvotes", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        triageTable = new JTable(tableModel);
        triageTable.setRowHeight(36);
        triageTable.setFont(UITheme.FONT_BODY_REGULAR);
        triageTable.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        triageTable.getTableHeader().setBackground(UITheme.SUBTLE_BG);
        triageTable.getTableHeader().setForeground(UITheme.TEXT_MAIN);
        triageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        triageTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        triageTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        triageTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        triageTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        triageTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        JScrollPane tableScroll = new JScrollPane(triageTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        mainContentPanel.add(tableScroll, BorderLayout.CENTER);

        // Bottom Actions Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        PrimaryButton refreshBtn = new PrimaryButton("Refresh Table", UITheme.SUBTLE_BG, UITheme.BORDER_COLOR, UITheme.TEXT_MAIN, 8);
        refreshBtn.addActionListener(e -> refreshTriage());

        PrimaryButton manageBtn = new PrimaryButton("Update Ticket Status");
        manageBtn.addActionListener(e -> openUpdateModal());

        bottomBar.add(refreshBtn);
        bottomBar.add(manageBtn);
        mainContentPanel.add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createMiniStat(String label, JLabel countLabel, Color color) {
        RoundedPanel p = new RoundedPanel(10, UITheme.SURFACE_WHITE, UITheme.BORDER_COLOR, 1);
        p.setLayout(new BorderLayout(4, 2));
        p.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_CAPTION);
        l.setForeground(UITheme.TEXT_MUTED);

        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        countLabel.setForeground(color);

        p.add(l, BorderLayout.NORTH);
        p.add(countLabel, BorderLayout.SOUTH);
        return p;
    }

    public void refreshTriage() {
        if (!isAuthenticated) return;

        tableModel.setRowCount(0);
        List<report> reports = repo.findAll();

        totalSubmissionsLabel.setText(String.valueOf(reports.size()));
        pendingLabel.setText(String.valueOf(repo.countByStatus("PENDING")));
        inProgressLabel.setText(String.valueOf(repo.countByStatus("IN_PROGRESS")));
        resolvedLabel.setText(String.valueOf(repo.countByStatus("RESOLVED")));

        for (report r : reports) {
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getDomain().toUpperCase(),
                    r.getTitle(),
                    r.getLocation(),
                    r.isAnonymous() ? "🔒 Anonymous" : r.getReporterIdentity(),
                    r.calculateUrgencyIndex(),
                    r.getSupportCount(),
                    r.getStatus()
            });
        }
    }

    private void openUpdateModal() {
        int selectedRow = triageTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a ticket from the table to update.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String ticketId = (String) tableModel.getValueAt(selectedRow, 0);
        report target = repo.findById(ticketId);
        if (target == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Resolve Ticket #" + ticketId, true);
        dialog.setSize(440, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        form.setBackground(UITheme.BG_MAIN);

        JLabel info = new JLabel("Updating: " + target.getTitle());
        info.setFont(UITheme.FONT_BODY_BOLD);
        info.setForeground(UITheme.TEXT_MAIN);

        JComboBox<String> statusDropdown = new JComboBox<>(new String[]{"IN_PROGRESS", "RESOLVED", "REJECTED"});
        statusDropdown.setSelectedItem(target.getStatus());

        JTextArea noteArea = new JTextArea(4, 20);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        JScrollPane noteScroll = new JScrollPane(noteArea);

        form.add(info);
        form.add(Box.createVerticalStrut(12));
        form.add(new JLabel("Set Status:"));
        form.add(statusDropdown);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Action Note / Resolution Comment:"));
        form.add(noteScroll);

        PrimaryButton saveBtn = new PrimaryButton("Commit Update");
        saveBtn.addActionListener(ev -> {
            String newStatus = (String) statusDropdown.getSelectedItem();
            String note = noteArea.getText().trim();

            repo.updateStatus(ticketId, newStatus, note);
            dialog.dispose();
            refreshTriage();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
