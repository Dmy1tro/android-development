package dmytro.laskuryk.lab_4.Models;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Note {
    public UUID Id;
    public String CreationDate;
    public String AppointmentDate;
    public String Name;
    public String Description;
    public Importance Importance;
    public String ImagePath;

    public static Note create(String name,
                              String description,
                              Importance importance,
                              LocalDateTime appointmentDate,
                              @Nullable String imagePath) {
        Note note = new Note();

        note.Id = UUID.randomUUID();
        note.CreationDate = LocalDateTime.now().format(getRequiredDateTimeFormatter());
        note.AppointmentDate = appointmentDate.format(getRequiredDateTimeFormatter());
        note.Name = name;
        note.Description = description;
        note.Importance = importance;
        note.ImagePath = imagePath;

        return note;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Note) {
            return this.Id.equals(((Note)obj).Id);
        }

        return false;
    }

    public static DateTimeFormatter getRequiredDateTimeFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    public static Pattern getDateTimePattern() {
        return Pattern.compile("\\d{4}-(0\\d|1[0-2])-([0-2]\\d|3[0-1])\\s([0-1]\\d|2[0-3]):([0-5]\\d)$");
    }

    public void setData(String name,
                        String description,
                        Importance importance,
                        LocalDateTime appointmentDate,
                        @Nullable String imagePath) {
        this.Name = name;
        this.AppointmentDate = appointmentDate.format(getRequiredDateTimeFormatter());
        this.Description = description;
        this.Importance = importance;
        this.ImagePath = imagePath;
    }

    public boolean hasImage() {
        return ImagePath != null && !ImagePath.isEmpty();
    }
}
