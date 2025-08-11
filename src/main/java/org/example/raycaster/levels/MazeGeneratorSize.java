package org.example.raycaster.levels;

import java.util.Arrays;

public enum MazeGeneratorSize {

    LEVEL_1(1, 400, 400, 100),
    LEVEL_2(2, 500, 500, 100),
    LEVEL_3(3, 600, 600, 100),
    LEVEL_4(4, 400, 400, 80),
    LEVEL_5(5, 400, 400, 40);

    public final int level;
    public final int height;
    public final int width;
    public final int size;

    MazeGeneratorSize(int level, int height, int width, int size) {
        this.level = level;
        this.height = height;
        this.width = width;
        this.size = size;
    }

    public static MazeGeneratorSize getSizes(int level) {
        return Arrays.stream(MazeGeneratorSize.values())
                .filter(l -> l.level == level)
                .findFirst()
                .orElse(null);
    }
}
