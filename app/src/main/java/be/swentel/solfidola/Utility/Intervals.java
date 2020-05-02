package be.swentel.solfidola.Utility;

import java.util.ArrayList;

import be.swentel.solfidola.Model.Interval;

public class Intervals {

    public static ArrayList<Interval> list() {
        ArrayList<Interval> intervals = new ArrayList<>();
        intervals.add(new Interval(1, "Kl. Secunde"));
        intervals.add(new Interval(2, "Gr. Secunde"));
        intervals.add(new Interval(3, "Kl Terts"));
        intervals.add(new Interval(4, "Gr. Terts"));
        intervals.add(new Interval(5, "Kwart"));
        intervals.add(new Interval(6, "V. Kwint"));
        intervals.add(new Interval(7, "R. Kwint"));
        intervals.add(new Interval(8, "Kl. Sext"));
        intervals.add(new Interval(9, "Gr. Sext"));
        intervals.add(new Interval(10, "Kl. Septiem"));
        intervals.add(new Interval(11, "Gr. Septiem"));
        intervals.add(new Interval(12, "Octaaf"));
        return intervals;
    }

    public static ArrayList<Interval> list(ArrayList<Integer> i) {
        ArrayList<Interval> intervals = new ArrayList<>();

        if (i.contains(1)) {
            intervals.add(new Interval(1, "Kl. Secunde"));
        }
        if (i.contains(2)) {
            intervals.add(new Interval(2, "Gr. Secunde"));
        }
        if (i.contains(3)) {
            intervals.add(new Interval(3, "Kl Terts"));
        }
        if (i.contains(4)) {
            intervals.add(new Interval(4, "Gr. Terts"));
        }
        if (i.contains(5)) {
            intervals.add(new Interval(5, "Kwart"));
        }
        if (i.contains(6)) {
            intervals.add(new Interval(6, "V. Kwint"));
        }
        if (i.contains(7)) {
            intervals.add(new Interval(7, "R. Kwint"));
        }
        if (i.contains(8)) {
            intervals.add(new Interval(8, "Kl. Sext"));
        }
        if (i.contains(9)) {
            intervals.add(new Interval(9, "Gr. Sext"));
        }
        if (i.contains(10)) {
            intervals.add(new Interval(10, "Kl. Septiem"));
        }
        if (i.contains(11)) {
            intervals.add(new Interval(11, "Gr. Septiem"));
        }
        if (i.contains(12)) {
            intervals.add(new Interval(12, "Octaaf"));
        }
        return intervals;
    }
}
