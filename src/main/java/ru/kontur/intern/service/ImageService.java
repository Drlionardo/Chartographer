package ru.kontur.intern.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public class ImageService {

    public String createCharta(int width, int height) {
        return "ID";
    }

    public void fillCharta(int width, int height, int x, int y, MultipartFile image) {
    }

    public File getChartaPart(String id, int width, int height, int x, int y) {
        return new File("static/test_image.bmp");

    }

    public void deleteCharta(String id) {
    }
}
