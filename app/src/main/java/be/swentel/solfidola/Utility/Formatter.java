package be.swentel.solfidola.Utility;

import android.annotation.SuppressLint;

public class Formatter {

    @SuppressLint("DefaultLocale")
    public static String elapsedTime(int seconds) {
        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int minutes = seconds / SECONDS_IN_A_MINUTE;
        seconds -= minutes * SECONDS_IN_A_MINUTE;

        int hours = minutes / MINUTES_IN_AN_HOUR;
        minutes -= hours * MINUTES_IN_AN_HOUR;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

}
