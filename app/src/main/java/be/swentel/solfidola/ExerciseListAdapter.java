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
import be.swentel.solfidola.Utility.Formatter;
import be.swentel.solfidola.Utility.Intervals;
import be.swentel.solfidola.db.DatabaseHelper;

import static be.swentel.solfidola.Solfege.PLAYBACK_HARMONIC;

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
        TextView intervals;
        TextView intervalType;
        TextView root;
        TextView rounds;
        TextView playbackMode;
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
            holder.intervals = convertView.findViewById(R.id.list_intervals);
            holder.intervalType = convertView.findViewById(R.id.list_interval_type);
            holder.root = convertView.findViewById(R.id.list_interval_root);
            holder.rounds = convertView.findViewById(R.id.list_interval_rounds);
            holder.playbackMode = convertView.findViewById(R.id.list_playback_mode);
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

            // Intervals
            ArrayList<Interval> intervals = Intervals.list(exercise.getIntervals(), false, false);
            ArrayList<String> text = new ArrayList<>();
            for (Interval i : intervals) {
                text.add(i.getLabel());
            }
            if (exercise.addRandomInterval()) {
                text.add(context.getString(R.string.random_interval));
            }
            holder.intervals.setText(String.format(context.getString(R.string.intervals), text.toString().replace("[", "").replace("]", "")));

            // Root.
            String[] rootArray = context.getResources().getStringArray(R.array.root_options);
            String root_value = rootArray[exercise.getRoot()];
            holder.root.setText(String.format(context.getString(R.string.root_value), root_value));

            // Rounds.
            String[] roundsArray = context.getResources().getStringArray(R.array.round_options);
            String rounds_value = roundsArray[exercise.getRoundsLimit()];
            holder.rounds.setText(String.format(context.getString(R.string.rounds_value), rounds_value));

            // Interval type.
            String interval;
            switch (exercise.getIntervalType()) {
                case 2:
                    interval = context.getString(R.string.random);
                    break;
                case 1:
                    interval = context.getString(R.string.desc);
                    break;
                case 0:
                default:
                    interval = context.getString(R.string.asc);
                    break;
            }
            holder.intervalType.setText(String.format(context.getString(R.string.interval_type), interval));

            // Playback mode.
            String playbackMode = context.getString(R.string.melodic);
            if (exercise.getPlaybackMode() == PLAYBACK_HARMONIC) {
                playbackMode = context.getString(R.string.harmonic);
            }
            holder.playbackMode.setText(String.format(context.getString(R.string.playback_mode), playbackMode));

            // Stats.
            int success = exercise.getRounds() - exercise.getMistakes();
            String elapsed = Formatter.elapsedTime(exercise.getTimer());
            String stats = String.format(context.getString(R.string.stats), success, exercise.getRounds(), exercise.getReplays(), elapsed);
            holder.stats.setText(stats);

            // Published
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatOut = new SimpleDateFormat("dd/MM/yyyy HH:mm");
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