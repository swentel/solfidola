package be.swentel.solfidola.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static be.swentel.solfidola.Solfege.PLAYBACK_MELODIC;
import static be.swentel.solfidola.db.DatabaseHelper.DATA_TYPE_EXERCISE;

public class Exercise extends Record {

    private int attempts = 0;
    private int mistakes = 0;
    private int timer = 0;
    private int replays = 0;
    // 0 = ascending, 1 = descending, 2 = random
    private int intervalType = 0;
    private boolean showBar = false;
    private boolean randomInterval = false;
    private ArrayList<Integer> intervals = new ArrayList<>();
    private int playbackMode = PLAYBACK_MELODIC;

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

    public boolean addRandomInterval() {
        return randomInterval;
    }

    public void setRandomInterval(boolean randomInterval) {
        this.randomInterval = randomInterval;
    }

    public int getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(int intervalType) {
        this.intervalType = intervalType;
    }

    public boolean showBar() {
        return showBar;
    }

    public void setShowBar(boolean showBar) {
        this.showBar = showBar;
    }

    public int getPlaybackMode() {
        return playbackMode;
    }

    public void setPlaybackMode(int playbackMode) {
        this.playbackMode = playbackMode;
    }

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

            if (o.has("intervalType")) {
                this.setIntervalType(o.getInt("intervalType"));
            }

            if (o.has("showBar")) {
                this.setShowBar(o.getBoolean("showBar"));
            }

            if (o.has("randomInterval")) {
                this.setRandomInterval(o.getBoolean("randomInterval"));
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

            if (o.has("playbackMode")) {
                this.setPlaybackMode(o.getInt("playbackMode"));
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
            o.put("randomInterval", this.addRandomInterval());
            o.put("intervals", i);
            o.put("intervalType", this.getIntervalType());
            o.put("timer", this.getTimer());
            o.put("replays", this.getReplays());
            o.put("mistakes", this.getMistakes());
            o.put("attempts", this.getAttempts());
            o.put("playbackMode", this.getPlaybackMode());

        }
        catch (JSONException ignored) { }
        this.setData(o.toString());
    }

}
