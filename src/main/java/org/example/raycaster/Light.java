package org.example.raycaster;

public class Light {
    float x, y;
    float intensity; // 1.0 = full brightness
    float radius;

    Light(float x, float y, float intensity, float radius) {
        this.x = x;
        this.y = y;
        this.intensity = intensity;
        this.radius = radius;
    }
}
