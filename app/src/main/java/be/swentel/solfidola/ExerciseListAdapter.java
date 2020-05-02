package be.swentel.solfidola;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.Date;
import java.util.List;

import be.swentel.solfidola.Model.Exercise;
import be.swentel.solfidola.db.DatabaseHelper;

public class ExerciseListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<Exercise> exercises;
    private LayoutInflater mInflater;
    private RelativeLayout layout;
    private ExercisesFragment.OnExercisesChangedListener callback;

    ExerciseListAdapter(Context context, List<Exercise> exercises, ExercisesFragment.OnExercisesChangedListener callback, RelativeLayout layout) {
        this.context = context;
        this.exercises = exercises;
        this.callback = callback;
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

            // Color of row.
            int color = context.getResources().getColor(R.color.listRowBackgroundColor);
            holder.row.setBackgroundColor(color);

            // Label
            String label = "";
            /*if (draft.getName().length() > 0) {
                label = draft.getName();
            }
            else if (draft.getBody().length() > 0) {
                label = draft.getBody();
            }
            if (label.length() > 40) {
                label = label.substring(0, 40) + " ...";
            }*/
            holder.label.setText(label);

            // Published.
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatOut = new SimpleDateFormat("dd MM yyyy HH:mm");
            try {
                holder.date.setVisibility(View.VISIBLE);
                Date result = formatIn.parse(exercise.getTimestamp());
                //holder.date.setText(String.format(context.getString(R.string.draft_last_edit), draft.getType(), formatOut.format(result)));
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
            Intent startActivity = null;
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
            final Exercise draft = exercises.get(this.position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.exercise_delete_confirm));
            builder.setPositiveButton(context.getString(R.string.delete),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    DatabaseHelper db = new DatabaseHelper(context);
                    db.deleteRecordById(draft.getId());
                    exercises.remove(position);
                    notifyDataSetChanged();
                    callback.onExercisesChanged();
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