package dmytro.laskuryk.lab_4.Repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;

import dmytro.laskuryk.lab_4.Models.Note;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NoteDb";
    public static final String TABLE_NOTES = "Notes";

    public static final String KEY_ID = "_id";
    public static final String KEY_CreationDate = "creationDate";
    public static final String KEY_AppointmentDate = "appointmentDate";
    public static final String KEY_Name = "name";
    public static final String KEY_Description = "description";
    public static final String KEY_Importance = "importance";
    public static final String KEY_ImagePath = "imagePath";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(
                "create table %s (" +
                "%s text primary key, " + // id
                "%s text, " + // creationDate
                "%s text, " + // appointmentDate
                "%s text, " + // name
                "%s text, " + // description
                "%s integer, " + // importance
                "%s text " + // imagePath
                ")",
                TABLE_NOTES,
                KEY_ID,
                KEY_CreationDate,
                KEY_AppointmentDate,
                KEY_Name,
                KEY_Description,
                KEY_Importance,
                KEY_ImagePath
        ));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        List<Note> allNotes = new SQLiteRepository.GetAllNotes().doInBackground(db);

        db.execSQL("drop table if exists " + TABLE_NOTES);
        onCreate(db);

        allNotes.forEach(x -> {
            ContentValues cv = SQLiteRepository.getContentValues(x);
            db.insert(DatabaseHelper.TABLE_NOTES, null, cv);
        });
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
