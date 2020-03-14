package be.swentel.solfidola.Model;

import be.swentel.solfidola.SheetMusicView.NoteData;

public class Note {

    private int midiValue;
    private NoteData.NoteValue noteViewValue;

    public Note(int midiValue, NoteData.NoteValue noteValue) {
        this.setMidiValue(midiValue);
        this.setNoteViewValue(noteValue);
    }

    public int getMidiValue() {
        return midiValue;
    }

    private void setMidiValue(int midiValue) {
        this.midiValue = midiValue;
    }

    public NoteData.NoteValue getNoteViewValue() {
        return noteViewValue;
    }

    private void setNoteViewValue(NoteData.NoteValue noteViewValue) {
        this.noteViewValue = noteViewValue;
    }

}
