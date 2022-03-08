package ru.kontur.intern.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kontur.intern.exception.IllegalImageSizeException;
import ru.kontur.intern.repo.ImageRepo;

import java.awt.image.BufferedImage;

@Service
@AllArgsConstructor
public class ImageService {
    private ImageRepo imageRepo;

    public String createImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        return imageRepo.saveImage(image);
    }

    public void insertImage(String targetId, int width, int height, int x, int y, BufferedImage source) {
        if (source.getHeight() != height || source.getWidth() != width) {
            throw new IllegalImageSizeException(source.getWidth(), width, source.getHeight(), height);
        } else {
            var target = imageRepo.readImage(targetId);
            target.getGraphics().drawImage(source, x, y, null);
            imageRepo.updateImage(targetId, target);
        }
    }

    public BufferedImage getImagePart(String id, int width, int height, int x, int y) {
        BufferedImage sourceImage = imageRepo.readImage(id);
        if (x >= sourceImage.getWidth() || y >= sourceImage.getHeight()) {
            throw new IllegalImageSizeException(width, x, height, y);
        }

        BufferedImage imagePart = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int sourceWidth = Math.min(width, sourceImage.getWidth() - x);
        int sourceHeight = Math.min(width, sourceImage.getHeight() - y);
        imagePart.getGraphics().drawImage(sourceImage.getSubimage(x, y, sourceWidth, sourceHeight),
                0,0,null);
        return imagePart;
    }

    public void deleteImage(String id) {
        imageRepo.deleteImage(id);
    }
}
