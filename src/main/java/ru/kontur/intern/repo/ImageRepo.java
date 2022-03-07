package ru.kontur.intern.repo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.kontur.intern.exception.ImageNotFoundException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Repository
public class ImageRepo {
    @Value("#{springApplicationArguments.nonOptionArgs.get(0)}")
    private String folderPath;

    public String saveImage(BufferedImage image) {
        setUp();

        String id = UUID.randomUUID().toString();
        try {
            ImageIO.write(image, "bmp", new File(folderPath + "/" + id+ ".bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return id;
    }

    private void setUp() {
        if(!Files.exists(Path.of(folderPath))) {
            try {
                Files.createDirectory(Path.of(folderPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BufferedImage readImage(String id) {
        String imagePath = String.format("%s/%s.bmp", folderPath, id);
        try {
            return ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new ImageNotFoundException(imagePath);
        }
    }

    public void updateImage(String id, BufferedImage target) {
        String targetPath = String.format("%s/%s.bmp", folderPath, id);
        try {
            ImageIO.write(target, "bmp", new File(targetPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteImage(String id) {
        Path imagePath = Path.of(String.format("%s/%s.bmp", folderPath, id));
        if (Files.exists(imagePath)) {
            try {
                Files.delete(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
