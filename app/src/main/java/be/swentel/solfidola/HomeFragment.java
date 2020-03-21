package be.swentel.solfidola;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import be.swentel.solfidola.Model.Interval;
import be.swentel.solfidola.Model.Note;
import be.swentel.solfidola.SheetMusicView.MusicBarView;
import be.swentel.solfidola.SheetMusicView.NoteData;
import be.swentel.solfidola.SheetMusicView.NoteView;
import be.swentel.solfidola.Utility.Preferences;
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

    private int interval = 1;
    private Receiver receiver;
    private SoftSynthesizer synthesizer;
    private MusicBarView bar;
    private TableLayout choicesContainer;
    private ArrayList<Note> randomNotes = new ArrayList<>();
    private ArrayList<Interval> intervals = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        try {
            SF2Soundbank sf = new SF2Soundbank(requireActivity().getAssets().open("SmallTimGM6mb.sf2"));
            synthesizer = new SoftSynthesizer();
            synthesizer.open();
            synthesizer.loadAllInstruments(sf);

            /*Instrument[] insts = synth.getLoadedInstruments();
            for (Instrument ins : insts) {
                Log.d(DEBUG_TAG, ins.getName() + " " + ins.getPatch().getBank() + " " + ins.getPatch().getProgram() + " ");
            }*/

            synthesizer.getChannels()[0].programChange(0);
            synthesizer.getChannels()[1].programChange(1);
            receiver = synthesizer.getReceiver();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading soundfont: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (MidiUnavailableException e) {
            Toast.makeText(getContext(), "Midi not available: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        bar = view.findViewById(R.id.bar);
        choicesContainer = view.findViewById(R.id.choicesContainer);
        setIntervals();
        drawNotes();
        drawChoices();

        final Button play = view.findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(randomNotes);
            }
        });

        Button refresh = view.findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRefresh();
            }
        });

        /*Button listen = view.findViewById(R.id.listen);
        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("spotify:album:0sNOF9WDwhWunNAHPD3Baj"));
                intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + requireContext().getPackageName()));
                startActivity(intent);
            }
        });*/

        //choicesContainer.post(new Runnable() {
        //    public void run() {
        //        play(randomNotes);
        //    }
        //});
    }

    private NoteView getNote(NoteData.NoteValue value) {
        NoteData data = new NoteData(value, NoteData.NoteDuration.FOURTH);
        NoteView note = new NoteView(getContext(), data);
        note.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0));
        return note;
    }

    private void doRefresh() {
        setIntervals();
        drawNotes();
        drawChoices();
    }

    private void drawChoices() {
        choicesContainer.removeAllViews();

        Button b;
        int numberOfChoices = Preferences.getPreference(getContext(), "numberOfChoices", 4);
        ArrayList<Button> choices = new ArrayList<>();

        // Solution
        int solution = 0;
        for (int i = 0; i < intervals.size(); i++) {
            if (intervals.get(i).getInterval() == interval) {
                solution = i;
                break;
            }
        }

        b = new Button(getContext());
        b.setText(intervals.get(solution).getLabel());
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                if (Preferences.getPreference(getContext(), "autoRefresh", true)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doRefresh();
                        }
                    }, Preferences.getPreference(getContext(), "autoRefreshDelay", 2) * 1000);
                }
            }
        });
        choices.add(b);
        numberOfChoices--;
        intervals.remove(solution);

        // Others.
        while (numberOfChoices != 0) {
            b = new Button(getContext());

            Random randomGenerator = new Random();
            int randomIndex = randomGenerator.nextInt(intervals.size());
            b.setText(intervals.get(randomIndex).getLabel());
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }
            });
            choices.add(b);
            intervals.remove(randomIndex);
            numberOfChoices--;
        }

        TableRow row = null;
        int numberOfButtons = 0;
        Collections.shuffle(choices);
        for (Button choice: choices) {

            if ((numberOfButtons % 2) == 0) {
                row = new TableRow(getContext());
                choicesContainer.addView(row);
                row.addView(choice);
            }
            else {
                row.addView(choice);
            }

            numberOfButtons++;
        }
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

        randomNotes.clear();

        // Add c as first one.
        randomNotes.add(notes.get(0));
        notes.remove(0);

        // Add another one.
        Random randomGenerator = new Random();
        int randomIndex = randomGenerator.nextInt(notes.size());
        randomNotes.add(notes.get(randomIndex));
        notes.remove(randomIndex);

        // Get interval.
        interval = randomNotes.get(1).getMidiValue() - randomNotes.get(0).getMidiValue();

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
            receiver.send(msg, TIMESTAMP);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored ) { }
            msg.setMessage(ShortMessage.NOTE_OFF, 0, notes.get(0).getMidiValue(), 127);
            receiver.send(msg, TIMESTAMP);

            msg.setMessage(ShortMessage.NOTE_ON, 0, notes.get(1).getMidiValue(), 127);
            receiver.send(msg, TIMESTAMP);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored ) { }
            msg.setMessage(ShortMessage.NOTE_OFF, 0, notes.get(1).getMidiValue(), 127);
            receiver.send(msg, TIMESTAMP);
        }
        catch (InvalidMidiDataException e) {
            Toast.makeText(getContext(), "Down: invalid midi data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void setIntervals() {
        intervals.clear();
        intervals.add(new Interval(2, "Secunde"));
        intervals.add(new Interval(4, "Terts"));
        intervals.add(new Interval(5, "Kwart"));
        intervals.add(new Interval(7, "Kwint"));
        intervals.add(new Interval(9, "Sext"));
        intervals.add(new Interval(11, "Septiem"));
        intervals.add(new Interval(12, "Octaaf"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (synthesizer != null) {
            synthesizer.close();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.numberOfChoices:
                final CharSequence[] numberOfChoices = {"2", "3", "4", "5", "6"};
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireContext());
                mBuilder.setTitle(R.string.number_of_choices);
                mBuilder.setSingleChoiceItems(numberOfChoices, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Preferences.setPreference(getContext(), "numberOfChoices", Integer.parseInt(numberOfChoices[i].toString()));
                        dialogInterface.dismiss();
                        doRefresh();
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
