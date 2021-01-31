package dmytro.laskuryk.lab_6.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.List;

import dmytro.laskuryk.lab_6.Models.Song;
import dmytro.laskuryk.lab_6.R;

@RequiresApi(api = Build.VERSION_CODES.N)
public class SongAdapter extends ArrayAdapter<Song> {
    private LayoutInflater inflater;
    private int layout;
    private int currentSelected = -1;
    private final List<Song> songs;

    public SongAdapter(@NonNull Context context, int resource, List<Song> songs) {
        super(context, resource, songs);
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
        this.songs = songs;
    }

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

        Song song = songs.get(position);
        viewHolder.nameTextView.setText(song.Name);
        viewHolder.artistTextView.setText(song.Artist);
        viewHolder.durationTextView.setText(song.Duration);

        if (currentSelected != -1 && song.Id == songs.get(currentSelected).Id) {
            viewHolder.nameLayout.setBackgroundColor(Color.parseColor("#9370DB"));
            viewHolder.nameTextView.setTextColor(Color.WHITE);
            viewHolder.artistTextView.setTextColor(Color.WHITE);
            viewHolder.durationTextView.setTextColor(Color.WHITE);
        } else {
            viewHolder.nameLayout.setBackgroundColor(Color.parseColor("#e9e9e9"));
            viewHolder.nameTextView.setTextColor(Color.BLACK);
            viewHolder.artistTextView.setTextColor(Color.BLACK);
            viewHolder.durationTextView.setTextColor(Color.BLACK);
        }

        return convertView;
    }

    public void makeSelected(int position) {
        currentSelected = position;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        final TextView nameTextView, artistTextView, durationTextView;
        final LinearLayout nameLayout;

        ViewHolder(View view){
            nameTextView = view.findViewById(R.id.song_name);
            artistTextView = view.findViewById(R.id.song_artist);
            durationTextView = view.findViewById(R.id.song_duration);
            nameLayout = view.findViewById(R.id.song_item_layout);
        }
    }
}
