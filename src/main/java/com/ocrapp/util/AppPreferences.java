package com.ocrapp.util;

import java.util.prefs.Preferences;

/**
 * Manages application preferences and settings
 */
public class AppPreferences {
    
    private static final Preferences prefs = Preferences.userNodeForPackage(AppPreferences.class);
    
    private static final String KEY_LAST_LANGUAGE = "lastLanguage";
    private static final String KEY_LAST_DIRECTORY = "lastDirectory";
    
    public static void saveLastLanguage(String language) {
        if (language != null) {
            prefs.put(KEY_LAST_LANGUAGE, language);
        }
    }
    
    public static String getLastLanguage() {
        return prefs.get(KEY_LAST_LANGUAGE, "English");
    }
    
    public static void saveLastDirectory(String directory) {
        if (directory != null) {
            prefs.put(KEY_LAST_DIRECTORY, directory);
        }
    }
    
    public static String getLastDirectory() {
        return prefs.get(KEY_LAST_DIRECTORY, null);
    }
    
    public static void clearAll() {
        try {
            prefs.clear();
        } catch (Exception e) {
            System.err.println("Failed to clear preferences: " + e.getMessage());
        }
    }
}