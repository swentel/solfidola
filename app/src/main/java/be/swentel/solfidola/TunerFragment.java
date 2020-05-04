package be.swentel.solfidola;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class TunerFragment extends Fragment {

    private TextView pitch;
    private TextView note;
    private TextView instruction;
    private TextView feedback;
    private static final int RECORD_AUDIO_INT = 52;
    private boolean isRunning = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tuner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pitch = view.findViewById(R.id.pitch);
        note = view.findViewById(R.id.note);
        instruction = view.findViewById(R.id.instruction);
        feedback = view.findViewById(R.id.feedback);
        if (requestPermission()) {
            setNote();
            startTuner();
        }
    }

    private void setNote() {
        instruction.setText(String.format(getString(R.string.sing_note), "C"));
    }

    private void render(float pitchInHz) {

        // Set pitch.
        pitch.setText(String.format(getString(R.string.pitch_value), pitchInHz));

        boolean ok = false;

        // Set note.
        if(pitchInHz >= 110 && pitchInHz < 123.47) {
            note.setText("A");
        }
        else if(pitchInHz >= 123.47 && pitchInHz < 130.81) {
            note.setText("B");
        }
        else if(pitchInHz >= 130.81 && pitchInHz < 146.83) {
            note.setText("C");
            ok = true;
        }
        else if(pitchInHz >= 146.83 && pitchInHz < 164.81) {
            note.setText("D");
        }
        else if(pitchInHz >= 164.81 && pitchInHz <= 174.61) {
            note.setText("E");
        }
        else if(pitchInHz >= 174.61 && pitchInHz < 185) {
            note.setText("F");
        }
        else if(pitchInHz >= 185 && pitchInHz < 196) {
            note.setText("G");
        }

        if (ok) {
            feedback.setText(R.string.feedback_nice);
        }
        else {
            feedback.setText(R.string.feedback_not_close);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RECORD_AUDIO_INT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTuner();
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

    private void startTuner() {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                Activity ac = getActivity();
                if (ac != null) {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isRunning && pitchInHz != -1) {
                                render(pitchInHz);
                            }
                        }
                    });
                }
            }
        };

        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher").start();
        isRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isRunning = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }
}
