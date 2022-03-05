package ru.kontur.intern.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
public class ImageService {
    @Value("#{springApplicationArguments.nonOptionArgs.get(0)}")
    private String path;

    public String createCharta(int width, int height) {
        return UUID.randomUUID().toString();
    }

    public void fillCharta(int width, int height, int x, int y, MultipartFile image) {
    }

    public File getChartaPart(String id, int width, int height, int x, int y) {
        return new File("static/test_image.bmp");

    }

    public void deleteCharta(String id) {
    }
}
