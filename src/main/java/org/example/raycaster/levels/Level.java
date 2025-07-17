package org.example.raycaster.levels;

import org.example.raycaster.Light;
import org.example.raycaster.texture.Texture;
import org.example.raycaster.texture.TextureGenerator;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;

public class Level {

    public int[] map;

    public Texture floorTexture;
    public Texture ceilingTexture;
    public Texture wallTexture;

    public List<Light> lights;

    private final int mapX;
    private final int mapY;

    /**
     * Must pass the parameters mapX and mapY, because since int[] map is one-dimensional, it's impossible to know
     * how many columns each row have.
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

    public int[] getOneDimensionalMap(int[][] mapFromMazeGenerator) {
        //Creates single dimension array to accommodate all the bidimensional array positions
        int[] map = new int[mapX * mapY];

        //Populates the one-dimensional map
        for (int i = 0; i < mapY; i++) {
            for (int j = 0; j < mapX; j++) {
                map[i*mapY + j] = mapFromMazeGenerator[i][j];
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
