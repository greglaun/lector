package com.greglaun.lector;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;

public class ReadingListProvider {
    private static final String PLACE = "place";
    private static final String CURRENT = "current";

    public static boolean saveArticle(String title, String html, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String[] existingKeys = retrieveList(context);
        for (String key : existingKeys) {
            if (key.equals(title)) {
                return false; // Key exists, don't create new entry
            }
        }
        String readingList = prefs.getString(context.getString(R.string.pref_reading_list), "");
        String newList;
        if (readingList.length() == 0) {
            newList = title;
        } else {
            newList = readingList + ";" + title;
        }
        prefs.edit().putString(context.getString(R.string.pref_reading_list), newList).apply();
        prefs.edit().putString(title, html).apply();
        return true;
    }

    public static void savePlace(String title, String currentUtterance, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(title + PLACE, currentUtterance).apply();
    }

    public static void deletePlace(String title, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(title + PLACE);
    }

    public static String[] retrieveList(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String concatList = prefs.getString(context.getString(R.string.pref_reading_list), "");
        if (concatList.length() == 0) {
            return new String[0];
        }
        return concatList.split(";");
    }

    public static void saveCurrent(String html, String currentPlace, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(CURRENT, html).apply();
        prefs.edit().putString(CURRENT + PLACE, currentPlace).apply();
    }

    public static String retrieveCurrentHTML(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(CURRENT, "");
    }

    public static String retrieveCurrentPlace(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(CURRENT + PLACE, "");
    }

    public static String retrieveArticle(String title, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(title, "");
    }

    public static String retrievePlace(String title, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(title + PLACE, "");
    }

    public static void deleteArticle(String title, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Remove saved text and place
        prefs.edit().remove(title).apply();
        prefs.edit().remove(title + PLACE);

        // Remove title from list of saved articles
        ArrayList<String> currentList = new ArrayList<>(Arrays.asList(retrieveList(context)));
        currentList.remove(title);
        String newList = "";
        for (String key : currentList) {
            newList += key + ";";
        }
        prefs.edit().putString(context.getString(R.string.pref_reading_list), newList).apply();
    }

}
