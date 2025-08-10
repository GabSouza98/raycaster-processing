package org.example.raycaster.levels;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {

    public List<Level> levelList = new ArrayList<>();

    public LevelManager(List<Level> levelList) {
        this.levelList = levelList;
    }

    public LevelManager(boolean lights) {
        generateRandomLevels(lights);
    }

    public void generateRandomLevels(boolean lights) {
        for (int i = 0; i < 2; i++) {
            levelList.add(new Level(lights, i));
        }
    }
}
