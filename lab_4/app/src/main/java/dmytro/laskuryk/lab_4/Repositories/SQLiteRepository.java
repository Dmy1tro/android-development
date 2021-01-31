package dmytro.laskuryk.lab_4.Repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dmytro.laskuryk.lab_4.Interfaces.INoteRepository;
import dmytro.laskuryk.lab_4.Models.Importance;
import dmytro.laskuryk.lab_4.Models.Note;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SQLiteRepository implements INoteRepository {
    private final DatabaseHelper dbHelper;
    private final SQLiteDatabase db;

    public SQLiteRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    @Override
    public List<Note> getAll() {
        return new GetAllNotes().doInBackground(db);
    }

    @Override
    public List<Note> getByFilter(@Nullable String name, @Nullable Importance importance) {
        name = (name == null || name.isEmpty()) ? null : name;
        if (name == null && importance == null) {
            return getAll();
        }

        FilterParameters parameters = new FilterParameters(db, name, importance);

        return new FilterNotes().doInBackground(parameters);
    }

    @Override
    public void save(Note item) {
        ContentValues cv = getContentValues(item);

        db.insert(DatabaseHelper.TABLE_NOTES, null, cv);
    }

    @Override
    public void update(Note item) {
        ContentValues cv = getContentValues(item);
        String whereClause = getWhereIdClause(item.Id);
        db.update(DatabaseHelper.TABLE_NOTES, cv, whereClause, null);
    }

    @Override
    public void delete(UUID id) {
        String whereClause = getWhereIdClause(id);
        db.delete(DatabaseHelper.TABLE_NOTES, whereClause, null);
    }

    private static String getWhereIdClause(UUID id) {
        return String.format("%s = '%s'", DatabaseHelper.KEY_ID, id.toString());
    }

    private static List<Note> extractNotes(Cursor cursor) {
        List<Note> notes = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();

                note.Id = UUID.fromString(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_ID)));
                note.CreationDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_CreationDate));
                note.AppointmentDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_AppointmentDate));
                note.Name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_Name));
                note.Description = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_Description));
                note.Importance = Importance.parse(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_Importance)));
                note.ImagePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_ImagePath));

                notes.add(note);
            } while (cursor.moveToNext());
        }

        return notes;
    }

    public static ContentValues getContentValues(Note item) {
        ContentValues cv = new ContentValues();

        cv.put(DatabaseHelper.KEY_ID, item.Id.toString());
        cv.put(DatabaseHelper.KEY_CreationDate, item.CreationDate);
        cv.put(DatabaseHelper.KEY_AppointmentDate, item.AppointmentDate);
        cv.put(DatabaseHelper.KEY_Name, item.Name);
        cv.put(DatabaseHelper.KEY_Description, item.Description);
        cv.put(DatabaseHelper.KEY_Importance, item.Importance.getValue());
        cv.put(DatabaseHelper.KEY_ImagePath, item.ImagePath);

        return cv;
    }

    private static String[] allColumns = new String[] {
            DatabaseHelper.KEY_ID,
            DatabaseHelper.KEY_CreationDate,
            DatabaseHelper.KEY_AppointmentDate,
            DatabaseHelper.KEY_Name,
            DatabaseHelper.KEY_Description,
            DatabaseHelper.KEY_Importance,
            DatabaseHelper.KEY_ImagePath
    };

    public static class GetAllNotes extends AsyncTask<SQLiteDatabase, Void, List<Note>> {
        public GetAllNotes() {
            super();
        }

        @Override
        protected List<Note> doInBackground(SQLiteDatabase... databases) {
            SQLiteDatabase db = databases[0];
            Cursor cursor = db.query(DatabaseHelper.TABLE_NOTES, allColumns, null, null, null, null, null);
            List<Note> allNotes = extractNotes(cursor);
            cursor.close();

            return allNotes;
        }
    }

    public static class FilterNotes extends AsyncTask<FilterParameters, Void, List<Note>> {
        public FilterNotes() {
            super();
        }

        @Override
        protected List<Note> doInBackground(FilterParameters... parameters) {
            FilterParameters parameter = parameters[0];
            SQLiteDatabase db = parameter.db;
            String query = String.format("Select * From %s WHERE ", DatabaseHelper.TABLE_NOTES);

            if (parameter.name != null) {
                query += String.format("%s LIKE '%%%s%%' ", DatabaseHelper.KEY_Name, parameter.name);
            }

            if (parameter.importance != null) {
                if (parameter.name != null) { query += " AND ";}
                query += String.format("%s = %d", DatabaseHelper.KEY_Importance, parameter.importance.getValue());
            }

            Cursor cursor = db.rawQuery(query, null);
            List<Note> filteredNotes = extractNotes(cursor);
            cursor.close();

            return filteredNotes;
        }
    }

    protected class FilterParameters {
        public FilterParameters(SQLiteDatabase db, @Nullable String name, @Nullable Importance importance) {
            this.db = db;
            this.name = name;
            this.importance = importance;
        }

        SQLiteDatabase db;
        @Nullable
        String name;
        @Nullable
        Importance importance;
    }
}