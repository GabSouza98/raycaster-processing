package org.example.raycaster;

import processing.core.PApplet;
import processing.event.KeyEvent;

public class MazeRaycasterOriginal extends PApplet {

    public static PApplet processing;

    public ButtonKeys buttonKeys = new ButtonKeys();

    //Player coordinates
    float px;
    float py;
    float pdx;
    float pdy;
    float pa;

    int mapX = 8;
    int mapY = 8;
    int mapS = 64;

    int[] map = {
            1,1,1,1,1,1,1,1,
            1,0,1,0,0,0,0,1,
            1,0,1,0,0,0,0,1,
            1,0,1,0,0,0,0,1,
            1,0,0,0,0,0,0,1,
            1,0,0,0,0,1,0,1,
            1,0,0,0,0,0,0,1,
            1,1,1,1,1,1,1,1
    };

    public static void main(String[] args) {
        PApplet.main("org.example.raycaster.MazeRaycasterOriginal", args);
    }

    @Override
    public void settings() {
        size(1024, 512);
        processing = this;
    }

    @Override
    public void setup() {
        background(0);
        px=300;
        py=300;
        pa=0;
        pdx = cos(pa) * 5;
        pdy = sin(pa) * 5;
    }

    @Override
    public void draw() {
        background(0);
        updateKeys();
        drawMap2D();
        drawPlayer();
        drawRays2D();
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        char key = keyEvent.getKey();
        if (key == 'a') {
            buttonKeys.a = true;
        }
        if (key == 'd') {
           buttonKeys.d = true;
        }
        if (key == 'w') {
            buttonKeys.w = true;
        }
        if (key == 's') {
            buttonKeys.s = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        char key = keyEvent.getKey();
        if (key == 'a') {
            buttonKeys.a = false;
        }
        if (key == 'd') {
            buttonKeys.d = false;
        }
        if (key == 'w') {
            buttonKeys.w = false;
        }
        if (key == 's') {
            buttonKeys.s = false;
        }
    }

    void updateKeys() {
        if (buttonKeys.a) {
            pa -= 0.1f;
            if (pa < 0) {
                pa += TWO_PI;
            }

            pdx = cos(pa) * 5;
            pdy = sin(pa) * 5;
        }

        if (buttonKeys.d) {
            pa += 0.1f;
            if (pa > TWO_PI) {
                pa -= TWO_PI;
            }

            pdx = cos(pa) * 5;
            pdy = sin(pa) * 5;
        }

        if (buttonKeys.w) {
            px += pdx;
            py += pdy;
        }

        if (buttonKeys.s) {
            px -= pdx;
            py -= pdy;
        }
    }

    void drawPlayer() {
        stroke(255, 0, 0);
        strokeCap(ROUND); //makes the point to appear round
        strokeWeight(8);
        point(px, py);

        strokeWeight(3);
        line(px, py, px+pdx*5, py+pdy*5);
    }

    void drawMap2D() {
        strokeWeight(2);
        stroke(100);

        int x, y, xo, yo;
        for (y = 0; y < mapY; y++) {
            for (x = 0; x < mapX; x++) {

                if (map[y*mapX + x] == 1) {
                    //black
                    fill(255);
                } else {
                    //white
                    fill(0);
                }

                xo = x*mapS;
                yo = y*mapS;

                rect(xo, yo, mapS, mapS);
            }
        }
    }

    void drawRays2D() {
        int r, mx, my, mp, dof; //depth of field, mp = mapValue
        float rx = 0, ry = 0, ra, xo = 0, yo = 0; //xOffset, yOffset
        float disT = 0; //final distance

        ra = pa - 30 * DEG_TO_RAD;

        if (ra < 0) {
            ra += TWO_PI;
        }

        if (ra > TWO_PI) {
            ra -= TWO_PI;
        }

        for (r = 0; r < 60; r++) {
            //Check horizontal lines
            dof = 0;
            float disH = 10000000;
            float hx = px;
            float hy = py;
            float aTan = -1/tan(ra);

            if (ra > PI) {
                //Looking down
                ry = ((((int) py >> 6) << 6) - 0.0001f);
                rx = (py - ry) * aTan + px;
                yo = -64;
                xo = -yo * aTan;
            }

            if (ra < PI) {
                //Looking down
                ry = ((((int) py >> 6) << 6) + 64f);
                rx = (py - ry) * aTan + px;
                yo = 64;
                xo = -yo * aTan;
            }

            if (ra == 0 || ra == PI) {
                rx = px;
                ry = py;
                dof = 8;
            }

            while (dof < 8) {
                //mx = (int) (rx) >> 6; //equivale a dividir por 64, que seria 2^6
                //my = (int) (ry) >> 6;
                mx = (int) (rx) / 64;
                my = (int) (ry) / 64;
                mp = my * mapX + mx;

                if (mp > 0 && mp < mapX * mapY && map[mp] == 1) {
                    //Wall found
                    dof = 8;
                    hx = rx;
                    hy = ry;
                    disH = dist(px, py, hx, hy, ra);
                } else {
                    rx += xo;
                    ry += yo;
                    dof += 1;
                }
            }

            //Check vertical lines
            dof = 0;
            float disV = 10000000;
            float vx = px;
            float vy = py;
            float nTan = -tan(ra);

            if (ra > HALF_PI && ra < 3*HALF_PI) {
                //Looking left
                rx = ((((int) px >> 6) << 6) - 0.0001f);
                ry = (px - rx) * nTan + py;
                xo = -64;
                yo = -xo * nTan;
            }

            if (ra < HALF_PI || ra > 3*HALF_PI) {
                //Looking right
                rx = ((((int) px >> 6) << 6) + 64f);
                ry = (px - rx) * nTan + py;
                xo = 64;
                yo = -xo * nTan;
            }

            if (ra == HALF_PI || ra == 3*HALF_PI) {
                rx = px;
                ry = py;
                dof = 8;
            }

            while (dof < 8) {
                mx = (int) (rx) >> 6;
                my = (int) (ry) >> 6;
                mp = my * mapX + mx;

                if (mp > 0 && mp < mapX * mapY && map[mp] == 1) {
                    //Wall found
                    dof = 8;
                    vx = rx;
                    vy = ry;
                    disV = dist(px, py, vx, vy, ra);
                } else {
                    rx += xo;
                    ry += yo;
                    dof += 1;
                }
            }

            if (disV < disH) {
                rx = vx;
                ry = vy;
                disT = disV;
                stroke(255, 0, 0);
            }

            if (disH < disV) {
                rx = hx;
                ry = hy;
                disT = disH;
                stroke(200, 0, 0);
            }

            //Draw Ray
            strokeWeight(2);
            line(px, py, rx, ry);

            //Draw 3D Walls
            float ca = pa - ra;
            if (ca < 0) {
                ca += TWO_PI;
            }
            if (ca > TWO_PI) {
                ca -= TWO_PI;
            }

            disT = disT * cos(ca); //Fix fish eye

            int height = 320;

            float lineH = (mapS*height)/disT;
            if (lineH > height) {
                lineH = height;
            }

            float lineO = (height - lineH)/2; //Line offset

            strokeWeight(9f);
            strokeCap(PROJECT);
            line(r*8 + 530, lineO, r*8 + 530, lineH+lineO);

            ra += DEG_TO_RAD;

            if (ra < 0) {
                ra += TWO_PI;
            }
            if (ra > TWO_PI) {
                ra -= TWO_PI;
            }
        }
    }

    float dist(float ax, float ay, float bx, float by, float ang) {
        return sqrt((bx-ax)*(bx-ax) + (by-ay)*(by-ay));
    }
}
