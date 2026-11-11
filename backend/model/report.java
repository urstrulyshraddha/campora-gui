package backend.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class report implements reportable {
    protected String id;
    protected String title;
    protected String description;
    protected String location;
    protected String domain;
    protected boolean isAnonymous;
    protected String reporterType;
    protected String reporterIdentity;
    protected String createdAt;
    protected String status;
    protected int supportCount;
    protected List<String> supplementalNotes;

    // 1. Constructor for new submissions (7 parameters)
    public report(String title, String description, String location, String domain,
                  boolean isAnonymous, String reporterType, String reporterIdentity) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.title = title;
        this.description = description;
        this.location = location;
        this.domain = domain;
        this.isAnonymous = isAnonymous;
        this.reporterType = reporterType;
        this.reporterIdentity = reporterIdentity;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = "PENDING";
        this.supportCount = 0;
        this.supplementalNotes = new ArrayList<>();
    }

    // 2. Reconstitution constructor from DB / snapshot (11 parameters)
    public report(String id, String title, String description, String location, String domain,
                  boolean isAnonymous, String reporterType, String reporterIdentity,
                  String createdAt, String status, int supportCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.domain = domain;
        this.isAnonymous = isAnonymous;
        this.reporterType = reporterType;
        this.reporterIdentity = reporterIdentity;
        this.createdAt = createdAt;
        this.status = status;
        this.supportCount = supportCount;
        this.supplementalNotes = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getDomain() { return domain; }
    public boolean isAnonymous() { return isAnonymous; }
    public String getReporterType() { return reporterType; }
    public String getReporterIdentity() { return reporterIdentity; }
    public String getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public int getSupportCount() { return supportCount; }
    public List<String> getSupplementalNotes() { return supplementalNotes; }

    public void setStatus(String status) {
        this.status = status;
    }

    public void endorse() {
        this.supportCount++;
    }

    public void addSupplementalNote(String note) {
        if (note != null && !note.trim().isEmpty()) {
            this.supplementalNotes.add(note.trim());
        }
    }

    // JSON serialization method required by persistence_worker
    public String toJson() {
        StringBuilder notesJson = new StringBuilder("[");
        for (int i = 0; i < supplementalNotes.size(); i++) {
            notesJson.append("\"").append(escapeJson(supplementalNotes.get(i))).append("\"");
            if (i < supplementalNotes.size() - 1) notesJson.append(",");
        }
        notesJson.append("]");

        return "{"
                + "\"id\":\"" + escapeJson(id) + "\","
                + "\"title\":\"" + escapeJson(title) + "\","
                + "\"description\":\"" + escapeJson(description) + "\","
                + "\"location\":\"" + escapeJson(location) + "\","
                + "\"domain\":\"" + escapeJson(domain) + "\","
                + "\"status\":\"" + escapeJson(status) + "\","
                + "\"supportCount\":" + supportCount + ","
                + "\"urgencyIndex\":" + calculateUrgencyIndex() + ","
                + "\"anonymous\":" + isAnonymous + ","
                + "\"reporterType\":\"" + escapeJson(reporterType) + "\","
                + "\"reporterIdentity\":\"" + escapeJson(reporterIdentity) + "\","
                + "\"createdAt\":\"" + escapeJson(createdAt) + "\","
                + "\"summary\":\"" + escapeJson(getReportSummary()) + "\","
                + "\"supplementalNotes\":" + notesJson.toString()
                + "}";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
