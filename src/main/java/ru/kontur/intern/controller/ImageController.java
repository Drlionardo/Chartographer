package ru.kontur.intern.controller;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kontur.intern.exception.IllegalImageSizeException;
import ru.kontur.intern.exception.ImageNotFoundException;
import ru.kontur.intern.exception.NoAppropriateWriterException;
import ru.kontur.intern.exception.OffsetOutOfRangeException;
import ru.kontur.intern.service.ImageService;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

import static ru.kontur.intern.config.ImageSizeConfig.FULL_IMAGE_WIDTH_LIMIT;
import static ru.kontur.intern.config.ImageSizeConfig.FULL_IMAGE_HEIGHT_LIMIT;
import static ru.kontur.intern.config.ImageSizeConfig.IMAGE_SEGMENT_WIDTH_LIMIT;
import static ru.kontur.intern.config.ImageSizeConfig.IMAGE_SEGMENT_HEIGHT_LIMIT;

@Validated
@RestController
@AllArgsConstructor
@Log4j2
public class ImageController {
    private ImageService imageService;

    @PostMapping("/chartas/")
    public ResponseEntity<String> createImage(@RequestParam @Max(FULL_IMAGE_WIDTH_LIMIT) @Min(1) int width,
                                              @RequestParam @Max(FULL_IMAGE_HEIGHT_LIMIT) @Min(1) int height) {
        String imageId = imageService.createImage(width, height);
        return ResponseEntity.created(URI.create("/chartas/" + imageId)).body(imageId);
    }

    @PostMapping("/chartas/{id}/")
    public ResponseEntity<String> insertImage(@PathVariable String id,
                                                  @RequestParam @Min(1) @Max(FULL_IMAGE_WIDTH_LIMIT) int width,
                                                  @RequestParam @Min(1) @Max(FULL_IMAGE_HEIGHT_LIMIT) int height,
                                                  @RequestParam @Min(0) int x,
                                                  @RequestParam @Min(0) int y,
                                                  HttpServletRequest request) throws IOException {
        var sourceImage =  ImageIO.read(request.getInputStream());
        imageService.insertImage(id, width, height, x, y, sourceImage);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/chartas/{id}/")
    public ResponseEntity<BufferedImage> getImagePart(@PathVariable String id,
                                          @RequestParam @Min(1) @Max(IMAGE_SEGMENT_WIDTH_LIMIT) int width,
                                          @RequestParam @Min(1) @Max(IMAGE_SEGMENT_HEIGHT_LIMIT) int height,
                                          @RequestParam @Min(0) int x,
                                          @RequestParam @Min(0) int y) {
        BufferedImage imagePart = imageService.getImagePart(id, width, height, x, y);
        return ResponseEntity
                .ok()
                .contentType(MediaType.asMediaType(MimeType.valueOf("image/bmp")))
                .body(imagePart);
    }

    @DeleteMapping("/chartas/{id}/")
    public ResponseEntity<Void> deleteImage(@PathVariable String id) {
        imageService.deleteImage(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler({ConstraintViolationException.class, IllegalImageSizeException.class, OffsetOutOfRangeException.class})
    public ResponseEntity<String> handleBadRequest(Exception e) {
        return new ResponseEntity<>(String.format("Validation error: %s", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<String> handleImageNotFoundException(ImageNotFoundException e) {
        return new ResponseEntity<>(String.format("Image not found by id: %s", e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({IOException.class, NoAppropriateWriterException.class})
    public ResponseEntity<String> handleInternalException(Exception e) {
        log.error(e.getMessage());
        return new ResponseEntity<>("Internal server error, please report", HttpStatus.INTERNAL_SERVER_ERROR );
    }
}
