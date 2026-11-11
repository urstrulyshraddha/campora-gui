package ui.components;

import backend.model.report;
import backend.storage.report_repository;
import ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TicketCard extends RoundedPanel {
    private final report reportItem;
    private final Runnable onDataChanged;
    private JLabel voteCountLabel;

    public TicketCard(report r, Runnable onDataChanged) {
        super(16, UITheme.SURFACE_WHITE, UITheme.BORDER_COLOR, 1);
        this.reportItem = r;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 18, 16, 18));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        initUI();
    }

    private void initUI() {
        // --- TOP ROW: Badges & ID ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel badgeFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgeFlow.setOpaque(false);

        // Domain Badge
        Color trackColor = switch (reportItem.getDomain().toLowerCase()) {
            case "safety" -> UITheme.TRACK_SAFETY;
            case "suggestion" -> UITheme.TRACK_SUGGEST;
            default -> UITheme.TRACK_MGMT;
        };
        badgeFlow.add(createPill(reportItem.getDomain().toUpperCase(), trackColor, Color.WHITE));

        // Status Badge
        Color statusBg;
        Color statusFg;
        switch (reportItem.getStatus().toUpperCase()) {
            case "RESOLVED" -> {
                statusBg = UITheme.BADGE_RESOLVED;
                statusFg = UITheme.TEXT_RESOLVED;
            }
            case "IN_PROGRESS" -> {
                statusBg = UITheme.BADGE_PROGRESS;
                statusFg = UITheme.TEXT_PROGRESS;
            }
            case "REJECTED" -> {
                statusBg = UITheme.BADGE_REJECTED;
                statusFg = UITheme.TEXT_REJECTED;
            }
            default -> {
                statusBg = UITheme.BADGE_PENDING;
                statusFg = UITheme.TEXT_PENDING;
            }
        }
        badgeFlow.add(createPill(reportItem.getStatus().replace("_", " "), statusBg, statusFg));

        // Urgency score pill
        badgeFlow.add(createPill("Urgency: " + reportItem.calculateUrgencyIndex(), UITheme.PURPLE_LIGHT, UITheme.PURPLE_DARK));

        // Ticket ID right aligned
        JLabel idLabel = new JLabel("#" + reportItem.getId());
        idLabel.setFont(UITheme.FONT_MONO_SMALL);
        idLabel.setForeground(UITheme.TEXT_LIGHT);

        topPanel.add(badgeFlow, BorderLayout.WEST);
        topPanel.add(idLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER: Title & Details ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(reportItem.getTitle());
        titleLabel.setFont(UITheme.FONT_TITLE_BOLD);
        titleLabel.setForeground(UITheme.TEXT_MAIN);

        JLabel descLabel = new JLabel("<html><body style='width: 520px;'>" + escapeHtml(reportItem.getDescription()) + "</body></html>");
        descLabel.setFont(UITheme.FONT_BODY_REGULAR);
        descLabel.setForeground(UITheme.TEXT_MUTED);

        JLabel metaLabel = new JLabel(String.format("📍 %s   •   👤 %s",
                reportItem.getLocation(),
                reportItem.isAnonymous() ? "Anonymous" : reportItem.getReporterIdentity() + " (" + reportItem.getReporterType() + ")"
        ));
        metaLabel.setFont(UITheme.FONT_CAPTION);
        metaLabel.setForeground(UITheme.TEXT_LIGHT);

        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(descLabel);
        centerPanel.add(Box.createVerticalStrut(8));
        centerPanel.add(metaLabel);

        add(centerPanel, BorderLayout.CENTER);

        // --- BOTTOM ROW: Endorsement Button & Witness Notes ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        PrimaryButton upvoteBtn = new PrimaryButton("▲ Endorse", UITheme.PURPLE_LIGHT, UITheme.PURPLE_PRIMARY, UITheme.PURPLE_DARK, 8);
        upvoteBtn.setPreferredSize(new Dimension(100, 28));
        upvoteBtn.addActionListener(e -> {
            report_repository.getInstance().incrementEndorsement(reportItem.getId());
            reportItem.endorse();
            voteCountLabel.setText(reportItem.getSupportCount() + " Endorsements");
            if (onDataChanged != null) onDataChanged.run();
        });

        voteCountLabel = new JLabel(reportItem.getSupportCount() + " Endorsements");
        voteCountLabel.setFont(UITheme.FONT_BODY_BOLD);
        voteCountLabel.setForeground(UITheme.PURPLE_PRIMARY);

        leftActions.add(upvoteBtn);
        leftActions.add(voteCountLabel);

        JButton noteBtn = new JButton("Witness Notes (" + reportItem.getSupplementalNotes().size() + ")");
        noteBtn.setFont(UITheme.FONT_CAPTION);
        noteBtn.setForeground(UITheme.TEXT_MUTED);
        noteBtn.setContentAreaFilled(false);
        noteBtn.setBorderPainted(false);
        noteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        noteBtn.addActionListener(e -> showNotesDialog());

        bottomPanel.add(leftActions, BorderLayout.WEST);
        bottomPanel.add(noteBtn, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createPill(String text, Color bg, Color fg) {
        RoundedPanel pill = new RoundedPanel(8, bg, null, 0);
        pill.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 2));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(fg);
        pill.add(lbl);
        return pill;
    }

    private void showNotesDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ticket Notes — #" + reportItem.getId(), true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 360);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.setBackground(UITheme.BG_MAIN);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String n : reportItem.getSupplementalNotes()) {
            listModel.addElement(n);
        }
        if (listModel.isEmpty()) {
            listModel.addElement("No witness observations recorded yet.");
        }

        JList<String> list = new JList<>(listModel);
        list.setFont(UITheme.FONT_BODY_REGULAR);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        JPanel addPanel = new JPanel(new BorderLayout(8, 0));
        addPanel.setOpaque(false);
        JTextField noteInput = new JTextField();
        PrimaryButton addBtn = new PrimaryButton("Add Note", UITheme.PURPLE_PRIMARY, UITheme.PURPLE_HOVER, Color.WHITE, 8);
        addBtn.addActionListener(ev -> {
            String val = noteInput.getText().trim();
            if (!val.isEmpty()) {
                report_repository.getInstance().addNote(reportItem.getId(), val);
                reportItem.addSupplementalNote(val);
                if (listModel.contains("No witness observations recorded yet.")) listModel.clear();
                listModel.addElement(val);
                noteInput.setText("");
                if (onDataChanged != null) onDataChanged.run();
            }
        });
        addPanel.add(noteInput, BorderLayout.CENTER);
        addPanel.add(addBtn, BorderLayout.EAST);

        content.add(new JLabel("Observation & Resolution Log:"));
        content.add(Box.createVerticalStrut(6));
        content.add(scroll);
        content.add(Box.createVerticalStrut(10));
        content.add(addPanel);

        dialog.add(content);
        dialog.setVisible(true);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
