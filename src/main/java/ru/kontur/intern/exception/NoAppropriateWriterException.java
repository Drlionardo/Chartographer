package ru.kontur.intern.exception;

import java.awt.image.BufferedImage;

public class NoAppropriateWriterException extends RuntimeException {
    public NoAppropriateWriterException(BufferedImage image, String format) {
        super(String.format("Unable to get appropriate writer for image type - %s, requested format - %s ",
                image.getType(), format));
    }
}
