package be.swentel.solfidola.Utility;

import android.util.Log;

public class Debug {

    public static String debug(String message) {
        String DEBUG_TAG = "solfidola_debug";
        Log.d(DEBUG_TAG, message);
        return message + "\n";
    }

}
