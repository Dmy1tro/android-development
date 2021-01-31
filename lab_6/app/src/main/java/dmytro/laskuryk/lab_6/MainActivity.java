package dmytro.laskuryk.lab_6;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import dmytro.laskuryk.lab_6.Adapters.SongAdapter;
import dmytro.laskuryk.lab_6.Models.Song;
import dmytro.laskuryk.lab_6.Services.IMusicServiceListener;
import dmytro.laskuryk.lab_6.Services.MusicService;
import dmytro.laskuryk.lab_6.Services.PhoneStateCustomListener;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    private static final int NOTIFY_ID = 458;
    private static final String CHANNEL_ID = "CHANNEL_ID_12345";
    private final List<Song> songList = new ArrayList<>();
    private ListView songListView;
    private SongAdapter songAdapter;
    private TextView songNameTextView;
    private SeekBar songSeekBar;
    private Button playButton, previousButton, nextButton;
    private CheckBox randomSongCheck;
    private MusicService musicService;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) service;
            musicService = binder.getService();
            musicService.setSongs(songList);
            setDefaultListener(musicService);
            restoreState();
            hideNotification();
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songNameTextView = findViewById(R.id.song_name);
        songSeekBar = findViewById(R.id.seek_bar);
        songListView = findViewById(R.id.songsListView);
        playButton = findViewById(R.id.playButton);
        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        randomSongCheck = findViewById(R.id.song_random_check);

        setupView();
        requestPermissions();

        PhoneStateCustomListener phoneListener = new PhoneStateCustomListener();
        phoneListener.setOnCallStarted(() -> {
            if (musicService != null && musicService.isPlaying()) {
                musicService.pause();
                phoneListener.isMusicPausedByCall = true;
            }
        });
        phoneListener.setOnCallEnded(() -> {
            if (musicService != null && phoneListener.isMusicPausedByCall) {
                musicService.resume();
                phoneListener.isMusicPausedByCall = false;
            }
        });
        TelephonyManager telService = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telService.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();

        setNotificationListener(musicService);
        showNotification();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (musicService != null) {
            setDefaultListener(musicService);
            restoreState();
        }

        hideNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideNotification();
    }

    public void restoreState() {
        int position = musicService.getCurrentPosition();
        playButton.setText("play");
        randomSongCheck.setChecked(musicService.isRandomSongsEnabled());

        if (musicService.isSongSelected()) {
            Song song = songList.get(position);
            songNameTextView.setText(String.format("%s - %s", song.Name, song.Artist));
            songAdapter.makeSelected(position);
            songListView.smoothScrollToPosition(position);
            songSeekBar.setMax(musicService.getSelectedSongDuration());
            songSeekBar.setProgress(musicService.getSelectedSongProgress());

            if (musicService.isPlaying()) {
                playButton.setText("pause");
            }
        }
    }

    private void setupView() {
        songAdapter = new SongAdapter(this, R.layout.song_item, songList);
        songListView.setAdapter(songAdapter);

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (musicService.getCurrentPosition() == position) {
                    playButton.callOnClick();
                } else {
                    musicService.play(position);
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!musicService.isSongSelected()) {
                    return;
                }

                if (musicService.isPlaying()) {
                    musicService.pause();
                } else {
                    musicService.resume();
                }
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.previous();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.next();
            }
        });

        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicService.setProgress(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        randomSongCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    musicService.setRandomSongs(isChecked);
                }
            }
        });

        songNameTextView.setOnClickListener(v -> {
            if (musicService.isSongSelected()) {
                songListView.smoothScrollToPosition(musicService.getCurrentPosition());
            }
        });
    }

    private void requestPermissions() {
        String[] permissions = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 0);
        } else {
            setupSongsFromStorage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupSongsFromStorage();
        }
    }

    private void setupSongsFromStorage() {
        ContentResolver contentResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(musicUri, null, null, null, null);

        List<Song> songs = new ArrayList<>();

        if (cursor != null) {
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int authorColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(nameColumn);
                String author = cursor.getString(authorColumn);
                Integer duration = cursor.getInt(durationColumn);
                Song song = Song.create(id, title, author, duration);
                songs.add(song);
            }

            cursor.close();
        }

        this.songList.clear();
        this.songList.addAll(songs);
        this.songAdapter.notifyDataSetChanged();
    }

    private void showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        getNotificationManager().notify(NOTIFY_ID, getNotification(musicService));
    }

    private void hideNotification() {
        getNotificationManager().cancel(NOTIFY_ID);
    }

    private void setDefaultListener(MusicService service) {
        service.setProgressDelay(300);
        service.setListener(new IMusicServiceListener() {
            @Override
            public void onSongStartPlaying(int position, int maxProgress) {
                Song song = songList.get(position);
                songNameTextView.setText(String.format("%s - %s", song.Name, song.Artist));
                playButton.setText("pause");
                songSeekBar.setMax(maxProgress);
                songAdapter.makeSelected(position);
                songListView.smoothScrollToPosition(position);
            }
            @Override
            public void onPlayingProgressChange(int currentProgress, int maxProgress) {
                songSeekBar.setProgress(currentProgress);
            }

            @Override
            public void onResume() {
                playButton.setText("pause");
            }

            @Override
            public void onPause() {
                playButton.setText("play");
            }

            @Override
            public void onSongStopPlaying(int position) {
                service.next();
            }
        });
    }

    private void setNotificationListener(MusicService service) {
        service.setProgressDelay(1000);
        service.setListener(new IMusicServiceListener() {
            @Override
            public void onSongStartPlaying(int position, int maxProgress) {
                getNotificationManager().notify(NOTIFY_ID, getNotification(service));
            }

            @Override
            public void onPlayingProgressChange(int currentProgress, int maxProgress) {
                getNotificationManager().notify(NOTIFY_ID, getNotification(service));
            }
            @Override
            public void onResume() {
                getNotificationManager().notify(NOTIFY_ID, getNotification(service));
            }
            @Override
            public void onPause() {
                getNotificationManager().notify(NOTIFY_ID, getNotification(service));
            }
            @Override
            public void onSongStopPlaying(int position) {
                service.next();
            }
        });
    }

    private Notification getNotification(MusicService service) {
        Intent notificationIntent = new Intent(MainActivity.this, MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openAppIntent = PendingIntent.getActivity(
                MainActivity.this, 0, notificationIntent, 0 );

        PendingIntent pendingPlayIntent = PendingIntent.getBroadcast(this, 1, new Intent("song.play"), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 2, new Intent("song.next"), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingPreviousIntent = PendingIntent.getBroadcast(this, 3, new Intent("song.previous"), PendingIntent.FLAG_UPDATE_CURRENT);

        String songName = "Choose song";
        String songArtist = "";
        String playBtn = "Play";
        int currentProgress = 0;
        int maxProgress = 0;
        if (service != null && service.isSongSelected()) {
            if (service.isPlaying()) {
                playBtn = "Pause";
            }
            Song song = musicService.getCurrentSong();
            songName = song.Name;
            songArtist = song.Artist;
            currentProgress = service.getSelectedSongProgress();
            maxProgress = service.getSelectedSongDuration();
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(songName)
                .setContentText(songArtist)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .setProgress(0, 0, false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(R.drawable.ic_launcher_foreground, "Previous", pendingPreviousIntent)
                .addAction(R.drawable.ic_launcher_background, playBtn, pendingPlayIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Next", pendingNextIntent)
                .setProgress(maxProgress, currentProgress, false)
                .setContentIntent(openAppIntent)
                .build();
    }

    private NotificationManagerCompat getNotificationManager() {
        return NotificationManagerCompat.from(getApplicationContext());
    }
}