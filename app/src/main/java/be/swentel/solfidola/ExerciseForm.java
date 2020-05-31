package be.swentel.solfidola;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import be.swentel.solfidola.Model.Exercise;
import be.swentel.solfidola.Model.Interval;
import be.swentel.solfidola.Utility.Debug;
import be.swentel.solfidola.Utility.Intervals;
import be.swentel.solfidola.db.DatabaseHelper;

public class ExerciseForm extends AppCompatActivity {

    ScrollView root;
    LinearLayout intervalContainer;
    private ArrayList<Interval> intervals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_form);

        root = findViewById(R.id.root);
        intervalContainer = findViewById(R.id.intervalContainer);
        intervals = Intervals.list();

        for (int i = 0; i < intervals.size(); i++) {
            CheckBox ch = new CheckBox(this);
            ch.setText(intervals.get(i).getLabel());
            ch.setId(i);
            ch.setTextSize(15);
            ch.setPadding(0, 10, 0, 10);
            ch.setTextColor(getResources().getColor(R.color.textColor));
            intervalContainer.addView(ch);
        }

        Button start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Exercise e = new Exercise();

                CheckBox showBar = findViewById(R.id.showBar);
                if (showBar.isChecked()) {
                    e.setShowBar(true);
                }

                CheckBox randomInterval = findViewById(R.id.addRandomInterval);
                if (randomInterval.isChecked()) {
                    e.setRandomInterval(true);
                }

                CheckBox checkbox;
                for (int j = 0; j < intervals.size(); j++) {
                    checkbox = findViewById(j);
                    if (checkbox != null && checkbox.isChecked()) {
                        e.addInterval(intervals.get(j).getInterval());
                    }
                }

                int intervalType = 0;
                e.setIntervalType(intervalType);
                RadioButton desc = findViewById(R.id.intervalDesc);
                if (desc.isChecked()) {
                    intervalType = 1;
                }
                RadioButton random = findViewById(R.id.intervalRandom);
                if (random.isChecked()) {
                    intervalType = 2;
                }
                e.setIntervalType(intervalType);

                if (e.getIntervals().size() > 1) {
                    e.flattenData();
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    db.saveExercise(e);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("exercise", e.getId());
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
                else {
                    Snackbar.make(root, getString(R.string.interval_select), Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
