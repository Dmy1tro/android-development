package dmytro.laskuryk.lab_6.Models;

public class Song {
    public long Id;
    public String Name;
    public String Artist;
    public String Duration;

    public static Song create(long id, String name, String author, Integer duration) {
        Song song = new Song();
        song.Id = id;
        song.Name = name;
        song.Artist = author;

        String seconds = formatTime(Math.round(duration / 1000.0));
        String minutes = formatTime(Math.round(duration / (1000.0 * 60.0)));

        song.Duration = String.format("%s:%s", minutes, seconds);

        return song;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Song) {
            return this.Id == ((Song)obj).Id;
        }

        return false;
    }

    @Override
    public String toString() {
        return this.Name;
    }

    private static String formatTime(long time) {
        String strTime = Long.toString(time);

        while (strTime.length() < 2) {
            strTime = "0" + strTime;
        }

        if (strTime.length() > 2) {
            strTime = strTime.substring(0, 2);
        }

        return strTime;
    }
}
