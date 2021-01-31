package dmytro.laskuryk.lab_4.Interfaces;

import androidx.annotation.Nullable;

import java.io.Closeable;
import java.util.List;
import java.util.UUID;

import dmytro.laskuryk.lab_4.Models.Importance;
import dmytro.laskuryk.lab_4.Models.Note;

public interface INoteRepository {
    List<Note> getAll();
    List<Note> getByFilter(@Nullable String name, @Nullable Importance importance);
    void save(Note item);
    void update(Note item);
    void delete(UUID id);
}
