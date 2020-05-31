package be.swentel.solfidola;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;
import org.kaldi.Assets;
import org.kaldi.Model;
import org.kaldi.RecognitionListener;
import org.kaldi.SpeechRecognizer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import be.swentel.solfidola.Model.Exercise;
import be.swentel.solfidola.Model.Interval;
import be.swentel.solfidola.Model.Note;
import be.swentel.solfidola.Model.SolfidolaInstrument;
import be.swentel.solfidola.SheetMusicView.ClefView;
import be.swentel.solfidola.SheetMusicView.MusicBarView;
import be.swentel.solfidola.SheetMusicView.NoteData;
import be.swentel.solfidola.SheetMusicView.NoteView;
import be.swentel.solfidola.SheetMusicView.SignatureView;
import be.swentel.solfidola.Utility.Intervals;
import be.swentel.solfidola.Utility.Preferences;
import be.swentel.solfidola.db.DatabaseHelper;
import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.javax.sound.midi.Instrument;
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;

import static android.content.Context.AUDIO_SERVICE;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.HIGHER_A;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.HIGHER_B;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.HIGHER_C;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.LOWER_C;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.LOWER_D;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.LOWER_E;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.LOWER_F;
import static be.swentel.solfidola.SheetMusicView.NoteData.NoteValue.LOWER_G;

public class Solfege extends Fragment implements RecognitionListener {

    private Exercise e = null;
    private int interval = 1;
    private Receiver receiver;
    private SoftSynthesizer synthesizer;
    private Model model;
    private SpeechRecognizer recognizer;
    private boolean SetupListenerDone = false;
    private boolean speechMatchIsChecking = false;
    private TextView playbackMode;
    private TextView intervalType;
    private TextView instrument;
    private TextView speech;
    private TextView speechOutput;
    private TextView exercise;
    private MusicBarView bar;
    int intervalTypeSelected = 0;
    private boolean volumeOn = true;
    private boolean useMic = false;
    private ImageButton mic;
    private LinearLayout layout;
    private int startTime = 0;
    private boolean hasClicked = false;
    private TableLayout choicesContainer;
    private SoundPool soundPool;
    private boolean soundPoolLoaded = false;
    private int soundIdSuccess;
    private int soundIdWrong;
    private float volume;
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private static final int MAX_STREAMS = 2;
    private List<Button> choices = new ArrayList<>();
    private ArrayList<Note> randomNotes = new ArrayList<>();
    private ArrayList<Interval> intervals = new ArrayList<>();
    private static final int PLAYBACK_MELODIC = 0;
    private static final int DEFAULT_PROGRAM = 0;
    private static final int DEFAULT_CHOICES = 4;
    private static final String DEFAULT_INSTRUMENT = "Standard";
    private static final String DEFAULT_SCALE = "Cmaj";
    private static final int RECORD_AUDIO_INT = 52;

    static {
        System.loadLibrary("kaldi_jni");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.solfege, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getString(R.string.menu_solfege);

        // Start exercise.
        if (getArguments() != null) {
            int exerciseId = getArguments().getInt("exercise");
            if (exerciseId > 0) {
                exercise = view.findViewById(R.id.exercise);
                DatabaseHelper db = new DatabaseHelper(getContext());
                e = db.getExercise(exerciseId);
                title = getString(R.string.exercising);
            }
        }
        requireActivity().setTitle(title);

        layout = view.findViewById(R.id.root);
        speech = view.findViewById(R.id.speech);
        speechOutput = view.findViewById(R.id.speechOutput);
        bar = view.findViewById(R.id.bar);
        playbackMode = view.findViewById(R.id.playbackMode);
        intervalType = view.findViewById(R.id.intervalType);
        instrument = view.findViewById(R.id.instrument);
        choicesContainer = view.findViewById(R.id.choicesContainer);
        setHasOptionsMenu(true);
        setDisplay();

        try {
            AudioManager audioManager = (AudioManager) requireActivity().getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                int volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (volumeLevel == 0) {
                    volumeOn = false;
                    Snackbar.make(layout, getString(R.string.volume_off), Snackbar.LENGTH_LONG).show();
                }
            }
        }
        catch (Exception ignored) {}

        try {
            SF2Soundbank sf = new SF2Soundbank(requireActivity().getAssets().open("Solfidola.sf2"));
            synthesizer = new SoftSynthesizer();
            synthesizer.open();
            synthesizer.loadAllInstruments(sf);
            receiver = synthesizer.getReceiver();
            setProgram();
        }
        catch (IOException e) {
            Toast.makeText(getContext(), "Error loading soundfont: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        catch (MidiUnavailableException e) {
            Toast.makeText(getContext(), "Midi not available: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        ImageButton play = view.findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (e != null && volumeOn) {
                    e.setReplays(e.getReplays() + 1);
                    saveExercise();
                }
                volumeOn = true;
                play(randomNotes);
            }
        });

        mic = view.findViewById(R.id.mic);
        if (e != null) {
            useMic = Preferences.getPreference(getContext(), "useMic", false);
            mic.setOnClickListener(new onMicClickListener());
            setMicState();
        }
        else {
            mic.setVisibility(View.GONE);
        }

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

        setup(false);
        playNotes();
    }

    private void playNotes() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                play(randomNotes);
            }
        }, 500);
    }

    private NoteView getNote(NoteData.NoteValue value) {
        NoteData data = new NoteData(value, NoteData.NoteDuration.FOURTH);
        NoteView note = new NoteView(getContext(), data);
        note.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0));
        return note;
    }

    private void setup(boolean clearBar) {
        if (clearBar) {
            bar.removeAllViews();
        }

        hasClicked = false;
        if (e != null) {
            e.setAttempts(e.getAttempts() + 1);
            saveExercise();
        }

        setPlaybackMode();
        setIntervalType();
        setIntervals();
        drawClef();
        drawSignature();
        drawNotes();
        drawChoices();
    }

    private void setDisplay() {
        if (Preferences.getPreference(getContext(), "display_on", false)) {
            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else {
            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void doRenew() {
        setup(true);
        playNotes();
    }

    private void saveExercise() {
        e.flattenData();
        DatabaseHelper db = new DatabaseHelper(getContext());
        db.saveExercise(e);
    }

    private void setPlaybackMode() {
        String mode = getString(R.string.melodic);
        boolean sleep = Preferences.getPreference(getContext(), "playback", PLAYBACK_MELODIC) == PLAYBACK_MELODIC;
        if (!sleep) {
            mode = getString(R.string.harmonic);
        }
        playbackMode.setText(String.format(getString(R.string.playback_mode), mode.toLowerCase()));
    }

    private void setIntervalType() {
        String interval;

        if (e != null) {
            intervalTypeSelected = e.getIntervalType();
        }
        else {
            intervalTypeSelected = Preferences.getPreference(getContext(), "intervalType", 0);
        }

        switch (intervalTypeSelected) {
            case 2:
                interval = getString(R.string.random);
                break;
            case 1:
                interval = getString(R.string.desc);
                break;
            case 0:
            default:
                interval = getString(R.string.asc);
                break;
        }
        this.intervalType.setText(String.format(getString(R.string.interval_type), interval));
    }

    private void setProgram() {
        instrument.setText(String.format(getString(R.string.instrument_selected), Preferences.getPreference(getContext(), "instrument", DEFAULT_INSTRUMENT)));
        synthesizer.getChannels()[0].programChange(Preferences.getPreference(getContext(), "program", DEFAULT_PROGRAM));
    }

    private void drawChoices() {
        choicesContainer.removeAllViews();

        Button b;
        int numberOfChoices = Preferences.getPreference(getContext(), "numberOfChoices", DEFAULT_CHOICES);

        // Limit choices to the number of intervals in the exercise.
        if (e != null) {
            numberOfChoices = e.getIntervals().size();
            if (e.addRandomInterval()) {
                numberOfChoices++;
            }
        }

        choices = new ArrayList<>();

        // Solution
        int solution = 0;
        for (int i = 0; i < intervals.size(); i++) {
            if (intervals.get(i).getInterval() == interval) {
                solution = i;
                break;
            }
        }

        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(40, 0, 0, 0);

        b = new Button(getContext());
        b.setText(intervals.get(solution).getLabel());
        b.setTag(intervals.get(solution).getInterval());
        b.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        b.setTextColor(getResources().getColor(R.color.buttonDarkText));
        b.setPadding(30, 0, 30, 0);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedBack(true);
                v.setBackgroundColor(getResources().getColor(R.color.right));
                if (Preferences.getPreference(getContext(), "autoRefresh", true)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doRenew();
                        }
                    }, Preferences.getPreference(getContext(), "autoRefreshDelay", 2) * 250);
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
            b.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            b.setTextColor(getResources().getColor(R.color.buttonDarkText));
            b.setPadding(30, 0, 30, 0);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (e != null) {
                        hasClicked = true;
                        e.setMistakes(e.getMistakes() + 1);
                        saveExercise();
                    }
                    feedBack(false);
                    v.setBackgroundColor(getResources().getColor(R.color.wrong));
                }
            });
            b.setTag(intervals.get(randomIndex).getInterval());
            choices.add(b);
            intervals.remove(randomIndex);
            numberOfChoices--;
        }

        TableRow row = null;
        int numberOfButtons = 0;

        // In an exercise, sort by interval asc, otherwise random.
        if (e != null) {
            Collections.sort(choices, new Comparator<Button>() {
                @Override
                public int compare(Button b1, Button b2) {
                    Integer i1 = (Integer) b1.getTag();
                    Integer i2 = (Integer) b2.getTag();
                    if (i1.equals(i2)) {
                        return 0;
                    }
                    return (i1 < i2) ? -1 : 1;

                }
            });
        }
        else {
            Collections.shuffle(choices);
        }
        for (Button choice: choices) {

            if ((numberOfButtons % 2) == 0) {
                row = new TableRow(getContext());
                row.setPadding(0, 0, 0, 40);
                choicesContainer.addView(row);
                row.addView(choice);
            }
            else {
                row.addView(choice);
                choice.setLayoutParams(params);
            }

            numberOfButtons++;
        }
    }

    /**
     * Draw clef.
     */
    private void drawClef() {
        ClefView clef = new ClefView(getContext());
        bar.addView(clef);
    }

    private void drawSignature() {
        String scale = Preferences.getPreference(getContext(), "scale", DEFAULT_SCALE);
        if (scale.equals("Cmin")) {
            // TODO use constants.
            SignatureView s = new SignatureView(getContext(), "flat", 7);
            bar.addView(s);
            s = new SignatureView(getContext(), "flat", 10);
            bar.addView(s);
            s = new SignatureView(getContext(), "flat",6);
            bar.addView(s);
        }
    }

    /**
     * Draw notes.
     */
    private void drawNotes() {
        boolean showBar = Preferences.getPreference(getContext(), "show_bar", true);
        if (e != null) {
            showBar = e.showBar();
        }

        ArrayList<Note> notes = new ArrayList<>();
        String scale = Preferences.getPreference(getContext(), "scale", DEFAULT_SCALE);
        boolean removeUnison = true;

        if (e != null) {
            notes.add(new Note(60, LOWER_C));

            if (e.getIntervals().contains(0)) {
                removeUnison = false;
            }

            if (e.getIntervals().contains(1)) {
                notes.add(new Note(61, LOWER_C));
            }
            if (e.getIntervals().contains(2)) {
                notes.add(new Note(62, LOWER_D));
            }
            if (e.getIntervals().contains(3)) {
                notes.add(new Note(63, LOWER_D));
            }
            if (e.getIntervals().contains(4)) {
                notes.add(new Note(64, LOWER_E));
            }
            if (e.getIntervals().contains(5)) {
                notes.add(new Note(65, LOWER_F));
            }
            if (e.getIntervals().contains(6)) {
                notes.add(new Note(66, LOWER_G));
            }
            if (e.getIntervals().contains(7)) {
                notes.add(new Note(67, LOWER_G));
            }
            if (e.getIntervals().contains(8)) {
                notes.add(new Note(68, HIGHER_A));
            }
            if (e.getIntervals().contains(9)) {
                notes.add(new Note(69, HIGHER_A));
            }
            if (e.getIntervals().contains(10)) {
                notes.add(new Note(70, HIGHER_B));
            }
            if (e.getIntervals().contains(11)) {
                notes.add(new Note(71, HIGHER_B));
            }
            if (e.getIntervals().contains(12)) {
                notes.add(new Note(72, HIGHER_C));
            }
        }
        else {
            if (scale.equals("Cmin")) {
                notes.add(new Note(60, LOWER_C));
                notes.add(new Note(62, LOWER_D));
                notes.add(new Note(63, LOWER_E));
                notes.add(new Note(65, LOWER_F));
                notes.add(new Note(67, LOWER_G));
                notes.add(new Note(68, HIGHER_A));
                notes.add(new Note(70, HIGHER_B));
                notes.add(new Note(72, HIGHER_C));
            }
            else {
                notes.add(new Note(60, LOWER_C));
                notes.add(new Note(62, LOWER_D));
                notes.add(new Note(64, LOWER_E));
                notes.add(new Note(65, LOWER_F));
                notes.add(new Note(67, LOWER_G));
                notes.add(new Note(69, HIGHER_A));
                notes.add(new Note(71, HIGHER_B));
                notes.add(new Note(72, HIGHER_C));
            }
        }

        randomNotes.clear();
        Random randomGenerator = new Random();

        // First note
        randomNotes.add(notes.get(0));

        if (removeUnison) {
            notes.remove(0);
        }

        // Second note.
        int randomIndex = randomGenerator.nextInt(notes.size());
        randomNotes.add(notes.get(randomIndex));
        notes.remove(randomIndex);

        // Get interval.
        interval = randomNotes.get(1).getMidiValue() - randomNotes.get(0).getMidiValue();

        if (showBar) {
            bar.setVisibility(View.VISIBLE);
            for (Note n : randomNotes) {
                NoteView note = getNote(n.getNoteViewValue());
                bar.addView(note);
            }
        }
        else {
            bar.setVisibility(View.GONE);
        }

    }

    /**
     * Play notes.
     */
    private void play(ArrayList<Note> notes) {
        long TIMESTAMP = -1;
        boolean melodic = Preferences.getPreference(getContext(), "playback", PLAYBACK_MELODIC) == PLAYBACK_MELODIC;

        if (startTime == 0) {
            startTime = (int) System.currentTimeMillis()/1000;
        }

        try {

            int channel = 0;
            if (melodic) {
                ShortMessage msg = new ShortMessage();
                msg.setMessage(ShortMessage.NOTE_ON, channel, notes.get(0).getMidiValue(), 127);
                receiver.send(msg, TIMESTAMP);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored ) { }

                msg.setMessage(ShortMessage.NOTE_OFF, channel, notes.get(0).getMidiValue(), 127);
                receiver.send(msg, TIMESTAMP);

                msg.setMessage(ShortMessage.NOTE_ON, channel, notes.get(1).getMidiValue(), 127);
                receiver.send(msg, TIMESTAMP);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored ) { }

                msg.setMessage(ShortMessage.NOTE_OFF, channel, notes.get(1).getMidiValue(), 127);
                receiver.send(msg, TIMESTAMP);
            }
            else {
                ShortMessage msg = new ShortMessage();
                msg.setMessage(ShortMessage.NOTE_ON, channel, notes.get(0).getMidiValue(), 127);
                receiver.send(msg, TIMESTAMP);

                msg.setMessage(ShortMessage.NOTE_ON, channel, notes.get(1).getMidiValue(), 127);
                receiver.send(msg, TIMESTAMP);

                try {
                    Thread.sleep(2500);
                } catch (InterruptedException ignored ) { }

                msg.setMessage(ShortMessage.NOTE_OFF, channel, notes.get(0).getMidiValue(), 127);
                receiver.send(msg, TIMESTAMP);

                msg.setMessage(ShortMessage.NOTE_OFF, channel, notes.get(1).getMidiValue(), 127);
                receiver.send(msg, TIMESTAMP);
            }

        }
        catch (InvalidMidiDataException e) {
            Toast.makeText(getContext(), "Down: invalid midi data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setIntervals() {
        intervals.clear();

        if (e != null) {
            intervals = Intervals.list(e.getIntervals(), e.addRandomInterval());
            exercise.setVisibility(View.VISIBLE);
            ArrayList<String> text = new ArrayList<>();
            for (Interval i : intervals) {
                if (e.getIntervals().contains(i.getInterval())) {
                    text.add(i.getLabel());
                }
            }
            if (e.addRandomInterval()) {
                text.add(getString(R.string.random_interval));
            }
            exercise.setText(String.format(getString(R.string.exercise), text.toString().replace("[", "").replace("]", "")));
        }
        else {
            intervals = Intervals.list();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (synthesizer != null) {
            synthesizer.close();
        }

        if (e != null) {

            if (!hasClicked) {
                e.setAttempts(e.getAttempts() - 1);
            }

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            e.setTimestamp(format.format(new Date()));
            e.setTimer(e.getTimer() + ((int) System.currentTimeMillis()/1000 - startTime));
            saveExercise();
        }

        stopListening();
        stopSoundPool();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.solfege_menu, menu);

        if (e != null) {

            MenuItem choicesItem = menu.findItem(R.id.numberOfChoices);
            if (choicesItem != null) {
                choicesItem.setVisible(false);
            }

            MenuItem scaleItem = menu.findItem(R.id.scale);
            if (scaleItem != null) {
                scaleItem.setVisible(false);
            }

        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireContext());
        AlertDialog mDialog;

        switch (item.getItemId()) {
            case R.id.playback:
                final CharSequence[] playback = {getString(R.string.melodic), getString(R.string.harmonic)};
                mBuilder.setTitle(R.string.playback);
                mBuilder.setSingleChoiceItems(playback, Preferences.getPreference(getContext(), "playback", PLAYBACK_MELODIC), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Preferences.setPreference(getContext(), "playback", i);
                        dialogInterface.dismiss();
                        doRenew();
                    }
                });

                mDialog = mBuilder.create();
                mDialog.show();
                return true;
            case R.id.display:
                mBuilder.setTitle(R.string.display);
                boolean[] checked = new boolean[] {Preferences.getPreference(getContext(), "display_on", false)};
                mBuilder.setMultiChoiceItems(new String[]{getString(R.string.display_on)}, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        Preferences.setPreference(getContext(), "display_on", b);
                        dialogInterface.dismiss();
                        setDisplay();

                        String message;
                        if (b) {
                            message = requireContext().getString(R.string.display_stay_on);
                        }
                        else {
                            message = requireContext().getString(R.string.display_stay_off);
                        }
                        Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
                    }
                });
                mDialog = mBuilder.create();
                mDialog.show();
                return true;
            case R.id.sheetMusic:
                mBuilder.setTitle(R.string.bar);
                boolean[] displayOn = new boolean[] {Preferences.getPreference(getContext(), "show_bar", true)};
                mBuilder.setMultiChoiceItems(new String[]{getString(R.string.show_bar)}, displayOn, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        Preferences.setPreference(getContext(), "show_bar", b);
                        dialogInterface.dismiss();
                        doRenew();
                    }
                });
                mDialog = mBuilder.create();
                mDialog.show();
                return true;
            case R.id.scale:
                final CharSequence[] scales = {"Cmaj", "Cmin"};
                mBuilder.setTitle(R.string.scale);
                int currentScale = 0;
                if (Preferences.getPreference(getContext(), "scale", DEFAULT_SCALE).equals("Cmin")) {
                    currentScale = 1;
                }
                mBuilder.setSingleChoiceItems(scales, currentScale, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Preferences.setPreference(getContext(), "scale", scales[i].toString());
                        dialogInterface.dismiss();
                        doRenew();
                    }
                });
                mDialog = mBuilder.create();
                mDialog.show();
                return true;
            case R.id.interval:
                final CharSequence[] intervalChoices = {getString(R.string.asc), getString(R.string.desc), getString(R.string.random)};
                mBuilder.setTitle(R.string.interval);
                int currentInterval = Preferences.getPreference(getContext(), "intervalType", 0);
                mBuilder.setSingleChoiceItems(intervalChoices, currentInterval, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Preferences.setPreference(getContext(), "intervalType", i);
                        dialogInterface.dismiss();
                        doRenew();
                    }
                });
                mDialog = mBuilder.create();
                mDialog.show();
                return true;
            case R.id.instrument:
                int delta = 0;
                int selectedIndex = 0;
                int currentProgram = Preferences.getPreference(getContext(), "program", DEFAULT_PROGRAM);
                List<String> items = new ArrayList<>();
                final List<SolfidolaInstrument> SolfidolaInstruments = new ArrayList<>();
                List<Instrument> instruments = Arrays.asList(synthesizer.getLoadedInstruments());

                Collections.sort(instruments, new Comparator<Instrument>() {
                    @Override
                    public int compare(Instrument i1, Instrument i2) {
                        return  i1.getName().compareTo(i2.getName());
                    }
                });

                for (Instrument instrument : instruments) {
                    SolfidolaInstrument si = new SolfidolaInstrument();
                    si.setProgram(instrument.getPatch().getProgram());
                    si.setLabel(instrument.getName());
                    SolfidolaInstruments.add(si);
                    items.add(instrument.getName());

                    if (si.getProgram() == currentProgram) {
                        selectedIndex = delta;
                    }
                    delta++;
                }

                final CharSequence[] SolfidolaChoiceItems = items.toArray(new CharSequence[0]);

                mBuilder.setTitle(R.string.instrument);
                mBuilder.setSingleChoiceItems(SolfidolaChoiceItems, selectedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Preferences.setPreference(getContext(), "program", SolfidolaInstruments.get(i).getProgram());
                        Preferences.setPreference(getContext(), "instrument", SolfidolaInstruments.get(i).getLabel());
                        dialogInterface.dismiss();
                        setProgram();
                    }
                });
                mDialog = mBuilder.create();
                mDialog.show();
                return true;
            case R.id.numberOfChoices:
                final CharSequence[] numberOfChoices = {"2", "3", "4", "5", "6"};
                mBuilder.setTitle(R.string.number_of_choices);
                int currentSelection = Preferences.getPreference(getContext(), "numberOfChoices", DEFAULT_CHOICES) - 2;
                mBuilder.setSingleChoiceItems(numberOfChoices, currentSelection, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Preferences.setPreference(getContext(), "numberOfChoices", Integer.parseInt(numberOfChoices[i].toString()));
                        dialogInterface.dismiss();
                        doRenew();
                    }
                });
                mDialog = mBuilder.create();
                mDialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Mic listener.
    class onMicClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            useMic = !useMic;
            Preferences.setPreference(getContext(), "useMic", useMic);
            setMicState();
        }
    }

    private void stopSoundPool() {
        if (soundPoolLoaded) {
            soundPool.release();
        }
    }

    private void setupSoundPool() {

        AudioManager audioManager = (AudioManager) requireActivity().getSystemService(AUDIO_SERVICE);

        // Current volume Index of particular stream type.
        float currentVolumeIndex = audioManager.getStreamVolume(streamType);

        // Get the maximum volume index for a particular stream type.
        float maxVolumeIndex  = audioManager.getStreamMaxVolume(streamType);

        // Volume (0 --> 1)
        volume = currentVolumeIndex / maxVolumeIndex;

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        requireActivity().setVolumeControlStream(streamType);

        // For Android SDK >= 21
        if (Build.VERSION.SDK_INT >= 21 ) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder= new SoundPool.Builder();
            builder.setAudioAttributes(audioAttributes).setMaxStreams(MAX_STREAMS);

            soundPool = builder.build();
        }
        // for Android SDK < 21
        else {
            // SoundPool(int maxStreams, int streamType, int srcQuality)
            //noinspection deprecation
            soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        // When Sound Pool load complete.
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPoolLoaded = true;
            }
        });

        soundIdSuccess = this.soundPool.load(getContext(), R.raw.success,1);
        soundIdWrong = this.soundPool.load(getContext(), R.raw.wrong,1);
    }

    /**
     * Set the mic state.
     */
    private void setMicState() {
        String usesMic = getString(R.string.no);
        speech.setVisibility(View.VISIBLE);
        if (useMic) {
            if (requestPermission()) {
                usesMic = getString(R.string.yes);
                setupSoundPool();
                setSpeechOutput(getString(R.string.setup));
                mic.setBackgroundResource(R.drawable.mic_on);
                startListening(false);
            }
        }
        else {
            stopSoundPool();
            stopListening();
            mic.setBackgroundResource(R.drawable.mic_off);
        }
        speech.setText(String.format(getString(R.string.speech_info), usesMic));
    }

    private void setMicStateError(String error) {
        useMic = false;
        Preferences.setPreference(getContext(), "useMic", useMic);
        setMicState();
        String message = String.format(getString(R.string.error_setup), error);
        final Snackbar snack = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE);
        snack.setAction(getString(R.string.close), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snack.dismiss();
                    }
                }
        );
        snack.show();

    }

    private void startListening(boolean setupDone) {

        if (setupDone) {
            SetupListenerDone = true;
        }

        if (!SetupListenerDone) {
            new SetupListener(this).execute();
        }
        else {
            try {
                recognizer = new SpeechRecognizer(model);
                recognizer.addListener(this);
                recognizer.startListening();
                setSpeechOutput(getString(R.string.listening));
            }
            catch (IOException e) {
                setMicStateError(e.getMessage());
            }

        }
    }

    private static class SetupListener extends AsyncTask<Void, Void, Exception> {
        WeakReference<Solfege> fragmentReference;

        SetupListener(Solfege fragment) {
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(fragmentReference.get().requireContext());
                File assetDir = assets.syncAssets();
                fragmentReference.get().model = new Model(assetDir.toString() + "/model-android");
            }
            catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                fragmentReference.get().setMicStateError(result.getMessage());
            }
            else {
                fragmentReference.get().startListening(true);
            }
        }
    }

    private void stopListening() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
        speechOutput.setVisibility(View.GONE);
    }

    private void setSpeechOutput(String s) {
        speechOutput.setVisibility(View.VISIBLE);
        speechOutput.setText(s);
    }

    private void analyzeSpeechResult(String s, boolean exact) {
        String match = "";

        //Debug.debug("exact" + exact + " - " + s);

        try {
            JSONObject o = new JSONObject(s);
            if (exact) {
                if (o.has("text")) {
                    match = o.getString("text");
                    checkSolutionFromSpeech(match);
                }
            }
        /*else {
            if (o.has("partial")) {
                text = o.getString("partial");
            }
        }*/
        }
        catch (JSONException e) {
            //Debug.debug(e.getMessage());
        }

        if (match.length() > 0) {
            String text;
            //noinspection ConstantConditions
            if (exact) {
                text = String.format(getString(R.string.match_exact), match);
            }
            else {
                text = String.format(getString(R.string.match_partial), match);
            }
            setSpeechOutput(text);
        }
    }

    private void checkSolutionFromSpeech(String s) {
        int speechInterval = -1;

        //Debug.debug("Match: " + s);

        if (speechMatchIsChecking) {
            return;
        }
        speechMatchIsChecking = true;

        switch (s) {
            case "prime":
            case "unison":
                speechInterval = 0;
                break;
            case "minor second":
                speechInterval = 1;
                break;
            case "major second":
                speechInterval = 2;
                break;
            case "minor third":
            case "augmented second":
                speechInterval = 3;
                break;
            case "major third":
                speechInterval = 4;
                break;
            case "perfect fourth":
                speechInterval = 5;
                break;
            case "tritone":
            case "try tone":
            case "diminished fifth":
            case "augmented fourth":
                speechInterval = 6;
                break;
            case "perfect fifth":
                speechInterval = 7;
                break;
            case "minor six":
            case "minor sixth":
            case "augmented fifth":
                speechInterval = 8;
                break;
            case "major six":
            case "major sixth":
            case "diminished seventh":
                speechInterval = 9;
                break;
            case "minor seven":
            case "minor seventh":
                speechInterval = 10;
                break;
            case "major seven":
            case "major seventh":
                speechInterval = 11;
                break;
            case "octave":
                speechInterval = 12;
                break;
        }

        if (speechInterval != -1) {
            try {
                boolean foundButton = false;

                for (Button b: choices) {
                    Integer buttonInterval = (Integer) b.getTag();
                    if (buttonInterval.equals(speechInterval)) {
                        foundButton = true;
                        b.performClick();
                        break;
                    }
                }

                if (!foundButton) {
                    feedBack(false);
                }
            }
            catch (Exception ignored) {}
        }

        if (s.equals("play") || s.equals("replay")) {
            if (e != null && volumeOn) {
                e.setReplays(e.getReplays() + 1);
                saveExercise();
            }
            volumeOn = true;
            play(randomNotes);
        }

        speechMatchIsChecking = false;
    }

    @Override
    public void onPartialResult(String s) {
        analyzeSpeechResult(s,false);
    }

    @Override
    public void onResult(String s) {
        analyzeSpeechResult(s,true);
    }

    @Override
    public void onError(Exception e) {
        String message = String.format(getString(R.string.error_listener), e.getMessage());
        final Snackbar snack = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE);
        snack.setAction(getString(R.string.close), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snack.dismiss();
                }
            }
        );
        snack.show();
    }

    @Override
    public void onTimeout() {
        Snackbar.make(layout, getString(R.string.timeout), Snackbar.LENGTH_INDEFINITE).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RECORD_AUDIO_INT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setMicState();
            }
        }
    }

    private boolean requestPermission() {
        boolean isGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        if (!isGranted) {
            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_INT);
        }
        return isGranted;
    }

    private void feedBack(boolean correctResponse) {
        if (useMic && soundPoolLoaded) {
            if (correctResponse) {
                soundPool.play(soundIdSuccess,volume, volume, 1, 0, 1f);
            }
            else {
                soundPool.play(soundIdWrong,volume, volume, 1, 0, 1f);
            }
        }
    }


}
