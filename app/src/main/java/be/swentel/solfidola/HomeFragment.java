package be.swentel.solfidola;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import be.swentel.solfidola.Model.Note;
import be.swentel.solfidola.SheetMusicView.MusicBarView;
import be.swentel.solfidola.SheetMusicView.NoteData;
import be.swentel.solfidola.SheetMusicView.NoteView;
import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;

import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.HIGHER_A;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.HIGHER_B;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.HIGHER_C;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.LOWER_C;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.LOWER_D;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.LOWER_E;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.LOWER_F;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.LOWER_G;

public class HomeFragment extends Fragment {

    private boolean isPlaying = false;
    private Receiver recv;
    private SoftSynthesizer synth;
    private MusicBarView bar;
    private ArrayList<Note> randomNotes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            SF2Soundbank sf = new SF2Soundbank(requireActivity().getAssets().open("SmallTimGM6mb.sf2"));
            synth = new SoftSynthesizer();
            synth.open();
            synth.loadAllInstruments(sf);

            /*Instrument[] insts = synth.getLoadedInstruments();
            for (Instrument ins : insts) {
                Log.d(DEBUG_TAG, ins.getName() + " " + ins.getPatch().getBank() + " " + ins.getPatch().getProgram() + " ");
            }*/

            synth.getChannels()[0].programChange(0);
            synth.getChannels()[1].programChange(1);
            recv = synth.getReceiver();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading soundfont: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (MidiUnavailableException e) {
            Toast.makeText(getContext(), "Midi not available: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        bar = view.findViewById(R.id.bar);
        drawNotes();

        Button play = view.findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    isPlaying = true;
                    play(randomNotes);
                }
            }
        });

        Button refresh = view.findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawNotes();
            }
        });
    }

    private NoteView getNote(NoteData.NoteValue value) {
        NoteData data = new NoteData(value, NoteData.NoteDuration.FOURTH);
        NoteView note = new NoteView(getContext(), data);
        note.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0));
        return note;
    }

    /**
     * Draw notes.
     */
    private void drawNotes() {
        bar.removeAllViews();

        ArrayList<Note> notes = new ArrayList<>();
        notes.add(new Note(60, LOWER_C));
        notes.add(new Note(62, LOWER_D));
        notes.add(new Note(64, LOWER_E));
        notes.add(new Note(65, LOWER_F));
        notes.add(new Note(67, LOWER_G));
        notes.add(new Note(69, HIGHER_A));
        notes.add(new Note(71, HIGHER_B));
        notes.add(new Note(72, HIGHER_C));

        Random randomGenerator = new Random();
        int numberOfElements = 2;

        randomNotes.clear();
        for (int i = 0; i < numberOfElements; i++) {
            int randomIndex = randomGenerator.nextInt(notes.size());
            randomNotes.add(notes.get(randomIndex));
            notes.remove(randomIndex);
        }

        for (Note n : randomNotes) {
            NoteView note = getNote(n.getNoteViewValue());
            bar.addView(note);
        }
    }

    /**
     * Play notes.
     */
    private void play(ArrayList<Note> notes) {
        long TIMESTAMP = -1;

        try {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(ShortMessage.NOTE_ON, 0, notes.get(0).getMidiValue(), 127);
            recv.send(msg, TIMESTAMP);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored ) { }
            msg.setMessage(ShortMessage.NOTE_OFF, 0, notes.get(0).getMidiValue(), 127);
            recv.send(msg, TIMESTAMP);

            msg.setMessage(ShortMessage.NOTE_ON, 0, notes.get(1).getMidiValue(), 127);
            recv.send(msg, TIMESTAMP);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored ) { }
            msg.setMessage(ShortMessage.NOTE_OFF, 0, notes.get(1).getMidiValue(), 127);
            recv.send(msg, TIMESTAMP);
        }
        catch (InvalidMidiDataException e) {
            Toast.makeText(getContext(), "Down: invalid midi data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        isPlaying = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (synth != null) {
            synth.close();
        }
    }
}
