package dmytro.laskuryk.lab_6.Services;

public interface IMusicServiceListener {
    void onSongStartPlaying(int position, int maxProgress);
    void onPlayingProgressChange(int currentProgress, int maxProgress);
    void onResume();
    void onPause();
    void onSongStopPlaying(int position);
}
