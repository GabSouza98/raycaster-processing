package org.example.raycaster.levels;

import org.example.maze_generator.Block;
import org.example.raycaster.Light;
import org.example.raycaster.MazeGenerator;
import org.example.raycaster.texture.Texture;
import org.example.raycaster.texture.TextureGenerator;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

public class Level {

    public int[] map;

    public Texture floorTexture;
    public Texture ceilingTexture;
    public Texture wallTexture;

    public List<Light> lights;

    public final int mapX;
    public final int mapY;

    /**
     * Must pass the parameters mapX and mapY, because since int[] map is one-dimensional, it's impossible to know
     * how many columns each row has.
     *
     * @param map
     * @param mapX
     * @param mapY
     * @param floorTexture
     * @param ceilingTexture
     * @param wallTexture
     * @param lights
     */
    public Level(int[] map, int mapX, int mapY, Texture floorTexture, Texture ceilingTexture, Texture wallTexture, List<Light> lights) {
        this.mapX = mapX;
        this.mapY = mapY;
        this.map = map;
        this.floorTexture = checkNull(floorTexture);
        this.ceilingTexture = checkNull(ceilingTexture);
        this.wallTexture = checkNull(wallTexture);
        this.lights = lights;
    }

    /**
     * From the bi-dimensional map it's easy to determine the number of rows and columns.
     *
     * @param bidimensionalMap
     * @param floorTexture
     * @param ceilingTexture
     * @param wallTexture
     * @param lights
     */
    public Level(int[][] bidimensionalMap, Texture floorTexture, Texture ceilingTexture, Texture wallTexture, List<Light> lights) {
        this.mapY = bidimensionalMap.length; //Lines
        this.mapX = bidimensionalMap[0].length; //Columns
        this.map = getOneDimensionalMap(bidimensionalMap);

        this.floorTexture = checkNull(floorTexture);
        this.ceilingTexture = checkNull(ceilingTexture);
        this.wallTexture = checkNull(wallTexture);

        this.lights = lights;
    }

    public Level() {
        int[][] bidimensionalMap = new MazeGenerator().generateMaze();

        this.mapY = bidimensionalMap.length;
        this.mapX = bidimensionalMap[0].length;
        this.map = getOneDimensionalMap(bidimensionalMap);

        this.floorTexture = TextureGenerator.getRandomTexture();
        this.ceilingTexture = TextureGenerator.getRandomTexture();
        this.wallTexture = TextureGenerator.getRandomTexture();

//        this.lights = getLights(bidimensionalMap);
        this.lights = new ArrayList<>();
    }

    public List<Light> getLights(int[][] bidimensionalMap) {

        List<Light> lights = new ArrayList<>();


        for (int i = 0; i < mapY; i++) {
            for (int j = 0; j < mapX; j++) {
                int current = bidimensionalMap[i][j];

                if (current > 0) {
                    //It's a wall block
                    continue;
                }

                if (current == 0) {
                    //Check if it has 3 intersection at least.
                    //
                    //    1 0 1   1 0 1   1 0 1   1 1 1
                    //    1 0 0   0 0 1   0 0 0   0 0 0
                    //    1 0 1   1 0 1   1 1 1   1 0 1
                    //
                    int bot   = existsPos(i+1, j) ? bidimensionalMap[i+1][j] : 1;
                    int up    = existsPos(i-1, j) ? bidimensionalMap[i-1][j] : 1;
                    int right = existsPos(i, j+1) ? bidimensionalMap[i][j+1] : 1;
                    int left  = existsPos(i, j-1) ? bidimensionalMap[i][j-1] : 1;

                    if (bot + up + right + left <= 1) {
                        //Intersection of 3 hallways
                        lights.add(new Light(i, j, 1.0f, 1.2f, 255, 255, 0));
                        continue;
                    }

                    //Checks for changes in direction
                    //
                    //    1 1 1   1 1 1   1 0 1   1 0 1
                    //    1 0 0   0 0 1   0 0 1   1 0 0
                    //    1 0 1   1 0 1   1 1 1   1 1 1
                    //
                    if ((bot == 0 && right == 0) || (bot == 0 && left == 0) || (up == 0 && right == 0) || (up == 0 && left == 0)) {
                        lights.add(new Light(i, j, 1.0f, 1.2f, 255, 255, 0));
                    }
                }
            }
        }

        // S = start
        // E = exit
        // XXXXXXX
        // XS    X
        // X     X
        // X    EX
        // XXXXXXX

        //Player spawn position
        lights.add(new Light(1, 1, 2.0f, 1.2f, 255, 0, 0));

        //Exit position
        lights.add(new Light(mapY - 2, mapX - 2, 1.0f, 1.2f, 0, 255, 0));

        return lights;
    }

    public boolean existsPos(int i, int j) {
        if (i < 0) {
            return false;
        }

        if (j < 0) {
            return false;
        }

        if (i >= mapY) {
            return false;
        }

        if (j >= mapX) {
            return false;
        }
        return true;
    }

    public int[] getOneDimensionalMap(int[][] mapFromMazeGenerator) {
        //Creates single dimension array to accommodate all the bidimensional array positions
        int[] map = new int[mapX * mapY];

        //Populates the one-dimensional map
        for (int i = 0; i < mapY; i++) {
            for (int j = 0; j < mapX; j++) {
                map[i * mapY + j] = mapFromMazeGenerator[i][j];
            }
        }

        return map;
    }

    public Texture checkNull(Texture texture) {
        if (nonNull(texture)) {
            return texture;
        } else {
            return TextureGenerator.getRandomTexture();
        }
    }
}
