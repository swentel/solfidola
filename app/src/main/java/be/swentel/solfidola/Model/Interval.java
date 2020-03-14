package be.swentel.solfidola.Model;

public class Interval {

    private int interval;
    private String label;

    public Interval(int interval, String label) {
        this.setInterval(interval);
        this.setLabel(label);
    }

    public int getInterval() {
        return interval;
    }

    private void setInterval(int interval) {
        this.interval = interval;
    }

    public String getLabel() {
        return label;
    }

    private void setLabel(String label) {
        this.label = label;
    }
}
