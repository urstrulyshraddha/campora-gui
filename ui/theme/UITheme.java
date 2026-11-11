package ui.theme;

import java.awt.Color;
import java.awt.Font;

public class UITheme {
    // Brand Colors
    public static final Color PURPLE_PRIMARY   = new Color(124, 58, 237);  // #7c3aed
    public static final Color PURPLE_HOVER     = new Color(109, 40, 217);  // #6d28d9
    public static final Color PURPLE_LIGHT     = new Color(237, 233, 254); // #ede9fe
    public static final Color PURPLE_DARK      = new Color(76, 29, 149);   // #4c1d95

    // Surface & Layout
    public static final Color BG_MAIN          = new Color(248, 247, 252); // #f8f7fc
    public static final Color SURFACE_WHITE    = Color.WHITE;
    public static final Color BORDER_COLOR     = new Color(233, 228, 245); // #e9e4f5
    public static final Color SUBTLE_BG        = new Color(243, 240, 250); // #f3f0fa

    // Typography
    public static final Color TEXT_MAIN        = new Color(30, 27, 75);    // #1e1b4b
    public static final Color TEXT_MUTED       = new Color(100, 116, 139); // #64748b
    public static final Color TEXT_LIGHT       = new Color(148, 163, 184); // #94a3b8

    // Status & Accent Badges
    public static final Color BADGE_PENDING    = new Color(254, 243, 199); // amber light
    public static final Color TEXT_PENDING     = new Color(180, 83, 9);
    public static final Color BADGE_PROGRESS   = new Color(224, 231, 255); // indigo light
    public static final Color TEXT_PROGRESS    = new Color(67, 56, 202);
    public static final Color BADGE_RESOLVED   = new Color(209, 250, 229); // emerald light
    public static final Color TEXT_RESOLVED    = new Color(4, 120, 87);
    public static final Color BADGE_REJECTED   = new Color(254, 226, 226); // rose light
    public static final Color TEXT_REJECTED    = new Color(185, 28, 28);

    // Track Domains
    public static final Color TRACK_SAFETY     = new Color(239, 68, 68);   // red
    public static final Color TRACK_MGMT       = new Color(14, 165, 233);  // sky
    public static final Color TRACK_SUGGEST    = new Color(168, 85, 247);  // purple

    // Fonts
    public static final Font FONT_HEADER_BOLD  = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_TITLE_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BODY_BOLD    = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_CAPTION      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO_SMALL   = new Font("Consolas", Font.PLAIN, 11);
}
