package org.example.raycaster.ui;

import processing.core.PConstants;
import processing.core.PFont;

import static org.example.raycaster.MazeRaycasterTest.processing;

public class Button {

    int x, y, w, h;
    int c;
    String label;
    boolean over = false;
    boolean pressed = false;

    public Button(int xpos, int ypos, int width, int height, int fillColor, String buttonLabel) {
        x = xpos;
        y = ypos;
        w = width;
        h = height;
        c = fillColor;
        label = buttonLabel;
    }

    public void display() {
        processing.stroke(0);
        processing.fill(c);
        if (over) {
            processing.fill(175);
        }
        processing.rect(x - 0.5f*w, y - 0.5f*h, w, h);
        processing.textAlign(PConstants.CENTER, PConstants.CENTER);
        processing.fill(0);
        PFont font = processing.createFont("Arial", 20.0f);
        processing.textFont(font);
        processing.text(label, x, y);
    }

    public boolean isPressed() {
        return processing.mouseX > x && processing.mouseX < x + w && processing.mouseY > y && processing.mouseY < y + h;
    }

}
