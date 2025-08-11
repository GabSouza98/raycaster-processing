package org.example.raycaster.songs;

public class SoundManager {

    private static final Song[] songsList = new Song[] {
            new Song("DoppelateSoakedInHoney.mp3"),
            new Song("HushHushJustifyMyLove.mp3"),
            new Song("Sabbath.mp3"),
            new Song("Sabbath.mp3"),
            new Song("Sabbath.mp3"),
    };

    public static Song getSong(Integer index) {
        return songsList[index-1];
    }

}
