import backend.model.*;
import backend.storage.persistence_worker;
import backend.storage.report_repository;
import ui.MainFrame;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        report_repository repo = report_repository.getInstance();

        // Seed initial tickets if database is fresh/empty
        if (repo.findAll().isEmpty()) {
            safety s1 = new safety(
                "Broken Streetlight & Blind Spot Near Block C",
                "Students reporting feeling unsafe near the pathway between Block C and Hostel 1 after 8 PM.",
                "Block C Pathway",
                true,
                "ANONYMOUS",
                "ANONYMOUS",
                "Lighting & CCTV"
            );
            for (int i = 0; i < 26; i++) s1.endorse();
            repo.save(s1);

            management m1 = new management(
                "Hostel 2 Third Floor Wi-Fi Packet Drop",
                "Frequent disconnects and packet loss during peak evening hours (8 PM to 11 PM).",
                "Hostel 2 - 3rd Floor East Wing",
                false,
                "STUDENT",
                "CSE-3rd-Sem-Rep",
                "IT Support"
            );
            for (int i = 0; i < 18; i++) m1.endorse();
            repo.save(m1);

            suggestion sg1 = new suggestion(
                "Extend Central Library Weekend Hours During CAT",
                "Requesting Central Library reading halls stay open until 2:00 AM on weekends during CAT cycles.",
                "Central Library",
                false,
                "FACULTY",
                "Dr. Academic Coordinator",
                "Academic & Facilities"
            );
            for (int i = 0; i < 41; i++) sg1.endorse();
            repo.save(sg1);
        }

        System.out.println("[Campora Desktop] Connected. Initial tickets loaded: " + repo.findAll().size());

        new persistence_worker().start();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
