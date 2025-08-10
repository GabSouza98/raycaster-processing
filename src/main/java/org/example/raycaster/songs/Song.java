package org.example.raycaster.songs;


import org.example.raycaster.MazeRaycasterTest;
import processing.sound.SoundFile;

public class Song {

    public SoundFile soundFile;

    public Song(String fileName) {
        this.soundFile = new SoundFile(MazeRaycasterTest.processing, fileName);
    }
}
