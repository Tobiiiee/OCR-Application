package com.ocrapp.view;

import java.awt.Color;
import java.awt.Font;

/**
 * Centralized theme management for the application
 * Supports dark and light themes
 */
public class Theme {
    
    public enum ThemeMode {
        DARK, LIGHT
    }
    
    private static ThemeMode currentTheme = ThemeMode.DARK;
    
    // Dark Theme Colors
    private static final Color DARK_BG_PRIMARY = new Color(43, 45, 49);      // #2B2D31
    private static final Color DARK_BG_SECONDARY = new Color(35, 37, 41);    // #232529
    private static final Color DARK_BG_TERTIARY = new Color(30, 31, 34);     // #1E1F22
    private static final Color DARK_TEXT_PRIMARY = new Color(242, 243, 245);  // #F2F3F5
    private static final Color DARK_TEXT_SECONDARY = new Color(181, 186, 193); // #B5BAC1
    private static final Color DARK_BORDER = new Color(56, 58, 64);          // #383A40
    private static final Color DARK_ACCENT = new Color(88, 101, 242);        // #5865F2
    private static final Color DARK_ACCENT_HOVER = new Color(71, 82, 196);   // #4752C4
    private static final Color DARK_ERROR = new Color(237, 66, 69);          // #ED4245
    private static final Color DARK_SUCCESS = new Color(67, 181, 129);       // #43B581
    
    // Light Theme Colors (later)
    private static final Color LIGHT_BG_PRIMARY = new Color(255, 255, 255);
    private static final Color LIGHT_BG_SECONDARY = new Color(242, 243, 245);
    private static final Color LIGHT_TEXT_PRIMARY = new Color(32, 34, 37);
    private static final Color LIGHT_BORDER = new Color(222, 224, 227);
    
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_MEDIUM = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_LARGE = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.PLAIN, 13);
    
    public static void setTheme(ThemeMode theme) {
        currentTheme = theme;
    }
    
    public static ThemeMode getCurrentTheme() {
        return currentTheme;
    }
    
    public static Color getBgPrimary() {
        return currentTheme == ThemeMode.DARK ? DARK_BG_PRIMARY : LIGHT_BG_PRIMARY;
    }
    
    public static Color getBgSecondary() {
        return currentTheme == ThemeMode.DARK ? DARK_BG_SECONDARY : LIGHT_BG_SECONDARY;
    }
    
    public static Color getBgTertiary() {
        return currentTheme == ThemeMode.DARK ? DARK_BG_TERTIARY : LIGHT_BG_PRIMARY;
    }
    
    public static Color getTextPrimary() {
        return currentTheme == ThemeMode.DARK ? DARK_TEXT_PRIMARY : LIGHT_TEXT_PRIMARY;
    }
    
    public static Color getTextSecondary() {
        return currentTheme == ThemeMode.DARK ? DARK_TEXT_SECONDARY : LIGHT_TEXT_PRIMARY;
    }
    
    public static Color getBorder() {
        return currentTheme == ThemeMode.DARK ? DARK_BORDER : LIGHT_BORDER;
    }
    
    public static Color getAccent() {
        return currentTheme == ThemeMode.DARK ? DARK_ACCENT : DARK_ACCENT;
    }
    
    public static Color getAccentHover() {
        return currentTheme == ThemeMode.DARK ? DARK_ACCENT_HOVER : DARK_ACCENT_HOVER;
    }
    
    // Status Colors
    public static Color getError() {
        return DARK_ERROR;
    }
    
    public static Color getSuccess() {
        return DARK_SUCCESS;
    }
    
    public static Color getSelectionFill() {
        return new Color(88, 101, 242, 80);
    }
    
    public static Color getSelectionBorder() {
        return getAccent();
    }
}