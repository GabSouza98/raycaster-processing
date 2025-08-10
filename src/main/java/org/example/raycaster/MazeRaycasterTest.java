package org.example.raycaster;

import org.example.raycaster.levels.Level;
import org.example.raycaster.levels.LevelManager;
import org.example.raycaster.texture.Texture;
import org.example.raycaster.texture.textures.BrickTexture;
import org.example.raycaster.texture.textures.CheckerboardTexture;
import org.example.raycaster.texture.textures.WindowTexture;
import org.example.raycaster.ui.Button;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.sound.FFT;
import processing.sound.SoundFile;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.nonNull;

public class MazeRaycasterTest extends PApplet {

    public static PApplet processing;

    private LevelManager levelManager;
    private Iterator<Level> levelIterator;
    private GameState gameState;

    public final ButtonKeys buttonKeys = new ButtonKeys();

    //Player coordinates
    float px;
    float py;
    float pdx;
    float pdy;
    float pa;
    float angleSpeed = 0.05f;
    float mouseHorizontalSpeed = 0.01f;
    float mouseVerticalSpeed = 5.0f;

    //These come from the Level class
    int[] map;
    int mapX;
    int mapY;
    int mapS;
    int winningPosition;
    Texture floorTexture;
    Texture ceilingTexture;
    Texture wallTexture;
    List<Light> lights;

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

    float cameraYOffset = 0.0f;
    float cameraSpeedY = 10.0f; // adjust to taste
    float centerY;

    float distShadeFactor = 0.0f;
    float lightShadeFactor = 0.0f;
    float soundShadeFactor;

    float aspectRatio = 1.0f;

    //Controls the opacity of the map while holding M
    float alpha = 180f;

    float dummy = 0.01f;

    Button button1;

    SoundFile theme;
    FFT fft;
    int bands = 256;
    float[] spectrum = new float[bands];
    float smoothedBass = 0;

    public static void main(String[] args) {
        PApplet.main("org.example.raycaster.MazeRaycasterTest", args);
    }

    @Override
    public void settings() {
        size(600, 600);
        centerY = height/2.0f + cameraYOffset;
        processing = this;

        button1 = new Button((int)(0.5f*width), (int)(0.5f*height), 200, 100, color(255, 0, 0), "START GAME");

        gameState = GameState.INITIALIZING;

        topViewMapSize = height;
        raycasterHeight = height;
        raycasterWidth = width;

//        levelManager = new LevelManager(CustomLevelGenerator.getLevelList());
        levelManager = new LevelManager(false);
        levelIterator = levelManager.levelList.iterator();
    }

    @Override
    public void setup() {
        //Level setup
        setupLevel();

//        String[] fontList = PFont.list();
//        printArray(fontList);

        background(0);
        gameState = GameState.TITLE_SCREEN;
        theme.loop();
    }

    @Override
    public void draw() {
        fft.analyze(spectrum);

        float bass = 0;
        for (int i = 0; i <= 10; i++) {
            bass += spectrum[i];
        }

        // Smooth it: mix old and new value
        float smoothingFactor = 0.1f; // Lower = smoother
        smoothedBass = lerp(smoothedBass, bass, smoothingFactor);

        soundShadeFactor = smoothedBass*0.8f;

        switch (gameState) {
            case TITLE_SCREEN:
                background(0);
                cursor();
                button1.display();
                break;
            case PLAYING:
                noCursor();
                background(0);
                updateKeys();
                drawRays2D();
                drawMap2D();
                drawPlayer();
                checkWin();
//                aspectRatio = 0.5f + abs(sin(dummy));
//                dummy += 0.005f;
                break;
            case FINISHED:
                System.out.println("You won!");
                gameState = GameState.TITLE_SCREEN;
                levelManager = new LevelManager(false);
                levelIterator = levelManager.levelList.iterator();
                setupLevel();
                break;
        }
    }

    private void checkWin() {
        int mx = (int) (px/squareSize);
        int my = (int) (py/squareSize);
        int mp = my * mapX + mx;

        if (mp == winningPosition) {
            if (theme != null) {
                theme.stop();
                theme.removeFromCache();
                theme = null;
            }
            setupLevel();
        }
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        int dx = mouseX - pmouseX;
        int dy = mouseY - pmouseY;

        if (dx != 0) {
            if (dx > 0) {
                pa += mouseHorizontalSpeed;
                if (pa > TWO_PI) {
                    pa -= TWO_PI;
                }

                pdx = cos(pa) * pSpeed;
                pdy = sin(pa) * pSpeed;
            }

            if (dx < 0) {
                pa -= mouseHorizontalSpeed;
                if (pa < 0) {
                    pa += TWO_PI;
                }

                pdx = cos(pa) * pSpeed;
                pdy = sin(pa) * pSpeed;
            }
        }

        if (dy != 0) {
            if (dy > 0) {
                cameraYOffset -= mouseVerticalSpeed;
            }

            if (dy < 0) {
                cameraYOffset += mouseVerticalSpeed;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        char key = keyEvent.getKey();
        if (key == 'a') {
            buttonKeys.LEFT = true;
        }
        if (key == 'd') {
           buttonKeys.RIGHT = true;
        }
        if (key == 'w') {
            buttonKeys.FORWARD = true;
        }
        if (key == 's') {
            buttonKeys.BACKWARD = true;
        }
        if (key == '\uFFFF') {
            buttonKeys.SHIFT = true;
        }
        if (key == 'q') {
            buttonKeys.LOOK_UP = true;
        }
        if (key == 'e') {
            buttonKeys.LOOK_DOWN = true;
        }
        if (key == 'm') {
            buttonKeys.MAP = true;
        }
        if (key == 'i') {
            distShadeFactor += 0.05f;
            System.out.println(distShadeFactor);
        }
        if (key == 'o') {
            distShadeFactor -= 0.05f;
            System.out.println(distShadeFactor);
        }
        if (key == 'k') {
            lightShadeFactor += 0.05f;
            System.out.println(lightShadeFactor);
        }
        if (key == 'l') {
            lightShadeFactor -= 0.05f;
            System.out.println(lightShadeFactor);
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        char key = keyEvent.getKey();
        if (key == 'a') {
            buttonKeys.LEFT = false;
        }
        if (key == 'd') {
            buttonKeys.RIGHT = false;
        }
        if (key == 'w') {
            buttonKeys.FORWARD = false;
        }
        if (key == 's') {
            buttonKeys.BACKWARD = false;
        }
        if (key == '\uFFFF') {
            buttonKeys.SHIFT = false;
        }
        if (key == 'q') {
            buttonKeys.LOOK_UP = false;
        }
        if (key == 'e') {
            buttonKeys.LOOK_DOWN = false;
        }
        if (key == 'm') {
            buttonKeys.MAP = false;
        }
    }

    void updateKeys() {
        //TODO
//        if (buttonKeys.SHIFT) {
//            pSpeed = runningSpeed;
//        } else {
//            pSpeed = normalSpeed;
//        }

        if (buttonKeys.LOOK_UP) {
            cameraYOffset += cameraSpeedY;
        }

        if (buttonKeys.LOOK_DOWN) {
            cameraYOffset -= cameraSpeedY;
        }

        if (buttonKeys.LEFT) {
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

        if (buttonKeys.FORWARD) {
            if (map[ipy*mapX + ipx_add_xo] == 0) {
                px += pdx;
            }
            if (map[ipy_add_yo*mapX + ipx] == 0) {
                py += pdy;
            }
        }

        if (buttonKeys.BACKWARD) {
            if (map[ipy*mapX + ipx_sub_xo] == 0) {
                px -= pdx;
            }
            if (map[ipy_sub_yo*mapX + ipx] == 0) {
                py -= pdy;
            }
        }
    }

    void drawPlayer() {
        if (buttonKeys.MAP) {
            stroke(255, 0, 0, alpha);
            strokeCap(ROUND); //makes the point to appear round
            strokeWeight(8);
            point(px, py);
            strokeWeight(3);
            line(px, py, px+pdx*5, py+pdy*5);
        }
    }

    void drawMap2D() {
        if (buttonKeys.MAP) {
            strokeWeight(2);
            stroke(100, alpha);

            int x, y, xo, yo;
            for (y = 0; y < mapY; y++) {
                for (x = 0; x < mapX; x++) {

                    if (map[y*mapX + x] > 0) {
                        //black
                        fill(255, alpha);
                    } else {
                        //white
                        fill(0, alpha);
                    }

                    xo = (int) (x*squareSize);
                    yo = (int) (y*squareSize);

                    rect(xo, yo, squareSize, squareSize);
                }
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
                    disH = dist(px, py, hx, hy);
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
                    disV = dist(px, py, vx, vy);
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
                stroke(255 - shadingFactor, 0, 0, alpha);
            }

            if (disH < disV) {
                rx = hx;
                ry = hy;
                disT = disH;
                mt = hmt;
                shadingFactor = disT*0.8f;
                stroke(180 - shadingFactor, 0, 0, alpha);
            }

            if (buttonKeys.MAP) {
                //Draw Ray
                strokeWeight(2);
                line(px, py, rx, ry);
            }

            //Draw 3D Walls
            float ca = pa - ra;
            if (ca < 0) {
                ca += TWO_PI;
            }
            if (ca > TWO_PI) {
                ca -= TWO_PI;
            }

            disT = disT * cos(ca); //Fix fish eye

            int lineH = (int) ((aspectRatio * squareSize * height)/disT);
            int originalLineH = lineH;

            centerY = height/2.0f + cameraYOffset;

            int originalLineO = (int) (centerY - originalLineH/2.0f);

            float strokeWeight = rayWidth;
            strokeWeight(strokeWeight);
            strokeCap(PROJECT);

            //In processing, the line is drawn at the exact position specified,
            //and the strokeWeight occupies space beginning from the center.
            //If the line has X = 50, and strokeWeight = 10,
            //the resulting line will start at X=45 and end at X=55
            float strokeOffset = strokeWeight/2;

            //The final ty and tx value will be at most 31.
            float ty;
            float tx;

            if (shade == 1) {
                //Horizontal wall hit
                    tx = (int) (rx/textureRatio) & 31;
                //Flip if looking down
                if (ra < PI) {
                    tx = 31 - tx;
                }

            } else {
                //Vertical wall hit
                    tx = (int) (ry/textureRatio) & 31;
                //Flip if looking left
                if (ra > HALF_PI && ra < 3*HALF_PI) {
                    tx = 31 - tx;
                }
            }

            int drawStart = -originalLineH/2 + height/2 + (int)cameraYOffset;
            if(drawStart < 0) drawStart = 0;
            int drawEnd = originalLineH/2 + height/2 + (int)cameraYOffset;
            if(drawEnd >= height) drawEnd = height - 1;

            //The ty_step is the texture size divided by the lineH
            //This step will be incremented in ty each loop,
            float ty_step = 32.0f/originalLineH;

            float texPos = ((drawStart - cameraYOffset - height/2.0f + originalLineH/2.0f) * ty_step);

            for (int y = drawStart; y < drawEnd; y++) {
                ty = (int) texPos & 31;
                int c = wallTexture.textureMap[(int) ty * 32 + (int) tx];

                float light = getSingleColorLight(rx, ry);
                shade = getShade(disT);

                float finalShade = (distShadeFactor*shade + lightShadeFactor*light + soundShadeFactor);
                finalShade = constrain(finalShade, shade, 1.0f);

                stroke(wallTexture.r * c * finalShade,
                        wallTexture.g * c * finalShade,
                        wallTexture.b * c * finalShade);
                point(r * rayWidth + strokeOffset, y);

                texPos += ty_step;
            }

            //Equation: dy / (h/2) = squareSize / rowDistance
            //rowDistance = squareSize * (h/2) / dy
            //rowDistance = constant / dy
            float constant = aspectRatio * squareSize * height/2.0f;
            //Floor and ceiling casting for Y-shearing
            for (int y = 0; y < originalLineO; y++) {
                // Distance from player to ceiling point at this pixel row
                float dyCeiling = centerY - y; // from center screen upwards

                float rowDistanceCeiling = constant / dyCeiling;

                //Corrects the distance considering the angle relative to the player ca = (pa - ra)
                float dCeiling = rowDistanceCeiling / cos(ca);
                float shadeCeiling = getShade(dCeiling);

                // World coordinates
                float ceilingX = px + dCeiling * cos(ra);
                float ceilingY = py + dCeiling * sin(ra);

                int texX = ((int)(ceilingX / textureRatio)) & 31;
                int texY = ((int)(ceilingY / textureRatio)) & 31;

                if (texX < 0) texX += 32;
                if (texY < 0) texY += 32;

                int c = ceilingTexture.textureMap[texY * 32 + texX];

                float light = getSingleColorLight(ceilingX, ceilingY);

                float finalShade = (distShadeFactor*shadeCeiling + lightShadeFactor*light + soundShadeFactor);
                finalShade = constrain(finalShade, shadeCeiling, 1.0f);

                stroke(ceilingTexture.r * c * finalShade,
                        ceilingTexture.g * c * finalShade,
                        ceilingTexture.b * c * finalShade);

                point(r * rayWidth + strokeOffset, y);
            }

            for (int y = originalLineO + originalLineH; y < height; y++) {
                // Distance from player to ceiling point at this pixel row
                float dyFloor = y - centerY;

                float rowDistanceFloor = constant / dyFloor;

                //Corrects the distance considering the angle relative to the player ca = (pa - ra)
                float dFloor = rowDistanceFloor / cos(ca);
                float shadeFloor = getShade(dFloor);

                // World coordinates
                float floorX = px + dFloor * cos(ra);
                float floorY = py + dFloor * sin(ra);

                int texX = ((int)(floorX / textureRatio)) & 31;
                int texY = ((int)(floorY / textureRatio)) & 31;

                if (texX < 0) texX += 32;
                if (texY < 0) texY += 32;

                int c = floorTexture.textureMap[texY * 32 + texX];

                float light = getSingleColorLight(floorX, floorY);

                float finalShade = (distShadeFactor*shadeFloor + lightShadeFactor*light + soundShadeFactor);
                finalShade = constrain(finalShade, shadeFloor, 1.0f);

                stroke(floorTexture.r * c * finalShade,
                        floorTexture.g * c * finalShade,
                        floorTexture.b * c * finalShade);

                point(r * rayWidth + strokeOffset, y);
            }

            //Walls painted inverted!!
//            for (int y = originalLineO; y < originalLineO + originalLineH; y++) {
//
//                float dy = abs(centerY - y); // from center screen upwards
//
//                float rowDistance = squareSize * centerY / dy;
//
//                //Corrects the distance considering the angle relative to the player ca = (pa - ra)
//                float dWall = rowDistance / cos(ca);
//                float shadeWall = getShade(dWall);
//
//                // World coordinates
//                float wallX = px + dWall * cos(ra);
//                float wallY = py + dWall * sin(ra);
//
//                int texX = ((int)(wallX / textureRatio)) & 31;
//                int texY = ((int)(wallY / textureRatio)) & 31;
//
//                if (texX < 0) texX += 32;
//                if (texY < 0) texY += 32;
//
//                int c = wallTexture.textureMap[texY * 32 + texX];
//
//                float light = getSingleColorLight(wallX, wallY);
//
//                float finalShade = (distShadeFactor*shadeWall + lightShadeFactor*light);
//                finalShade = constrain(finalShade, shadeWall, 1.0f);
//
//                stroke(wallTexture.r * c * finalShade,
//                       wallTexture.g * c * finalShade,
//                       wallTexture.b * c * finalShade);
//
//                point(r * rayWidth + strokeOffset, y);
//            }

            ra += DEG_TO_RAD * angleIncrement;

            if (ra < 0) {
                ra += TWO_PI;
            }
            if (ra > TWO_PI) {
                ra -= TWO_PI;
            }
        }
    }

    private PVector getRGBLight(float x, float y) {
        float r = 0;
        float g = 0;
        float b = 0;

        for (Light l : lights) {
            float dx = x - l.x;
            float dy2 = y - l.y;
            float dist = sqrt(dx * dx + dy2 * dy2);
            float contribution = l.intensity * (1.0f - (dist / l.radius));
            if (contribution > 0) {
                // Scale light color by influence
                r += l.r * contribution;
                g += l.g * contribution;
                b += l.b * contribution;
            }
        }

        return new PVector(r, g, b);
    }

    private float getSingleColorLight(float x, float y) {
        float light = 0.0f;
        for (Light l : lights) {
            float dx = x - l.x;
            float dy2 = y - l.y;
            float dist = sqrt(dx * dx + dy2 * dy2);
            float contribution = l.intensity * (1.0f - (dist / l.radius));
            if (contribution > 0) {
                light += contribution;
            }
        }

        light = constrain(light, 0.0f, 1.0f); // ambient min brightness
        return light;
    }

    float getShade(float disT) {
        var shade = 1.0f - (disT / (maxDist));
        return max(shade, minBrightness);
    }

    public void setupLevel() {
        Level currentLevel;

        if (levelIterator.hasNext()) {
            currentLevel = levelIterator.next();

            mapX = currentLevel.mapX;
            mapY = currentLevel.mapY;
            map = currentLevel.map;
            ceilingTexture = new WindowTexture();
//            ceilingTexture = currentLevel.ceilingTexture;
            floorTexture = new CheckerboardTexture();
//            floorTexture = currentLevel.floorTexture;
            wallTexture = new BrickTexture();
//            wallTexture = currentLevel.wallTexture;
            lights = currentLevel.lights;
            theme = currentLevel.song.soundFile;

            theme.loop();
            fft = new FFT(this, bands);
            fft.input(theme);

            //This is basically the same as map.length
            mapS = mapX * mapY;

            winningPosition = (mapY-2)*mapX + (mapX-2);

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

            float halfSquareSize = 0.5f * squareSize;

            if (nonNull(lights) && !lights.isEmpty()) {
                lights.forEach(light -> {
                    light.y = (light.i * squareSize) + halfSquareSize;
                    light.x = (light.j * squareSize) + halfSquareSize;
                    light.radius = light.radius * squareSize;
                });
            }

            //Starts on the map[1][1] position, first available square.
            px = squareSize * 1.5f;
            py = squareSize * 1.5f;
            // Zero is looking ->
            // pi/2 is looking down.
            // The circle is clockwise
            pa = HALF_PI;
            pdx = cos(pa) * pSpeed;
            pdy = sin(pa) * pSpeed;
        } else {
            gameState = GameState.FINISHED;
            System.out.println("Game Finished");
        }
    }

    @Override
    public void mousePressed() {
        if (button1.isPressed()) {
            gameState = GameState.PLAYING;
        }
    }
}
