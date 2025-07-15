package org.example.raycaster;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.raycaster.MazeGenerator.*;
import static org.example.raycaster.MazeRaycasterTest.processing;

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

        return unvisitedNeighbors.get(processing.floor(processing.random(0, unvisitedNeighbors.size())));
    }

}


