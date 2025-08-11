package org.example.raycaster;

import java.util.ArrayList;
import java.util.List;

public class MazeGenerator {

    public final int height;
    public final int width;
    public final int size;
    public final int rows;
    public final int cols;
    public final Block[][] blocks;

    Block current;
    Block next;

    boolean isMazeFinished = false;
    List<Block> stack = new ArrayList<>();

    public MazeGenerator() {
        this.height = 400;
        this.width = 400;
        this.size = 100;

        this.rows = height / size;
        this.cols = width / size;
        this.blocks = new Block[rows][cols];
    }

    public MazeGenerator(int height, int width, int size) {
        this.height = height;
        this.width = width;
        this.size = size;

        this.rows = height / size;
        this.cols = width / size;
        this.blocks = new Block[rows][cols];
    }

    public int[][] generateMaze() {
        //Create the blocks for the maze
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                blocks[i][j] = new Block(i, j, rows, cols, size);
            }
        }

        //Adds the neighbours to each block
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                blocks[i][j].addNeighbours(blocks);
            }
        }

        current = blocks[0][0];

        while (!isMazeFinished) {
            current.visitedByMaze = true;

            if (current.hasUnvisitedNeighbours()) {
                next = current.pickRandomNeighbour();
                stack.add(current);
                removeWalls(current, next);
                current = next;
            } else if (!stack.isEmpty()) {
                next = stack.get(stack.size() - 1);
                stack.remove(next);
                current = next;
            } else {
                isMazeFinished = true;
            }
        }

        RayCasterMapConverter mapConverter = new RayCasterMapConverter(blocks);
        return mapConverter.createRayCasterMap();
    }

    void removeWalls(Block current, Block next) {
        int xDistance = next.thisCol - current.thisCol;
        int yDistance = next.thisRow - current.thisRow;

        //Andou para a direita
        if (xDistance > 0) {
            current.rightWall = false;
            next.leftWall = false;
        }
        //Andou para a esquerda
        if (xDistance < 0) {
            current.leftWall = false;
            next.rightWall = false;
        }
        //Andou para baixo
        if (yDistance > 0) {
            current.downWall = false;
            next.upWall = false;
        }
        //Andou para cima
        if (yDistance < 0) {
            current.upWall = false;
            next.downWall = false;
        }
    }
}