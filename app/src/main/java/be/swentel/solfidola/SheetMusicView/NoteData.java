package be.swentel.solfidola.SheetMusicView;

public class NoteData {

    public enum NoteValue {
        LOWER_B(0),
        LOWER_C(1),
        LOWER_D(2),
        LOWER_E(3),
        LOWER_F(4),
        LOWER_G(5),
        HIGHER_A(6),
        HIGHER_B(7),
        HIGHER_C(8),
        HIGHER_D(9),
        HIGHER_E(10),
        HIGHER_F(11),
        HIGHER_G(12),
        DOUBLE_HIGH_A(13),
        DOUBLE_HIGH_B(14);

        private int value;
        NoteValue(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public boolean greaterThanHigherB() {
            return value >= DOUBLE_HIGH_B.getValue();
        }
    }

    public enum NoteDuration {
        SIXTEENTH,
        EIGHTH,
        FOURTH,
        HALF,
        WHOLE
    }

    private NoteValue noteValue;
    private NoteDuration noteDuration;

    public NoteData(NoteValue noteValue, NoteDuration noteDuration) {
        this.noteValue = noteValue;
        this.noteDuration = noteDuration;
    }

    NoteValue getNoteValue() {
        return this.noteValue;
    }

    void setNoteValue(NoteValue in) {
        this.noteValue = in;
    }

    NoteDuration getNoteDuration() {
        return this.noteDuration;
    }

    void setNoteDuration(NoteDuration in) {
        this.noteDuration = in;
    }
}
