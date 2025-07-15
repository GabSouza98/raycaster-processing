package org.example.raycaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class RayCasterMapConverter {

    Block[][] blocks;
    int rows;
    int cols;

    int[][] map;

    Block current;

    public RayCasterMapConverter(Block[][] blocks) {
        this.blocks = blocks;
        //Assume que o mapa é quadrado
        int size = blocks.length;
        rows = blocks.length;
        cols = blocks.length;

        //Each map will contain the number of cells + walls
        //In a 4x4 map, each row/column will contain 3 walls
        //Therefore the resulting size must be 7x7
        int newSize = size*2 - 1;

        map = new int[newSize][newSize];
    }

    public int[][] createRayCasterMap() {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                current = blocks[i][j];

                Block bot = getBlockIfExists(i+1, j);
                Block right = getBlockIfExists(i, j+1);

                if (nonNull(bot) && current.downWall && bot.upWall) {
                    //tem parede entre esse bloco e o de baixo
                    map[bot.thisRow*2-1][j*2] = 1;

                    if (j*2 + 1 < map.length) {
                        //Marca parede na linha abaixo na coluna da direita
                        map[bot.thisRow*2-1][j*2 + 1] = 1;
                    }
                    if (j*2 - 1 > 0) {
                        //Marca parede na linha abaixo na coluna da esquerda
                        map[bot.thisRow*2-1][j*2 - 1] = 1;
                    }
                }

                if (nonNull(right) && current.rightWall && right.leftWall) {
                    //tem parede entre esse bloco e o da direita
                    map[i*2][right.thisCol*2-1] = 1;

                    if (i*2 + 1 < map.length) {
                        //Marca parede na linha debaixo também
                        map[i*2 + 1][right.thisCol*2-1] = 1;
                    }

                    if (i*2 - 1 > 0) {
                        //Marca parede na linha acima também
                        map[i*2 - 1][right.thisCol*2-1] = 1;
                    }
                }
            }
        }

        map = surroundWalls(map);

        for (int i = 0; i < map.length; i++) {
            System.out.println(Arrays.toString(map[i]));
        }

        return map;
    }

    int[][] surroundWalls(int[][] map) {
        //Adds 2 lines and 2 columns for the new walls
        int[][] fullMap = new int[map.length+2][map.length+2];

        //Generate array filled with 1's
        int[] topAndBottomWalls = new int[fullMap.length];
        Arrays.fill(topAndBottomWalls, 1);

        //Fills first and last rows with walls
        fullMap[0] = topAndBottomWalls;
        fullMap[fullMap.length-1] = topAndBottomWalls;

        //Skips first and last row
        for (int i = 1; i < fullMap.length-1; i++) {

            List<Integer> row = new ArrayList<>();

            List<Integer> oldRow = Arrays.stream(map[i-1]).boxed().collect(Collectors.toList());

            row.add(1);           //First wall
            row.addAll(oldRow);   //
            row.add(1);           //Last wall

            fullMap[i] = row.stream().mapToInt(Integer::intValue).toArray();;
        }

        return fullMap;
    }

    Block getBlockIfExists(int i, int j) {
        if (i < 0) {
            return null;
        }
        if (j < 0) {
            return null;
        }
        if (i >= rows) {
            return null;
        }
        if (j >= cols) {
            return null;
        }

        return blocks[i][j];
    }
}
