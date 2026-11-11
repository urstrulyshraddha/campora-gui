package backend.storage;

import backend.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class report_repository {
    private static final report_repository INSTANCE = new report_repository();
    private final List<report> memoryCache = Collections.synchronizedList(new ArrayList<>());
    private boolean initialized = false;

    private report_repository() {
        init();
    }

    public static report_repository getInstance() {
        return INSTANCE;
    }

    private synchronized void init() {
        if (!initialized) {
            loadFromDatabase();
            initialized = true;
        }
    }

    public synchronized void save(report r) {
        if (r == null) return;

        // 1. Immediate in-memory cache update for desktop GUI responsiveness
        int existingIndex = -1;
        for (int i = 0; i < memoryCache.size(); i++) {
            if (memoryCache.get(i).getId().equalsIgnoreCase(r.getId())) {
                existingIndex = i;
                break;
            }
        }
        if (existingIndex >= 0) {
            memoryCache.set(existingIndex, r);
        } else {
            memoryCache.add(0, r); // Newest report on top
        }

        // 2. Persist to MySQL database
        String sql = "INSERT INTO reports (id, title, description, location, domain, status, support_count, anonymous, reporter_type, reporter_identity, created_at, category_or_dept) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE support_count = VALUES(support_count), status = VALUES(status)";

        String extra = "";
        if (r instanceof safety) {
            extra = ((safety) r).getHazardCategory();
        } else if (r instanceof management) {
            extra = ((management) r).getDepartment();
        } else if (r instanceof suggestion) {
            extra = ((suggestion) r).getInitiativeType();
        }

        try (Connection conn = database_manager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, r.getId());
            pstmt.setString(2, r.getTitle());
            pstmt.setString(3, r.getDescription());
            pstmt.setString(4, r.getLocation());
            pstmt.setString(5, r.getDomain());
            pstmt.setString(6, r.getStatus());
            pstmt.setInt(7, r.getSupportCount());
            pstmt.setInt(8, r.isAnonymous() ? 1 : 0);
            pstmt.setString(9, r.getReporterType() != null ? r.getReporterType() : "STUDENT");
            pstmt.setString(10, r.getReporterIdentity() != null ? r.getReporterIdentity() : "ANONYMOUS");
            pstmt.setString(11, r.getCreatedAt());
            pstmt.setString(12, extra != null ? extra : "");

            pstmt.executeUpdate();
            System.out.println("[DB Save Success] Report ID: " + r.getId());
        } catch (Exception e) {
            System.err.println("[DB Notice] Retained in desktop local session cache: " + e.getMessage());
        }
    }

    public synchronized report findById(String id) {
        if (id == null) return null;
        String cleanId = id.trim();
        for (report r : memoryCache) {
            if (r.getId().equalsIgnoreCase(cleanId)) return r;
        }
        return null;
    }

    public synchronized int incrementEndorsement(String id) {
        report r = findById(id);
        if (r != null) {
            r.endorse();
            save(r);
            return r.getSupportCount();
        }
        return -1;
    }

    public synchronized boolean updateStatus(String id, String newStatus, String resolutionNote) {
        if (id == null) return false;
        report r = findById(id);
        if (r != null) {
            r.setStatus(newStatus);
            if (resolutionNote != null && !resolutionNote.trim().isEmpty()) {
                String fullNote = "[STATUS UPDATE: " + newStatus + "] " + resolutionNote.trim();
                r.addSupplementalNote(fullNote);
                addNote(r.getId(), fullNote);
            }
            save(r);
            return true;
        }
        return false;
    }

    public synchronized void addNote(String reportId, String note) {
        report r = findById(reportId);
        if (r != null && !r.getSupplementalNotes().contains(note)) {
            r.addSupplementalNote(note);
        }

        String sql = "INSERT INTO report_notes (report_id, note) VALUES (?, ?)";
        try (Connection conn = database_manager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reportId.trim());
            pstmt.setString(2, note);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("[DB Note Error] " + e.getMessage());
        }
    }

    public synchronized List<report> findAll() {
        return new ArrayList<>(memoryCache);
    }

    public synchronized List<report> findByDomain(String domain) {
        List<report> list = new ArrayList<>();
        if (domain == null) return list;
        for (report r : memoryCache) {
            if (r.getDomain().equalsIgnoreCase(domain) ||
               (domain.equalsIgnoreCase("management") && r.getDomain().equalsIgnoreCase("facilities"))) {
                list.add(r);
            }
        }
        return list;
    }

    public synchronized List<report> findByStatus(String status) {
        List<report> list = new ArrayList<>();
        if (status == null) return list;
        for (report r : memoryCache) {
            if (r.getStatus().equalsIgnoreCase(status)) {
                list.add(r);
            }
        }
        return list;
    }

    public synchronized int countByDomain(String domain) {
        return findByDomain(domain).size();
    }

    public synchronized int countByStatus(String status) {
        return findByStatus(status).size();
    }

    private void loadFromDatabase() {
        String sql = "SELECT * FROM reports ORDER BY support_count DESC";
        try (Connection conn = database_manager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                report r = mapRowToReport(rs);
                loadNotes(conn, r);
                memoryCache.add(r);
            }
            System.out.println("[DB Loaded] Loaded " + memoryCache.size() + " records from MySQL.");
        } catch (Exception e) {
            System.err.println("[Repository Notice] Database connection deferred (" + e.getMessage() + "). Ready in local desktop mode.");
        }
    }

    private void loadNotes(Connection conn, report r) {
        String sql = "SELECT note FROM report_notes WHERE LOWER(TRIM(report_id)) = LOWER(TRIM(?))";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, r.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    r.addSupplementalNote(rs.getString("note"));
                }
            }
        } catch (SQLException ignored) {}
    }

    private report mapRowToReport(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String domain = rs.getString("domain");
        String title = rs.getString("title");
        String desc = rs.getString("description");
        String loc = rs.getString("location");
        boolean anon = rs.getInt("anonymous") == 1;
        String reporterType = rs.getString("reporter_type");
        String reporter = rs.getString("reporter_identity");
        String createdAt = rs.getString("created_at");
        String status = rs.getString("status");
        int count = rs.getInt("support_count");
        String extra = rs.getString("category_or_dept");

        if ("safety".equalsIgnoreCase(domain)) {
            return new safety(id, title, desc, loc, anon, reporterType, reporter, createdAt, status, count, extra);
        } else if ("suggestion".equalsIgnoreCase(domain)) {
            return new suggestion(id, title, desc, loc, anon, reporterType, reporter, createdAt, status, count, extra);
        } else {
            return new management(id, title, desc, loc, anon, reporterType, reporter, createdAt, status, count, extra);
        }
    }
}
