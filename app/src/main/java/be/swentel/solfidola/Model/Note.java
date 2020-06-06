package be.swentel.solfidola.Model;

import be.swentel.solfidola.SheetMusicView.NoteData;

public class Note {

    private int midiValue;
    private int rootIdValue;
    private NoteData.NoteValue noteViewValue;

    public Note(int midiValue, int rootId, NoteData.NoteValue noteValue) {
        this.setMidiValue(midiValue);
        this.setRootIdValue(rootId);
        this.setNoteViewValue(noteValue);
    }

    public int getRootIdValue() {
        return rootIdValue;
    }

    public void setRootIdValue(int rootIdValue) {
        this.rootIdValue = rootIdValue;
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
