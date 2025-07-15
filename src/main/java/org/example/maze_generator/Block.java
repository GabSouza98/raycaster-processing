package org.example.maze_generator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.maze_generator.MazeGenerator.*;

public class Block {

    //in pixels coordinates
    int x;
    int y;

    //index coordinates
    int thisRow;
    int thisCol;

    boolean visitedByMaze = false;

    //Walls
    boolean upWall = true;
    boolean downWall = true;
    boolean leftWall = true;
    boolean rightWall = true;

    //Neighbours
    List<Block> neighbours = new ArrayList<>();

    public Block(int row, int col) {
        this.x = col * size;
        this.y = row * size;
        this.thisRow = row;
        this.thisCol = col;
    }

    void show() {
        if (upWall) {
            processing.line(x, y, x+size, y);
        }
        if (downWall) {
            processing.line(x, y+size, x+size, y+size);
        }
        if (leftWall) {
            processing.line(x, y, x, y+size);
        }
        if (rightWall) {
            processing.line(x+size, y, x+size, y+size);
        }
    }

    void addNeighbours() {
        if (thisRow > 0) {
            neighbours.add(blocks[thisRow - 1][thisCol]);
        }
        if (thisCol > 0) {
            neighbours.add(blocks[thisRow][thisCol - 1]);
        }
        if (thisRow < rows - 1) {
            neighbours.add(blocks[thisRow + 1][thisCol]);
        }
        if (thisCol < cols - 1) {
            neighbours.add(blocks[thisRow][thisCol + 1]);
        }
    }

    boolean hasUnvisitedNeighbours() {
        return neighbours.stream().anyMatch(b -> !b.visitedByMaze);
    }

    Block pickRandomNeighbour() {
        List<Block> unvisitedNeighbors = neighbours.stream()
                .filter(b -> !b.visitedByMaze)
                .collect(Collectors.toList());

        return unvisitedNeighbors.get(floor(processing.random(0, unvisitedNeighbors.size())));
    }

}


