package org.example.raycaster;

public class Light {
    float x, y;
    float intensity; // 1.0 = full brightness
    float radius;

    //Position in the grid/maze
    int i, j;

    //RGB values
    int r, g, b;

    //Receives directly the position in pixels, same scale as px, py
    public Light(float x, float y, float intensity, float radius) {
        this.x = x;
        this.y = y;
        this.intensity = intensity;
        this.radius = radius;
        this.r = 255;
        this.g = 255;
        this.b = 255;
    }

    //Receives the position in the grid where the light sits
    public Light(int i, int j, float intensity, float radius) {
        this.i = i;
        this.j = j;
        this.intensity = intensity;
        this.radius = radius;
        this.r = 255;
        this.g = 255;
        this.b = 255;
    }

    //Receives the position in the grid where the light sits
    public Light(int i, int j, float intensity, float radius, int r, int g, int b) {
        this.i = i;
        this.j = j;
        this.intensity = intensity;
        this.radius = radius;
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
