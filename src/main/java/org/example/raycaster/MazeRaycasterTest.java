package org.example.raycaster;

import org.example.raycaster.levels.Level;
import org.example.raycaster.levels.LevelManager;
import org.example.raycaster.texture.Texture;
import org.example.raycaster.texture.textures.BrickTexture;
import org.example.raycaster.texture.textures.CheckerboardTexture;
import org.example.raycaster.texture.textures.DoorTexture;
import org.example.raycaster.texture.textures.WindowTexture;
import processing.core.PApplet;
import processing.event.KeyEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MazeRaycasterTest extends PApplet {

    public static PApplet processing;

    private LevelManager levelManager;
    private Iterator<Level> levelIterator;

    public final ButtonKeys buttonKeys = new ButtonKeys();

    //Player coordinates
    float px;
    float py;
    float pdx;
    float pdy;
    float pa;
    float angleSpeed = 0.05f;

    //These come from the Level class
    int[] map;
    int mapX;
    int mapY;
    int mapS;
    Texture floorTexture;
    Texture ceilingTexture;
    Texture wallTexture;
//    List<Light> lights = new ArrayList<>();

    //These are computed after knowing the variables above
    //The squareSize is determined based on the mapX and mapY
    float squareSize;
    //All of these are functions of squareSize
    float maxDist;
    float pWallDistance;
    float textureRatio;
    float pSpeed; //The bigger the maze, the less the squareSize, the less the speed.
    float normalSpeed;
    float runningSpeed;

    //These are defined once in the program, they depend only on the size() args
    int topViewMapSize;
    int raycasterHeight;
    int raycasterWidth;

    float minBrightness = 0.00f;   // minimum visible brightness

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

        levelManager = new LevelManager();
        levelIterator = levelManager.levelList.iterator();
    }

    @Override
    public void setup() {
        //Level setup
        setupLevel();

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
            buttonKeys.DOWN = true;
        }
        if (key == 'd') {
           buttonKeys.RIGHT = true;
        }
        if (key == 'w') {
            buttonKeys.UP = true;
        }
        if (key == 's') {
            buttonKeys.LEFT = true;
        }
        if (key == '\uFFFF') {
            buttonKeys.SHIFT = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        char key = keyEvent.getKey();
        if (key == 'a') {
            buttonKeys.DOWN = false;
        }
        if (key == 'd') {
            buttonKeys.RIGHT = false;
        }
        if (key == 'w') {
            buttonKeys.UP = false;
        }
        if (key == 's') {
            buttonKeys.LEFT = false;
        }
        if (key == '\uFFFF') {
            buttonKeys.SHIFT = false;
        }
    }

    void updateKeys() {
        //TODO
//        if (buttonKeys.SHIFT) {
//            pSpeed = runningSpeed;
//        } else {
//            pSpeed = normalSpeed;
//        }

        if (buttonKeys.DOWN) {
            pa -= angleSpeed;
            if (pa < 0) {
                pa += TWO_PI;
            }

            pdx = cos(pa) * pSpeed;
            pdy = sin(pa) * pSpeed;
        }

        if (buttonKeys.RIGHT) {
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

        if (buttonKeys.UP) {
            if (map[ipy*mapX + ipx_add_xo] == 0) {
                px += pdx;
            }
            if (map[ipy_add_yo*mapX + ipx] == 0) {
                py += pdy;
            }
        }

        if (buttonKeys.LEFT) {
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

                if (map[y*mapX + x] > 0) {
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

            //Vertical and horizontal map texture number (0 to 3 for 4 textures)
            int vmt = 0;
            int hmt = 0;
            int mt = 0;

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

                if (mp > 0 && mp < mapX * mapY && map[mp] > 0) {
                    //Wall found
                    hmt = map[mp] - 1;
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

                if (mp > 0 && mp < mapX * mapY && map[mp] > 0) {
                    //Wall found
                    vmt = map[mp] - 1;
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

            float shade = 1.0f;

            if (disV < disH) {
                rx = vx;
                ry = vy;
                disT = disV;
                mt = vmt;
                shade = 0.8f;
                shadingFactor = disT*0.8f;
                stroke(255 - shadingFactor, 0, 0);
            }

            if (disH < disV) {
                rx = hx;
                ry = hy;
                disT = disH;
                mt = hmt;
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

//            int lineH = (int) (0.6f * (squareSize*height)/disT);
            int lineH = (int) ((squareSize * height)/disT);

            //The ty_step is the texture size divided by the lineH
            //This step will be incremented in ty each loop,
            float ty_step = 32.0f/lineH;

            float ty_off = 0f;

            if (lineH > height) {
                //This is like the part of the wall that is not visible
                ty_off = (lineH - height)/2.0f;
                lineH = height;
            }

            int lineO = (height - lineH) /2; //Line offset

            float strokeWeight = rayWidth;
            strokeWeight(strokeWeight);
            strokeCap(PROJECT);

            //In processing, the line is drawn at the exact position specified,
            //and the strokeWeight occupies space beginning from the center.
            //If the line has X = 50, and strokeWeight = 10,
            //the resulting line will start at X=45 and end at X=55
            float strokeOffset = strokeWeight/2;

            //The final ty value will be at most 31.
            float ty = ty_off * ty_step;

            float tx;

            if (shade == 1) {
                //Horizontal wall hit
                tx = (int) (rx/textureRatio) % 32;
                //Flip if looking down
                if (ra < PI) {
                    tx = 31 - tx;
                }

            } else {
                //Vertical wall hit
                tx = (int) (ry/textureRatio) % 32;
                //Flip if looking left
                if (ra > HALF_PI && ra < 3*HALF_PI) {
                    tx = 31 - tx;
                }
            }

            //Wall as sequence of points starting from lineO to lineH+lineO
            for (int y = 0; y < lineH; y++) {
                int c = wallTexture.textureMap[(int) ty * 32 + (int) tx];

                //Extract method
//                float light = 0.0f;
//                for (Light l : lights) {
//                    float dx = rx - l.x;
//                    float dy = ry - l.y;
//                    float dist = sqrt(dx * dx + dy * dy);
//                    float contribution = l.intensity * (1.0f - (dist / l.radius));
//                    if (contribution > 0) {
//                        light += contribution;
//                    }
//                }
//                light = constrain(light, 0.2f, 1.0f);

                shade = getShade(disT);

                stroke(wallTexture.r * c * shade,
                       wallTexture.g * c * shade,
                       wallTexture.b * c * shade);

                point(r*rayWidth + 512 + strokeOffset, y+lineO);
                ty += ty_step;
            }

            //Floor casting
            for (int y = lineO + lineH; y < height; y++) {
                // Distance from player to floor point at this pixel row
                float dy = y - (height / 2.0f); // distance from center screen

                //This is like the "straight distance" to the floor point
                float rowDistance = (squareSize * height/2.0f) / dy;

                //Corrects the distance considering the angle relative to the player ca = (pa - ra)
                float d = rowDistance / cos(ca);

                shade = getShade(d);

                // Calculate world coordinates of the floor point for this pixel
                float floorX = px + d * cos(ra);
                float floorY = py + d * sin(ra);

                // Convert world coords to texture coords
                int texX = ((int)(floorX / textureRatio)) % 32;
                int texY = ((int)(floorY / textureRatio)) % 32;

                if (texX < 0) texX += 32;
                if (texY < 0) texY += 32;

                // Get pixel color from floor texture (you can pick which texture to use)
                int c = floorTexture.textureMap[texY * 32 + texX];

                //Extract method
//                float light = 0.0f;
//
//                for (Light l : lights) {
//                    float dx = floorX - l.x;
//                    float dy2 = floorY - l.y;
//                    float dist = sqrt(dx * dx + dy2 * dy2);
//                    float contribution = l.intensity * (1.0f - (dist / l.radius));
//                    if (contribution > 0) {
//                        light += contribution;
//                    }
//                }

//                light = constrain(light, 0.2f, 1.0f); // ambient min brightness

                // Optionally darken the floor a bit to give depth
                stroke(floorTexture.r * c * shade,
                       floorTexture.g * c * shade,
                       floorTexture.b * c * shade);

                // Draw pixel (1 column per ray)
                point(r*rayWidth + 512 + strokeOffset, y);
            }

            for (int y = 0; y < lineO; y++) {
                // Distance from player to ceiling point at this pixel row
                float dy = (height / 2.0f) - y; // from center screen upwards
                float rowDistance = (squareSize * height) / (2.0f * dy);

                //Corrects the distance considering the angle relative to the player ca = (pa - ra)
                float d = rowDistance / cos(ca);

                shade = getShade(d);

                // World coordinates
                float ceilingX = px + d * cos(ra);
                float ceilingY = py + d * sin(ra);

                int texX = ((int)(ceilingX / textureRatio)) % 32;
                int texY = ((int)(ceilingY / textureRatio)) % 32;

                if (texX < 0) texX += 32;
                if (texY < 0) texY += 32;

                int c = ceilingTexture.textureMap[texY * 32 + texX];

                stroke(ceilingTexture.r * c * shade,
                       ceilingTexture.g * c * shade,
                       ceilingTexture.b * c * shade);

                point(r * rayWidth + 512 + strokeOffset, y);
            }

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

    float getShade(float disT) {
        var shade = 1.0f - (disT / maxDist);
        return max(shade, minBrightness);
    }

    public void setupLevel() {
        Level currentLevel;
        if (levelIterator.hasNext()) {
            currentLevel = levelIterator.next();

            mapX = currentLevel.mapX;
            mapY = currentLevel.mapY;
            map = currentLevel.map;

            //This is basically the same as map.length
            mapS = mapX * mapY;

            System.out.println("Map one dimensional");
            System.out.println(Arrays.toString(map));

            //Represents the size (height and width) of each square in the minimap
            //It's the total space available divided by the number of rows/cols
            //Also represents the distance to the projection plane
            squareSize = (float) topViewMapSize / (float) mapY;

            //Distance at which it’s fully dark
            maxDist = 5*squareSize;

            //Distance in front of player for wall collisions
            //It's equal to 1/3 of the squareSize viewed in the topDownView
            pWallDistance = squareSize/3;

            //How many textures fit in one square.
            textureRatio = squareSize/32.0f;

            //The player movement speed.
            pSpeed = squareSize / 20;
            normalSpeed = pSpeed;
            runningSpeed = pSpeed * 2.0f;

            ceilingTexture = currentLevel.ceilingTexture;
            floorTexture = currentLevel.floorTexture;
            wallTexture = currentLevel.wallTexture;

//        lights.add(new Light(squareSize * 1.5f, squareSize * 1.5f, 1.0f, squareSize)); // player light
//        lights.add(new Light(squareSize * 1.5f, squareSize * 3.5f, 1.0f, squareSize));
//        lights.add(new Light(squareSize * 1.5f, squareSize * 5.5f, 1.0f, squareSize));
//        lights.add(new Light(squareSize * 1.5f, squareSize * 7.5f, 1.0f, squareSize));
//        lights.add(new Light(squareSize * 3.5f, squareSize * 1.5f, 1.0f, squareSize));
//        lights.add(new Light(squareSize * 5.5f, squareSize * 1.5f, 1.0f, squareSize));
//        lights.add(new Light(squareSize * 7.5f, squareSize * 1.5f, 1.0f, squareSize));
        } else {
            System.out.println("Game Finished");
        }
    }
}
