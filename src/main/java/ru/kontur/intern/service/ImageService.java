package ru.kontur.intern.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
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

    public void fillCharta(int width, int height, int x, int y, MultipartFile image) {
    }

    public File getChartaPart(String id, int width, int height, int x, int y) {
        return new File("static/test_image.bmp");

    }

    public void deleteCharta(String id) {
    }
}
