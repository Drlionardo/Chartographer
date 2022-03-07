package ru.kontur.intern.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageSizeConfig {
    public static final int FULL_IMAGE_HEIGHT_LIMIT = 50000;
    public static final int FULL_IMAGE_WIDTH_LIMIT = 20000;
    public static final int IMAGE_SEGMENT_WIDTH_LIMIT = 5000;
    public static final int IMAGE_SEGMENT_HEIGHT_LIMIT = 5000;
}
