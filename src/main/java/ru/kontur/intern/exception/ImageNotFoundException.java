package ru.kontur.intern.exception;

public class ImageNotFoundException extends RuntimeException{
    public ImageNotFoundException(String path) {
        super(String.format("Image not found at %s", path));
    }
}
