package org.example.raycaster;

import org.example.raycaster.textures.CheckerboardTexture;
import org.example.raycaster.textures.Texture;
import processing.core.PApplet;
import processing.event.KeyEvent;

import java.util.Arrays;

public class MazeRaycasterTest extends PApplet {

    public static PApplet processing;

    public ButtonKeys buttonKeys = new ButtonKeys();

    public Texture floorTexture = new CheckerboardTexture();

    //Player coordinates
    float px;
    float py;
    float pdx;
    float pdy;
    float pa;
    float pSpeed = 1;
    float angleSpeed = 0.05f;
    float pWallDistance;

    int[] map = {
            1,1,1,1,1,1,1,1,1,
            1,0,1,0,0,0,0,0,1,
            1,0,1,0,0,0,0,0,1,
            1,0,1,0,0,0,0,0,1,
            1,0,0,0,0,0,0,0,1,
            1,0,0,0,0,0,0,0,1,
            1,0,0,0,1,1,0,0,1,
            1,0,0,0,1,1,0,0,1,
            1,1,1,1,1,1,1,1,1
    };

    int mapX;
    int mapY;
    int mapS;

    int topViewMapSize;
    int raycasterHeight;
    int raycasterWidth;
    float squareSize;

    public static void main(String[] args) {
        PApplet.main("org.example.raycaster.MazeRaycasterTest", args);
    }

    @Override
    public void settings() {
        size(1112, 512); //width is 512 for the MapView + 600 for the raycaster view
        processing = this;
        topViewMapSize = height;
        raycasterHeight = height;
        raycasterWidth = width - height; //1112 - 512 = 600

        //Generate bidimensional array maze
        int[][] mapFromMazeGenerator = new MazeGenerator().generateMaze();

        //Creates single dimension array to accommodate all the bidimensional array positions
        map = new int[mapFromMazeGenerator.length*mapFromMazeGenerator.length];

        //Assumes the original is square.
        mapX = mapFromMazeGenerator.length;
        mapY = mapFromMazeGenerator.length;

        //This is basically the same as map.length
        mapS = mapX * mapY;

        //Populates the one-dimensional map
        for (int i = 0; i < mapX; i++) {
            for (int j = 0; j < mapY; j++) {
                map[i*mapX + j] = mapFromMazeGenerator[i][j];
            }
        }

        System.out.println("Map one dimensional");
        System.out.println(Arrays.toString(map));

        //Represents the size (height and width) of each square in the minimap
        //It's the total space available divided by the number of rows/cols
        squareSize = (float) topViewMapSize / (float) mapY;
        pWallDistance = squareSize/3;
    }

    @Override
    public void setup() {
        background(0);
        //Starts on the map[1][1] position, first available square.
        px = squareSize * 1.5f;
        py = squareSize * 1.5f;
        // Zero is looking ->
        // pi/2 is looking down.
        // The circle is clockwise
        pa = HALF_PI;
        pdx = cos(pa) * pSpeed;
        pdy = sin(pa) * pSpeed;
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
            pa -= angleSpeed;
            if (pa < 0) {
                pa += TWO_PI;
            }

            pdx = cos(pa) * pSpeed;
            pdy = sin(pa) * pSpeed;
        }

        if (buttonKeys.d) {
            pa += angleSpeed;
            if (pa > TWO_PI) {
                pa -= TWO_PI;
            }

            pdx = cos(pa) * pSpeed;
            pdy = sin(pa) * pSpeed;
        }

        //Wall collision check
        float xo;
        if (pdx < 0) {
            xo = -pWallDistance;
        } else {
            xo = pWallDistance;
        }

        float yo;
        if (pdy < 0) {
            yo = -pWallDistance;
        } else {
            yo = pWallDistance;
        }

        int ipx = (int) (px / squareSize);
        int ipx_add_xo = (int) ((px + xo) / squareSize);
        int ipx_sub_xo = (int) ((px - xo) / squareSize);

        int ipy = (int) (py / squareSize);
        int ipy_add_yo = (int) ((py + yo) / squareSize);
        int ipy_sub_yo = (int) ((py - yo) / squareSize);

        if (buttonKeys.w) {
            if (map[ipy*mapX + ipx_add_xo] == 0) {
                px += pdx;
            }
            if (map[ipy_add_yo*mapX + ipx] == 0) {
                py += pdy;
            }
        }

        if (buttonKeys.s) {
            if (map[ipy*mapX + ipx_sub_xo] == 0) {
                px -= pdx;
            }
            if (map[ipy_sub_yo*mapX + ipx] == 0) {
                py -= pdy;
            }
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

                xo = (int) (x*squareSize);
                yo = (int) (y*squareSize);

                rect(xo, yo, squareSize, squareSize);
            }
        }
    }

    void drawRays2D() {
        int r, mx, my, mp, dof; //depth of field, mp = mapValue
        float rx = 0, ry = 0, ra, xo = 0, yo = 0; //xOffset, yOffset
        float disT = 0; //final distance
        float shadingFactor;

        int fov = 60;
        int numberOfRays = 2*fov;
        float angleIncrement = 0.5f;
        int rayWidth = raycasterWidth / numberOfRays;

        ra = pa - ((float) fov /2) * DEG_TO_RAD;

        if (ra < 0) {
            ra += TWO_PI;
        }

        if (ra > TWO_PI) {
            ra -= TWO_PI;
        }

        for (r = 0; r < numberOfRays; r++) {
            //Check horizontal lines
            dof = 0;
            float disH = 10000000;
            float hx = px;
            float hy = py;
            float aTan = -1/tan(ra);

            if (ra > PI) {
                //Looking up
                ry = (float) (Math.floor(py / squareSize) * squareSize) - 0.0001f;
                rx = (py - ry) * aTan + px;
                yo = -squareSize;
                xo = -yo * aTan;
            }

            if (ra < PI) {
                //Looking down
//                ry = (float) (Math.floor(py / squareSize) * squareSize) + squareSize;
                ry = (float) (Math.ceil(py / squareSize) * squareSize);
                rx = (py - ry) * aTan + px;
                yo = squareSize;
                xo = -yo * aTan;
            }

            if (ra == 0 || ra == PI) {
                rx = px;
                ry = py;
                dof = mapX;
            }

            while (dof < mapY) {
                mx = (int) Math.floor(rx / squareSize);
                my = (int) Math.floor(ry / squareSize);
                mp = my * mapX + mx;

                if (mp > 0 && mp < mapX * mapY && map[mp] == 1) {
                    //Wall found
                    dof = mapX;
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
                rx = (float) (Math.floor(px / squareSize) * squareSize) - 0.0001f;
                ry = (px - rx) * nTan + py;
                xo = -squareSize;
                yo = -xo * nTan;
            }

            if (ra < HALF_PI || ra > 3*HALF_PI) {
                //Looking right
//                rx = (float) (Math.floor(px / squareSize) * squareSize) + squareSize;
                rx = (float) (Math.ceil(px / squareSize) * squareSize);
                ry = (px - rx) * nTan + py;
                xo = squareSize;
                yo = -xo * nTan;
            }

            if (ra == HALF_PI || ra == 3*HALF_PI) {
                rx = px;
                ry = py;
                dof = mapX;
            }

            while (dof < mapX) {
                mx = (int) Math.floor(rx / squareSize);
                my = (int) Math.floor(ry / squareSize);
                mp = my * mapX + mx;

                if (mp > 0 && mp < mapX * mapY && map[mp] == 1) {
                    //Wall found
                    dof = mapX;
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
                shadingFactor = disT*0.8f;
                stroke(255 - shadingFactor, 0, 0);
            }

            if (disH < disV) {
                rx = hx;
                ry = hy;
                disT = disH;
                shadingFactor = disT*0.8f;
                stroke(180 - shadingFactor, 0, 0);
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

            int height = 512; // altura da janela pequena

            int lineH = (int) (0.6f * (squareSize*height)/disT);

            if (lineH > height) {
                lineH = height;
            }

            int lineO = (height - lineH) /2; //Line offset

            float strokeWeight = rayWidth;
            strokeWeight(strokeWeight);
            strokeCap(PROJECT);

            //In processing, the line is drawn at the exact position specified,
            //and the strokeWeight occupies space beggining from the center.
            //If the line has X = 50, and strokeWeight = 10,
            //the resulting line will start at X=45 and end at X=55
            float strokeOffset = strokeWeight/2;

//            float ty = 0;
//            //The ty_step is the texture size divided by the lineH
//            //This step will be incremented in ty each loop,
//            //The final ty value will be at most 31.
//            float ty_step = 32.0f/lineH;

            //Wall as sequence of points starting from lineO to lineH+lineO
//            for (int y = 0; y < lineH; y++) {
//                int c = floorTexture.textureMap[((int) ty) * 32];
//                stroke(c * 255);
//                point(r*rayWidth + 512 + strokeOffset, y+lineO);
//                ty += ty_step;
//            }

            //Wall in one single line
            line(r*rayWidth + 512 + strokeOffset, lineO, r*rayWidth + 512 + strokeOffset, lineH+lineO);

            //Ceiling
            stroke(80);
            line(r*rayWidth + 512 + strokeOffset, 0, r*rayWidth + 512 + strokeOffset, lineO);

            //Floor
            stroke(40);
            line(r*rayWidth + 512 + strokeOffset, lineH+lineO, r*rayWidth + 512 + strokeOffset, height);

            ra += DEG_TO_RAD * angleIncrement;

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
