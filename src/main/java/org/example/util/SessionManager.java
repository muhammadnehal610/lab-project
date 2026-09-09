package org.example.util;

import java.util.prefs.Preferences;

public class SessionManager {
    private static final String KEY_USER_EMAIL = "logged_in_user_email";

    private  static  final Preferences prefs = Preferences.userNodeForPackage(SessionManager.class);

    public static void saveSession(String email) {
        prefs.put(KEY_USER_EMAIL, email);
    }

    public static String getSavedSession() {
        return prefs.get(KEY_USER_EMAIL, null);
    }

    public static boolean hasActiveSession() {
        return getSavedSession() != null;
    }

    public static void clearSession() {
        prefs.remove(KEY_USER_EMAIL);
    }
}
