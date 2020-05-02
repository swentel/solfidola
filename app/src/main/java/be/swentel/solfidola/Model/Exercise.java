package be.swentel.solfidola.Model;

import android.util.Log;

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
     */
    public void prepareData(String data) {
        this.setData(data);

        String[] properties = data.split(";");
        for (String p: properties) {
            String[] keyValues = p.split(":");
            int pos = 0;
            for (String kv: keyValues) {
                String[] values = kv.split(",");
                for (String v: values) {
                    if (pos != 0) {
                        this.addInterval(Integer.parseInt(v));
                        continue;
                    }
                    pos++;
                }
            }
        }
    }

    public void flattenData() {
        String data = "";

        StringBuilder i = new StringBuilder();
        for (int j = 0; j < this.getIntervals().size(); j++) {
            i.append(this.getIntervals().get(j)).append(",");
        }

        if (i.length() > 0) {
            data += "intervals:" + i + ";";
        }

        this.setData(data);
    }

}
