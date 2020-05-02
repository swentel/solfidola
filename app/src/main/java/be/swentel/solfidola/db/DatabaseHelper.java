package be.swentel.solfidola.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import be.swentel.solfidola.Model.Exercise;
import be.swentel.solfidola.Model.Record;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATA_TYPE_EXERCISE = "exercise";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "solfidola";

    private static final String TABLE_DATA_NAME = "data";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_DATA = "data";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_DATA_TABLE =
        "CREATE TABLE IF NOT EXISTS " + TABLE_DATA_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_DATA + " TEXT,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * Saves a record.
     *
     * @param record
     *   The record to save.
     */
    private void saveRecord(Record record) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, record.getType());
        values.put(COLUMN_DATA, record.getData());

        if (record.getId() > 0) {
            db.update(TABLE_DATA_NAME, values, COLUMN_ID + "=" + record.getId(), null);
        }
        else {
            long id = db.insert(TABLE_DATA_NAME, null, values);
            record.setId((int) id);
        }
        db.close();
    }

    /**
     * Set record properties.
     *
     *  @param record
     *   The record.
     * @param cursor
     *   The current cursor.
     */
    private void setRecordProperties(Record record, Cursor cursor) {
        record.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
        record.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
        record.setData(cursor.getString(cursor.getColumnIndex(COLUMN_DATA)));
        record.setTimestamp(cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)));
    }

    /**
     * Delete a record.
     *
     * @param id
     *   The record id to delete.
     */
    public void deleteRecordById(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DATA_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * Delete all records by type.
     */
    public void deleteAllRecordsByType(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_DATA_NAME, COLUMN_TYPE + " =?", new String[]{type});
        db.close();
    }

    /**
     * Delete all records.
     */
    public void deleteAllRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_DATA_NAME, null, null);
        db.close();
    }

    /**
     * Get a single record.
     *
     * @param id
     *   The record id.
     *
     * @return Record
     */
    private Record getRecord(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DATA_NAME,
                new String[]{
                        COLUMN_ID,
                        COLUMN_TYPE,
                        COLUMN_DATA,
                        COLUMN_TIMESTAMP
                },
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        Record record = new Record();
        record.setId(0);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            setRecordProperties(record, cursor);
            cursor.close();
        }

        return record;
    }

    /**
     * Get all records for a type.
     *
     * @param type
     *   The type to get the records for.
     *
     * @return <Draft>
     */
    private List<Record> getRecordsByType(String type) {
        List<Record> records = new ArrayList<>();

        // Select query
        String selectQuery = "SELECT * FROM " + TABLE_DATA_NAME + " WHERE " + COLUMN_TYPE + "=? " +
                "ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{type});

        if (cursor.moveToFirst()) {
            do {
                Record record = new Record();
                setRecordProperties(record, cursor);
                records.add(record);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // Return records list
        return records;
    }

    /**
     * Get the number of records by type.
     *
     * @return int
     *   The number of records.
     */
    @SuppressLint("Recycle")
    public int getRecordCountByType(String type) {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor dataCount;
        if (db != null) {
            dataCount = db.rawQuery("select "+ COLUMN_ID + " from " + TABLE_DATA_NAME + " WHERE " + COLUMN_TYPE + "=?", new String[]{type});
            count = dataCount.getCount();
            db.close();
        }
        return count;
    }

    /**
     * Get exercises.
     *
     * @return List<Exercise>
     */
    public List<Exercise> getExercises() {
        List<Exercise> exercises = new ArrayList<>();
        List<Record> records = this.getRecordsByType(DATA_TYPE_EXERCISE);
        for (Record r : records) {
            Exercise e = new Exercise();
            e.setId(r.getId());
            e.setType(DATA_TYPE_EXERCISE);
            e.setTimestamp(r.getTimestamp());
            e.prepareData(r.getData());
            exercises.add(e);
        }
        return exercises;
    }

    /**
     * Get a single exercise.
     *
     * @return List<Exercise>
     */
    public Exercise getExercise(long id) {
        Record r = this.getRecord(id);
        Exercise e = new Exercise();
        e.setId(r.getId());
        e.setType(DATA_TYPE_EXERCISE);
        e.setTimestamp(r.getTimestamp());
        e.prepareData(r.getData());
        return e;
    }

    /**
     * Saves an exercise.
     *
     * @param exercise
     *   An exercise object.
     */
    public void saveExercise(Exercise exercise) {
        saveRecord(exercise);
    }

}
