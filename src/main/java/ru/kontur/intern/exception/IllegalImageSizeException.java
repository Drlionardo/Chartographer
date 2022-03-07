package ru.kontur.intern.exception;

public class IllegalImageSizeException extends RuntimeException {
    public IllegalImageSizeException(int imageWidth, int width, int imageHeight, int height) {
        super(String.format("Image sized does not match parameters: Image width=%s, Given width=%s, Image height=%s, Given height=%s",
                imageWidth, width, imageHeight, height));
    }
}
