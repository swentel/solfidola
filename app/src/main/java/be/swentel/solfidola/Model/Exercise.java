package be.swentel.solfidola.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static be.swentel.solfidola.db.DatabaseHelper.DATA_TYPE_EXERCISE;

public class Exercise extends Record {

    private int attempts = 0;
    private int mistakes = 0;
    private int timer = 0;
    private int replays = 0;
    private boolean showBar = false;
    private ArrayList<Integer> intervals = new ArrayList<>();

    public Exercise() {
        this.setType(DATA_TYPE_EXERCISE);
    }

    public ArrayList<Integer> getIntervals() {
        return intervals;
    }

    public void addInterval(Integer interval) {
        intervals.add(interval);
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public int getReplays() {
        return replays;
    }

    public void setReplays(int replays) {
        this.replays = replays;
    }

    public int getMistakes() {
        return mistakes;
    }

    public void setMistakes(int mistakes) {
        this.mistakes = mistakes;
    }

    /**
     * Whether to show the bar or not.
     *
     * @return boolean
     */
    public boolean showBar() {
        return showBar;
    }

    /**
     * Set the showBar value.
     *
     * @param showBar
     *   Whether to show the bar or not.
     */
    public void setShowBar(boolean showBar) {
        this.showBar = showBar;
    }

    /**
     * Prepare data.
     *
     * @param data
     *   The data from the storage.
     */
    public void prepareData(String data) {
        this.setData(data);
        try {
            JSONObject o = new JSONObject(data);

            if (o.has("intervals")) {
                JSONArray intervals = o.getJSONArray("intervals");
                for (int i = 0; i < intervals.length(); i++) {
                    this.addInterval(intervals.getInt(i));
                }
            }

            if (o.has("showBar")) {
                this.setShowBar(o.getBoolean("showBar"));
            }

            if (o.has("attempts")) {
                this.setAttempts(o.getInt("attempts"));
            }

            if (o.has("mistakes")) {
                this.setMistakes(o.getInt("mistakes"));
            }

            if (o.has("replays")) {
                this.setReplays(o.getInt("replays"));
            }

            if (o.has("timer")) {
                this.setTimer(o.getInt("timer"));
            }
        }
        catch (JSONException ignored) { }
    }

    /**
     * Flat data.
     */
    public void flattenData() {
        JSONObject o = new JSONObject();
        JSONArray i = new JSONArray();

        for (int j = 0; j < this.getIntervals().size(); j++) {
            i.put(this.getIntervals().get(j));
        }
        try {
            o.put("showBar", this.showBar());
            o.put("intervals", i);
            o.put("timer", this.getTimer());
            o.put("replays", this.getReplays());
            o.put("mistakes", this.getMistakes());
            o.put("attempts", this.getAttempts());

        }
        catch (JSONException ignored) { }
        this.setData(o.toString());
    }

}
