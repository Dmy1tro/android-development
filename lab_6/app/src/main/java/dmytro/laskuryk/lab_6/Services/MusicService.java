package dmytro.laskuryk.lab_6.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dmytro.laskuryk.lab_6.Models.Song;

public class MusicService extends Service {
    private static final int SONG_NOT_SELECTED = -1;
    private static final Random randomizer = new Random();
    private static int PROGRESS_DELAY = 300;
    private static boolean is_next_song_random = false;
    private final IBinder binder = new MusicServiceBinder();
    private List<Song> songList = new ArrayList<>();
    private IMusicServiceListener listener;
    private MediaPlayer mediaPlayer;
    private int currentSongPosition = SONG_NOT_SELECTED;
    private Handler handler = new Handler(Looper.myLooper());
    private Runnable playingSongNitifyer;

    public class MusicServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceivers();

        playingSongNitifyer = () -> {
            listener.onPlayingProgressChange(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
            handler.postDelayed(playingSongNitifyer, PROGRESS_DELAY);
        };

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                removeSongProgressNotifyer();
                listener.onSongStopPlaying(currentSongPosition);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeSongProgressNotifyer();
        mediaPlayer.stop();
    }

    private void registerReceivers() {
        BroadcastReceiver playReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isSongSelected()) { return; }

                if (isPlaying()) {
                    pause();
                } else {
                    resume();
                }
            }
        };

        BroadcastReceiver nextReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                next();
            }
        };
        BroadcastReceiver previousReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                previous();
            }
        };

        registerReceiver(playReceiver, new IntentFilter("song.play"));
        registerReceiver(nextReceiver, new IntentFilter("song.next"));
        registerReceiver(previousReceiver, new IntentFilter("song.previous"));
    }

    public void setSongs(List<Song> songs) {
        this.songList = songs;
    }

    public void setListener(IMusicServiceListener listener) {
        this.listener = listener;
    }

    public void setProgressDelay(int delay) {
        PROGRESS_DELAY = delay;
    }

    public void setRandomSongs(boolean isRandomSongs) {
        is_next_song_random = isRandomSongs;
    }

    public void play(int position) {
        if (songList.isEmpty() || position >= songList.size() || position < 0) {
            return;
        }

        currentSongPosition = position;
        Uri currentSongUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songList.get(position).Id);

        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), currentSongUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            addSongProgressNotifyer();

            listener.onSongStartPlaying(currentSongPosition, mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void next() {
        if (songList.isEmpty()) {
            return;
        }

        if (is_next_song_random) {
            currentSongPosition = randomizer.nextInt(songList.size());
        } else {
            if (currentSongPosition == songList.size() - 1) {
                currentSongPosition = 0;
            } else {
                currentSongPosition++;
            }
        }

        play(currentSongPosition);
    }

    public void previous() {
        if (songList.isEmpty()) {
            return;
        }

        if (is_next_song_random) {
            currentSongPosition = randomizer.nextInt(songList.size());
        } else {
            if (currentSongPosition == 0) {
                currentSongPosition = songList.size() - 1;
            } else {
                currentSongPosition--;
            }
        }

        play(currentSongPosition);
    }

    public void resume() {
        if (currentSongPosition == SONG_NOT_SELECTED) {
            return;
        }

        mediaPlayer.start();
        addSongProgressNotifyer();
        listener.onResume();
    }

    public void pause() {
        mediaPlayer.pause();
        removeSongProgressNotifyer();
        listener.onPause();
    }

    public void setProgress(int position) {
        mediaPlayer.seekTo(position);
    }

    public boolean isPlaying() {return mediaPlayer.isPlaying();}
    public boolean isSongSelected() {return currentSongPosition != SONG_NOT_SELECTED;}
    public boolean isRandomSongsEnabled() {return is_next_song_random;}
    public int getSelectedSongDuration() { return mediaPlayer.getDuration(); }
    public int getSelectedSongProgress() {return mediaPlayer.getCurrentPosition();}
    public Song getCurrentSong() {
        return currentSongPosition != SONG_NOT_SELECTED
                ? songList.get(currentSongPosition)
                : null;
    }
    public int getCurrentPosition() {return currentSongPosition;}

    private void addSongProgressNotifyer() {
        if (!handler.hasCallbacks(playingSongNitifyer)) {
            handler.postDelayed(playingSongNitifyer, PROGRESS_DELAY);
        }
    }

    private void removeSongProgressNotifyer() {
        handler.removeCallbacks(playingSongNitifyer);
    }
}

