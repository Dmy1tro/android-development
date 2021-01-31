package dmytro.laskuryk.lab_4.Repositories;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import dmytro.laskuryk.lab_4.Interfaces.INoteRepository;
import dmytro.laskuryk.lab_4.Models.Importance;
import dmytro.laskuryk.lab_4.Models.Note;

@RequiresApi(api = Build.VERSION_CODES.R)
public class FileRepository implements INoteRepository {
    private static final String File_name = "User_notes.txt";
    private final Gson json_converter;
    private final File root;

    public FileRepository(File root) {
        this.json_converter = new Gson();
        this.root = root;
    }

    @Override
    public List<Note> getAll() {
        List<String> data = getDataFromFile();
        ArrayList<Note> result = new ArrayList<Note>();

        data.forEach(x -> {
            Note note = json_converter.fromJson(x, Note.class);
            result.add(note);
        });

        return result;
    }

    @Override
    public List<Note> getByFilter(@Nullable String name, @Nullable Importance importance) {
        List<Note> filtered = getAll();

        if (name != null && !name.isEmpty()) {
            filtered = filtered.stream().filter(x -> x.Name.contains(name)).collect(Collectors.toList());
        }

        if (importance != null) {
            filtered = filtered.stream().filter(x -> x.Importance == importance).collect(Collectors.toList());
        }

        return filtered;
    }

    @Override
    public void save(Note item) {
        String data = json_converter.toJson(item);
        appendToFile(data);
    }

    @Override
    public void update(Note item) {
        List<Note> all = getAll();
        List<String> data = new ArrayList<String>();
        deleteContentOfFile();

        all.forEach(x -> {
            if (x.Id.equals(item.Id)) {
                x.Name = item.Name;
                x.Description = item.Description;
                x.Importance = item.Importance;
                x.AppointmentDate = item.AppointmentDate;
                x.ImagePath = item.ImagePath;
            }

            String jsonData = json_converter.toJson(x);
            data.add(jsonData);
        });

        appendToFile(data);
    }

    @Override
    public void delete(UUID id) {
        List<Note> all = getAll();
        List<String> data = new ArrayList<String>();
        deleteContentOfFile();

        all.forEach(x -> {
            if (!x.Id.equals(id)) {
                String jsonData = json_converter.toJson(x);
                data.add(jsonData);
            }
        });

        appendToFile(data);
    }

    private File getFile() {
        File notes_folder = new File(root, "Notes_folder");

        if (!notes_folder.exists())
        {
            notes_folder.mkdirs();
        }

        File file = new File(notes_folder, File_name);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    private List<String> getDataFromFile() {
        try {
            return Files.readAllLines(getFile().toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<String>();
        }
    }

    private void appendToFile(String line) {
        try {
            FileWriter fw = new FileWriter(getFile(), true);
            fw.append(line).append("\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendToFile(List<String> lines) {
        try {
            FileWriter fw = new FileWriter(getFile(), true);
            lines.forEach(line -> {
                try {
                    fw.append(line).append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteContentOfFile() {
        try {
            FileWriter fw = new FileWriter(getFile(), false);
            fw.append("");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
