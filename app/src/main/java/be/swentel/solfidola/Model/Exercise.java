package be.swentel.solfidola.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static be.swentel.solfidola.db.DatabaseHelper.DATA_TYPE_EXERCISE;

public class Exercise extends Record {

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
            JSONArray intervals = o.getJSONArray("intervals");
            for (int i = 0; i < intervals.length(); i++) {
                this.addInterval(intervals.getInt(i));
            }
        }
        catch (JSONException ignored) { }
    }

    public void flattenData() {
        JSONObject o = new JSONObject();
        JSONArray i = new JSONArray();

        for (int j = 0; j < this.getIntervals().size(); j++) {
            i.put(this.getIntervals().get(j));
        }
        try {
            o.put("intervals", i);
        }
        catch (JSONException ignored) { }
        this.setData(o.toString());
    }

}