package be.swentel.solfidola;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.swentel.solfidola.Model.Exercise;
import be.swentel.solfidola.Model.Interval;
import be.swentel.solfidola.Utility.Intervals;
import be.swentel.solfidola.db.DatabaseHelper;

public class ExerciseListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<Exercise> exercises;
    private LayoutInflater mInflater;
    private RelativeLayout layout;

    ExerciseListAdapter(Context context, List<Exercise> exercises, RelativeLayout layout) {
        this.context = context;
        this.exercises = exercises;
        this.layout = layout;
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return exercises.size();
    }

    public Exercise getItem(int position) {
        return exercises.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void onClick(View view) { }

    public static class ViewHolder {
        int position;
        TextView label;
        TextView date;
        TextView stats;
        Button play;
        Button delete;
        LinearLayout row;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_exercise, null);
            holder = new ViewHolder();
            holder.row = convertView.findViewById(R.id.list_item_row);
            holder.label = convertView.findViewById(R.id.list_label);
            holder.stats = convertView.findViewById(R.id.list_stats);
            holder.date = convertView.findViewById(R.id.list_date);
            holder.play = convertView.findViewById(R.id.play);
            holder.delete = convertView.findViewById(R.id.delete);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Exercise exercise = exercises.get(position);
        if (exercise != null) {

            holder.position = position;

            // Color of row
            int color = context.getResources().getColor(R.color.listRowBackgroundColor);
            holder.row.setBackgroundColor(color);

            // Label
            ArrayList<Interval> intervals = Intervals.list(exercise.getIntervals(), false);
            ArrayList<String> text = new ArrayList<>();
            for (Interval i : intervals) {
                text.add(i.getLabel());
            }
            if (exercise.addRandomInterval()) {
                text.add(context.getString(R.string.random_interval));
            }
            holder.label.setText(String.format(context.getString(R.string.exercise), text.toString().replace("[", "").replace("]", "")));

            // Stats.
            int success = exercise.getAttempts() - exercise.getMistakes();
            String stats = String.format(context.getString(R.string.stats), success, exercise.getAttempts(), exercise.getReplays(), exercise.getTimer());
            holder.stats.setText(stats);

            // Published
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatOut = new SimpleDateFormat("dd MM yyyy HH:mm");
            try {
                holder.date.setVisibility(View.VISIBLE);
                Date result = formatIn.parse(exercise.getTimestamp());
                holder.date.setText(String.format(context.getString(R.string.exercise_last_test), formatOut.format(result)));
            }
            catch (ParseException ignored) {
                holder.date.setVisibility(View.GONE);
            }

            holder.play.setOnClickListener(new OnPlayClickListener(position));
            holder.delete.setOnClickListener(new OnDeleteClickListener(position));

        }

        return convertView;
    }

    // Update listener.
    class OnPlayClickListener implements OnClickListener {

        int position;

        OnPlayClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Exercise exercise = exercises.get(this.position);
            ((MainActivity)context).startExercise(exercise.getId());
        }
    }

    // Delete listener.
    class OnDeleteClickListener implements OnClickListener {

        int position;

        OnDeleteClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final Exercise e = exercises.get(this.position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.exercise_delete_confirm));
            builder.setPositiveButton(context.getString(R.string.delete),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    DatabaseHelper db = new DatabaseHelper(context);
                    db.deleteRecordById(e.getId());
                    exercises.remove(position);
                    notifyDataSetChanged();
                    Snackbar.make(layout, context.getString(R.string.exercise_deleted), Snackbar.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

}