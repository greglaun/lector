package org.leafcutter.webviewapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ReadingListProvider {

    public static boolean saveArticle(String title, String html, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String[] existingKeys = retrieveList(context);
        for (String key : existingKeys) {
            if (key.equals(title)) {
                return false; // Key exists, don't create new entry
            }
        }
        String readingList = prefs.getString(context.getString(R.string.pref_reading_list), "");
        prefs.edit().putString(context.getString(R.string.pref_reading_list), readingList + ";" + title).apply();
        prefs.edit().putString(title, html).apply();
        return true;
    }

    public static String[] retrieveList(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String concatList = prefs.getString(context.getString(R.string.pref_reading_list), "");
        if (concatList.length() == 0) {
            return new String[0];
        }
        return concatList.split(";");
    }

    public static String retrieveArticle(String title, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(title, "");
    }

    public static void deleteArticle(String title, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(title).apply();
    }
}
