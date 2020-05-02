package be.swentel.solfidola;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import be.swentel.solfidola.Model.Exercise;
import be.swentel.solfidola.db.DatabaseHelper;

import static be.swentel.solfidola.MainActivity.CREATE_EXERCISE;

public class ExerciseList extends Fragment implements View.OnClickListener {

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

        view.findViewById(R.id.actionButton).setOnClickListener(this);

        RelativeLayout layout = view.findViewById(R.id.layout_root);
        ListView exerercise = view.findViewById(R.id.exercise_list);
        TextView empty = view.findViewById(R.id.no_exercises);

        DatabaseHelper db = new DatabaseHelper(requireContext());
        List<Exercise> exercises = db.getExercises();

        if (exercises.size() == 0) {
            exerercise.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
            empty.setOnClickListener(this);
        }
        else {
            ExerciseListAdapter adapter = new ExerciseListAdapter(requireContext(), exercises, callback, layout);
            exerercise.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        Intent createExercise = new Intent(getActivity(), ExerciseForm.class);
        ((Activity) requireActivity()).startActivityForResult(createExercise, CREATE_EXERCISE);
    }

}
