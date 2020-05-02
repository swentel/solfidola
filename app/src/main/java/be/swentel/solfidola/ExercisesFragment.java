package be.swentel.solfidola;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import be.swentel.solfidola.Model.Exercise;
import be.swentel.solfidola.db.DatabaseHelper;

public class ExercisesFragment extends Fragment {

    private OnExercisesChangedListener callback;

    public void OnExercisesChangedListener(OnExercisesChangedListener callback) {
        this.callback = callback;
    }

    public interface OnExercisesChangedListener {
        void onExercisesChanged();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_excercises, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().setTitle(R.string.menu_exercise);

        RelativeLayout layout = view.findViewById(R.id.layout_root);
        ListView exerercise = view.findViewById(R.id.exercise_list);
        TextView empty = view.findViewById(R.id.no_exercises);

        DatabaseHelper db = new DatabaseHelper(requireContext());
        List<Exercise> exercises = db.getExercises();

        if (exercises.size() == 0) {
            exerercise.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        }
        else {
            ExerciseListAdapter adapter = new ExerciseListAdapter(requireContext(), exercises, callback, layout);
            exerercise.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

    }

}
