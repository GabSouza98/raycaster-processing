package org.example.maze_generator_animation;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

public class MazeGeneratorAnimation extends PApplet {

    public static PApplet processing;

    public static final int size = 40;
    public static Block[][] blocks;

    public static int rows;
    public static int cols;

    Block current;
    Block next;

    boolean isMazeFinished = false;
    List<Block> stack = new ArrayList<>();

    public static void main(String[] args) {
        PApplet.main("org.example.maze_generator_animation.MazeGeneratorAnimation", args);
    }

    @Override
    public void settings() {
        size(480, 480);
        processing = this;
    }

    @Override
    public void setup() {
        rows = height / size;
        cols = width / size;
        blocks = new Block[rows][cols];

        //Create the blocks for the maze
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                blocks[i][j] = new Block(i, j);
            }
        }

        //Adds the neighbours to each block
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                blocks[i][j].addNeighbours();
            }
        }

        current = blocks[0][0];
        current.visitedByMaze = true;
        stack.add(current);

        frameRate(30);
    }

    @Override
    public void draw() {
        if (!isMazeFinished) {
            background(0, 255, 255); //blue
            strokeWeight(3);

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    blocks[i][j].show();
                }
            }

            fill(193, 50, 193);
            rect(current.x, current.y, size, size);
            current.visitedByMaze = true;

            if (current.hasUnvisitedNeighbours()) {
                next = current.pickRandomNeighbour();
                stack.add(next);
                removeWalls(current, next);
                current = next;
            } else if (!stack.isEmpty()) {
                next = stack.get(stack.size() - 1);
                stack.remove(next);
                removeWalls(current, next);
                current = next;
            } else {
                System.out.println("Maze done");
                isMazeFinished = true;
                noLoop();
            }
        }
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