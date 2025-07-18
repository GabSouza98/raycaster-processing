package org.example.raycaster.levels;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {

    public List<Level> levelList = new ArrayList<>();

    public LevelManager(List<Level> levelList) {
        this.levelList = levelList;
    }

    public LevelManager() {
        generateRandomLevels();
    }

    public void generateRandomLevels() {
        for (int i = 0; i < 10; i++) {
            levelList.add(new Level());
        }
    }
}
