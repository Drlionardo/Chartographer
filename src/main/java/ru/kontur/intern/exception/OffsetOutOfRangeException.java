package ru.kontur.intern.exception;

public class OffsetOutOfRangeException extends RuntimeException {
    public OffsetOutOfRangeException(int imageWidth, int widthOffset, int imageHeight, int heightOffset) {
        super(String.format("Image offset out of range: Image width=%s, Offset width=%s, Image height=%s, Offset height=%s",
                imageWidth, widthOffset, imageHeight, heightOffset));
    }
}
