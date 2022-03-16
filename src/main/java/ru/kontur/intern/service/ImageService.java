package ru.kontur.intern.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kontur.intern.exception.IllegalImageSizeException;
import ru.kontur.intern.exception.ImageNotFoundException;
import ru.kontur.intern.exception.OffsetOutOfRangeException;
import ru.kontur.intern.repo.ImageRepo;

import java.awt.image.BufferedImage;

@Service
@AllArgsConstructor
public class ImageService {
    private ImageRepo imageRepo;

    /**
     * Creates an image with a specified size and fills it with black (0,0,0) color.
     *
     * @param width  width of the created image
     * @param height height of the created image
     * @return uniq created image identifier
     */
    public String createImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        return imageRepo.saveImage(image);
    }

    /**
     * Draws as much of the source image to the target image with specified offset;
     * The image is drawn with its top-left corner at (x, y) in this graphics context's coordinate space.
     * Fragments outside of target image dimension gets ignored.
     *
     * @param targetId Id of target image
     * @param width    width of source image
     * @param height   height of source image
     * @param x        width offset
     * @param y        height offset
     * @param source   source image
     * @throws ImageNotFoundException    if source image can not be found by {@code id}
     * @throws IllegalImageSizeException if source image dimensions do not match width and height params
     */
    public void insertImage(String targetId, int width, int height, int x, int y, BufferedImage source) {
        if (source.getHeight() != height || source.getWidth() != width) {
            throw new IllegalImageSizeException(source.getWidth(), width, source.getHeight(), height);
        } else {
            var target = imageRepo.readImage(targetId);
            target.getGraphics().drawImage(source, x, y, null);
            imageRepo.updateImage(targetId, target);
        }
    }

    /**
     * Gets a fragment of image with a specified id.
     * Returns as much of the source image as is currently available.
     * The image is drawn with its top-left corner at (x, y) in this graphics context's coordinate space.
     * Fills extra space with black (0,0,0) color if fragment size is larger than source image
     *
     * @param id     source image identifier
     * @param width  width of fragment
     * @param height height of fragment
     * @param x      width offset
     * @param y      height offset
     * @throws ImageNotFoundException    if source image can not be found by {@code id}
     * @throws OffsetOutOfRangeException if fragment offset out of source image dimensions
     */
    public BufferedImage getImagePart(String id, int width, int height, int x, int y) {
        BufferedImage sourceImage = imageRepo.readImage(id);
        if (x >= sourceImage.getWidth() || y >= sourceImage.getHeight()) {
            throw new OffsetOutOfRangeException(sourceImage.getWidth(), x, sourceImage.getHeight(), y);
        }

        BufferedImage imagePart = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int sourceWidth = Math.min(width, sourceImage.getWidth() - x);
        int sourceHeight = Math.min(height, sourceImage.getHeight() - y);
        imagePart.getGraphics().drawImage(sourceImage.getSubimage(x, y, sourceWidth, sourceHeight),
                0, 0, null);
        return imagePart;
    }

    /**
     * Removes an image with a specified id.
     *
     * @param id image identifier
     */
    public void deleteImage(String id) {
        imageRepo.deleteImage(id);
    }
}
