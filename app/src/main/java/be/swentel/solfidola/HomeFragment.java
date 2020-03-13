package be.swentel.solfidola;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.javax.sound.midi.Instrument;
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;

public class HomeFragment extends Fragment {

    private Receiver recv;
    private SoftSynthesizer synth;
    private static String DEBUG_TAG = "solfidola_debug";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
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

        Button play = view.findViewById(R.id.play);
        play.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                long now = System.currentTimeMillis();
                now = -1;
                //long nowPlusOneSecond = now;
                if (action == MotionEvent.ACTION_DOWN) {
                    try {

                        ShortMessage msg = new ShortMessage();
                        msg.setMessage(ShortMessage.NOTE_ON, 0, 60, 127);
                        recv.send(msg, now);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored ) { }
                        msg.setMessage(ShortMessage.NOTE_OFF, 0, 60, 127);
                        recv.send(msg, now);

                        ShortMessage msg2 = new ShortMessage();
                        msg2.setMessage(ShortMessage.NOTE_ON, 0, 62, 127);
                        recv.send(msg2, now);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored ) { }
                        msg2.setMessage(ShortMessage.NOTE_OFF, 0, 62, 127);
                        recv.send(msg2, now);

                        ShortMessage msg3 = new ShortMessage();
                        msg3.setMessage(ShortMessage.NOTE_ON, 0, 64, 127);
                        recv.send(msg3, now);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored ) { }
                        msg3.setMessage(ShortMessage.NOTE_OFF, 0, 64, 127);
                        recv.send(msg3, now);

                        ShortMessage msg4 = new ShortMessage();
                        msg4.setMessage(ShortMessage.NOTE_ON, 0, 60, 127);
                        recv.send(msg4, now);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored ) { }
                        msg4.setMessage(ShortMessage.NOTE_OFF, 0, 60, 127);
                        recv.send(msg4, now);

                    } catch (InvalidMidiDataException e) {
                        Toast.makeText(getContext(), "Down: invalid midi data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (synth != null) {
            synth.close();
        }
    }
}
