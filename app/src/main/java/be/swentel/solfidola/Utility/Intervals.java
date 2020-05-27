package be.swentel.solfidola.Utility;

import java.util.ArrayList;

import be.swentel.solfidola.Model.Interval;

public class Intervals {

    public static ArrayList<Interval> list() {
        ArrayList<Interval> intervals = new ArrayList<>();
        intervals.add(new Interval(1, "Minor second"));
        intervals.add(new Interval(2, "Major second"));
        intervals.add(new Interval(3, "Minor third"));
        intervals.add(new Interval(4, "Major third"));
        intervals.add(new Interval(5, "Perfect fourth"));
        intervals.add(new Interval(6, "Diminished fifth"));
        intervals.add(new Interval(7, "Perfect fifth"));
        intervals.add(new Interval(8, "Minor sixth"));
        intervals.add(new Interval(9, "Major sixth"));
        intervals.add(new Interval(10, "Minor seventh"));
        intervals.add(new Interval(11, "Major seventh"));
        intervals.add(new Interval(12, "Octave"));
        return intervals;
    }

    public static ArrayList<Interval> list(ArrayList<Integer> i) {
        ArrayList<Interval> intervals = new ArrayList<>();

        if (i.contains(1)) {
            intervals.add(new Interval(1, "Minor second"));
        }
        if (i.contains(2)) {
            intervals.add(new Interval(2, "Major second"));
        }
        if (i.contains(3)) {
            intervals.add(new Interval(3, "Minor third"));
        }
        if (i.contains(4)) {
            intervals.add(new Interval(4, "Major third"));
        }
        if (i.contains(5)) {
            intervals.add(new Interval(5, "Perfect fourth"));
        }
        if (i.contains(6)) {
            intervals.add(new Interval(6, "Diminished fifth"));
        }
        if (i.contains(7)) {
            intervals.add(new Interval(7, "Perfect fifth"));
        }
        if (i.contains(8)) {
            intervals.add(new Interval(8, "Minor sixth"));
        }
        if (i.contains(9)) {
            intervals.add(new Interval(9, "Major sixth"));
        }
        if (i.contains(10)) {
            intervals.add(new Interval(10, "Minor seventh"));
        }
        if (i.contains(11)) {
            intervals.add(new Interval(11, "Major seventh"));
        }
        if (i.contains(12)) {
            intervals.add(new Interval(12, "Octave"));
        }
        return intervals;
    }
}
