package dmytro.laskuryk.lab_4.Adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import dmytro.laskuryk.lab_4.Models.Note;
import dmytro.laskuryk.lab_4.R;
import dmytro.laskuryk.lab_4.Repositories.ImageRepository;
import dmytro.laskuryk.lab_4.Repositories.RepositoryFactory;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NoteAdapter extends ArrayAdapter<Note> {
    private LayoutInflater inflater;
    private int layout;
    private List<Note> displayedNotes;
    private List<Note> temporaryNotes;
    private List<Predicate<Note>> filters;
    private ImageRepository imageRepository;

    public NoteAdapter(@NonNull Context context, int resource, List<Note> notes) {
        super(context, resource, notes);
        this.displayedNotes = notes;
        this.temporaryNotes = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
        this.imageRepository = RepositoryFactory.getImageRepository(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Note note = displayedNotes.get(position);

        viewHolder.nameView.setText(note.Name);
        viewHolder.timeView.setText(note.CreationDate);

        if (note.hasImage()) {
            Bitmap bitmapImage = this.imageRepository.get(note.ImagePath);
            viewHolder.mainImage.setImageBitmap(bitmapImage);
        } else {
            viewHolder.mainImage.setImageResource(R.drawable.empty_img);
        }

        viewHolder.iconImage.setImageResource(note.Importance.getResource());

        return convertView;
    }

    public void addFilter(Predicate<Note> predicate) {
        filters.add(predicate);
    }

    public void removeFilter(Predicate<Note> predicate) {
        filters.remove(predicate);
    }

    public void applyFilters() {
        if (temporaryNotes.isEmpty()) {
            displayedNotes.forEach(x -> temporaryNotes.add(x));
        }

        if (filters.isEmpty()) {
            resetAllFilter();
            return;
        }

        displayedNotes.clear();

        temporaryNotes.forEach(x -> {
            if (filters.stream().allMatch(f -> f.test(x))) {
                displayedNotes.add(x);
            }
        });

        notifyDataSetChanged();
    }

    public void resetAllFilter() {
        displayedNotes.clear();
        temporaryNotes.forEach(x -> displayedNotes.add(x));
        temporaryNotes.clear();
        filters.clear();

        notifyDataSetChanged();
    }

    private class ViewHolder {
        final ImageView mainImage;
        final ImageView iconImage;
        final TextView nameView, timeView;
        ViewHolder(View view){
            mainImage = (ImageView)view.findViewById(R.id.main_image);
            iconImage = view.findViewById(R.id.icon_image);
            nameView = view.findViewById(R.id.name);
            timeView = view.findViewById(R.id.time);
        }
    }
}
