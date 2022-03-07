package ru.kontur.intern.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kontur.intern.exception.IllegalImageSizeException;
import ru.kontur.intern.exception.ImageNotFoundException;
import ru.kontur.intern.service.ImageService;

import javax.imageio.ImageIO;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

@Validated
@RestController
@AllArgsConstructor
public class ImageController {
    private ImageService imageService;
    @PostMapping("/chartas/")
    public ResponseEntity<String> createCharta(@RequestParam @Max(20000) @Min(1) int width,
                                               @RequestParam @Max(50000) @Min(1) int height) {
        String chartaId = imageService.createImage(width, height);
        return ResponseEntity.created(URI.create("/chartas/" + chartaId)).body(chartaId);
    }

    @PostMapping("/chartas/{id}/")
    public ResponseEntity<?> fillCharta(@PathVariable String id,
                                           @RequestParam @Min(1) @Max(20000) int width,
                                           @RequestParam @Min(1) @Max(50000) int height,
                                           @RequestParam @Min(0) int x,
                                           @RequestParam @Min(0) int y,
                                        @RequestParam("image") MultipartFile image) throws IOException {
        var source =  ImageIO.read(image.getInputStream());
        imageService.fillImage(id, width, height, x, y, source);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/chartas/{id}/")
    public ResponseEntity<?> getChartaPart(@PathVariable String id,
                                           @RequestParam @Min(1) @Max(5000) int width,
                                           @RequestParam @Min(1) @Max(5000) int height,
                                           @RequestParam @Min(0) int x,
                                           @RequestParam @Min(0) int y) {
        BufferedImage chartaPart = imageService.getImagePart(id, width, height, x, y);
        return ResponseEntity
                .ok()
                .contentType(MediaType.asMediaType(MimeType.valueOf("image/bmp")))
                .body(chartaPart);
    }

    @DeleteMapping("/chartas/{id}")
    public ResponseEntity<Void> deleteCharta(@PathVariable String id) {
        imageService.deleteImage(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler({ConstraintViolationException.class, IllegalImageSizeException.class})
    public ResponseEntity<String> handleBadRequest(Exception e) {
        return new ResponseEntity<>(String.format("Validation error: %s", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<String> handleImageNotFoundException(ImageNotFoundException e) {
        return new ResponseEntity<>(String.format("Image not found by id: %s", e.getMessage()), HttpStatus.NOT_FOUND);
    }

}
