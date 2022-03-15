package ru.kontur.intern.unit;

import java.awt.image.BufferedImage;

public class AbstractTest {
    public static final String TEMP_FOLDER_PATH = "src/test/resources/test";
    public static final String TEST_IMAGE = "src/test/resources/TestImage/input/input1.bmp";
    public static final String BLANK_IMAGE = "src/test/resources/TestImage/input/empty40x40.bmp";
    public static final String TARGET_IMAGE_ID = "50e5b4b5-c8ae-4994-8b66-bede185261b9";

    /**
     * Compares two images pixel by pixel.
     *
     * @param imgA the first image.
     * @param imgB the second image.
     * @return whether the images are both the same or not.
     */
    public static boolean compareBufferedImages(BufferedImage imgA, BufferedImage imgB) {
        // The images must be the same size.
        if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
            return false;
        }

        int width  = imgA.getWidth();
        int height = imgA.getHeight();

        // Loop over every pixel.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Compare the pixels for equality.
                if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }
}
