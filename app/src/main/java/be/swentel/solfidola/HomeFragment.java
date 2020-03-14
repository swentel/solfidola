package be.swentel.solfidola;

import android.os.Bundle;
import android.view.LayoutInflater;
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
import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;

public class HomeFragment extends Fragment {

    private boolean isPlaying = false;
    private Receiver recv;
    private SoftSynthesizer synth;

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

        Button play = view.findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    isPlaying = true;
                    play();
                }
            }
        });
    }

    /**
     * Play notes.
     */
    private void play() {
        long TIMESTAMP = -1;

        try {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(ShortMessage.NOTE_ON, 0, 60, 127);
            recv.send(msg, TIMESTAMP);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored ) { }
            msg.setMessage(ShortMessage.NOTE_OFF, 0, 60, 127);
            recv.send(msg, TIMESTAMP);

            msg.setMessage(ShortMessage.NOTE_ON, 0, 62, 127);
            recv.send(msg, TIMESTAMP);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored ) { }
            msg.setMessage(ShortMessage.NOTE_OFF, 0, 62, 127);
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
