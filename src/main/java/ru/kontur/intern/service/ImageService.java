package ru.kontur.intern.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kontur.intern.exception.IllegalImageSizeException;
import ru.kontur.intern.exception.ImageNotFoundException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ImageService {
    @Value("#{springApplicationArguments.nonOptionArgs.get(0)}")
    private String path;

    public String createCharta(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        String id = UUID.randomUUID().toString();
        if(!Files.exists(Path.of(path))) {
            try {
                Files.createDirectory(Path.of(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            ImageIO.write(image, "bmp", new File(path + "/" + id+ ".bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return id;
    }

    public void fillCharta(String id, int width, int height, int x, int y, MultipartFile image) {
        try {
            var imagePart = ImageIO.read(image.getInputStream());
            if(imagePart.getHeight() != height || imagePart.getWidth() != width) {
                throw new IllegalImageSizeException(imagePart.getWidth(), width, imagePart.getHeight(), height);
            } else {
                BufferedImage target = readImage(id);
                target.getGraphics().drawImage(imagePart, x, y, null);
                String targetPath = String.format("%s/%s.bmp", path, id);
                ImageIO.write(target, "bmp", new File(targetPath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public BufferedImage getChartaPart(String id, int width, int height, int x, int y) {
        BufferedImage sourceImage = readImage(id);
        if(x > sourceImage.getWidth() || y > sourceImage.getHeight()) {
            throw new IllegalImageSizeException(width, x, height, y);
        }
        //todo: fix outside of Raster error
        BufferedImage imagePart = sourceImage.getSubimage(x, y, width, height);
        return imagePart;
    }

    public void deleteCharta(String id) {
        Path chartaPath = Path.of(String.format("%s/%s.bmp",path, id));
        if(Files.exists(chartaPath)) {
            try {
                Files.delete(chartaPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private BufferedImage readImage(String id) {
        String imagePath = String.format("%s/%s.bmp",path, id);
        try {
            return ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new ImageNotFoundException(imagePath);
        }
    }
}
