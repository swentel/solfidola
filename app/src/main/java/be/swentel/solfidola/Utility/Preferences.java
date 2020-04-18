package be.swentel.solfidola.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class Preferences {

    /**
     * Get a boolean preference.
     *
     * @param context
     *   The current context
     * @param pref
     *   The preference key
     * @param DefaultValue
     *   The default value
     *
     * @return
     *   The preference
     */
    public static boolean getPreference(Context context, String pref, boolean DefaultValue) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        return preference.getBoolean(pref, DefaultValue);
    }

    /**
     * Get an integer preference.
     *
     * @param context
     *   The current context
     * @param pref
     *   The preference key
     * @param DefaultValue
     *   The default value
     *
     * @return
     *   The preference
     */
    public static int getPreference(Context context, String pref, int DefaultValue) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        return preference.getInt(pref, DefaultValue);
    }

    /**
     * Get a a string preference.
     *
     * @param context
     *   The current context
     * @param pref
     *   The preference key
     * @param DefaultValue
     *   The default value
     *
     * @return
     *   The preference
     */
    public static String getPreference(Context context, String pref, String DefaultValue) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        return preference.getString(pref, DefaultValue);
    }

    /**
     * Set an integer preference.
     *
     * @param context
     *   The current context
     * @param pref
     *   The preference key
     * @param value
     *   The value
     */
    public static void setPreference(Context context, String pref, int value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(pref, value).apply();
    }

    /**
     * Set a string preference.
     *
     * @param context
     *   The current context
     * @param pref
     *   The preference key
     * @param value
     *   The value
     */
    public static void setPreference(Context context, String pref, String value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(pref, value).apply();
    }

}
