package org.example.raycaster.texture;

import org.example.raycaster.texture.textures.*;

import java.util.Random;

public class TextureGenerator {

    private static final Random random = new Random();

    public static Texture[] allTextures = new Texture[] {
            new BrickTexture(),
            new CheckerboardTexture(),
            new DoorTexture(),
            new HeartTexture(),
            new NoiseTexture(),
            new WavyTexture(),
            new WindowTexture(),
    };

    public static Texture getRandomTexture() {
        int number = random.nextInt(allTextures.length);
        return allTextures[number];
    }

}
