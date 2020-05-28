package be.swentel.solfidola.Utility;

import java.util.ArrayList;
import java.util.Random;

import be.swentel.solfidola.Model.Interval;

public class Intervals {

    public static ArrayList<Interval> list() {
        ArrayList<Interval> intervals = new ArrayList<>();
        intervals.add(new Interval(0, "Unison"));
        intervals.add(new Interval(1, "Minor second"));
        intervals.add(new Interval(2, "Major second"));
        intervals.add(new Interval(3, "Minor third"));
        intervals.add(new Interval(4, "Major third"));
        intervals.add(new Interval(5, "Perfect fourth"));
        intervals.add(new Interval(6, "Tritone"));
        intervals.add(new Interval(7, "Perfect fifth"));
        intervals.add(new Interval(8, "Minor sixth"));
        intervals.add(new Interval(9, "Major sixth"));
        intervals.add(new Interval(10, "Minor seventh"));
        intervals.add(new Interval(11, "Major seventh"));
        intervals.add(new Interval(12, "Octave"));
        return intervals;
    }

    public static ArrayList<Interval> list(ArrayList<Integer> i, boolean addRandomInterval) {
        ArrayList<Interval> intervals = new ArrayList<>();
        ArrayList<Interval> intervalList = list();
        ArrayList<Interval> intervalListToRemove = new ArrayList<>();

        for (Interval interval : intervalList) {
            if (i.contains(interval.getInterval())) {
                intervals.add(interval);
                intervalListToRemove.add(interval);
            }
        }

        if (addRandomInterval) {
            intervalList.removeAll(intervalListToRemove);
            if (intervalList.size() > 0) {
                Random randomGenerator = new Random();
                int randomIndex = randomGenerator.nextInt(intervalList.size());
                intervals.add(intervalList.get(randomIndex));
            }
        }

        return intervals;
    }
}
