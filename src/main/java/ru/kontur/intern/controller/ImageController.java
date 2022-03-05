package ru.kontur.intern.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.File;
import java.net.URI;

@Validated
@RestController
public class ImageController {
    @PostMapping("/chartas/")
    public ResponseEntity<String> createCharta(@RequestParam @Max(20000) @Min(1) int width,
                                               @RequestParam @Max(50000) @Min(1) int height) {
        return ResponseEntity.created(URI.create("")).body("");
    }

    @PostMapping("/chartas/{id}/")
    public ResponseEntity<?> fillCharta(@PathVariable String id,
                                           @RequestParam @Min(1) @Max(20000) int width,
                                           @RequestParam @Min(1) @Max(50000) int height,
                                           @RequestParam @Min(0) int x,
                                           @RequestParam @Min(0) int y,
                                        @RequestParam("image") MultipartFile image) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.asMediaType(MimeType.valueOf("image/bmp")))
                .body(image.getResource());
    }


    @GetMapping("/chartas/{id}/")
    public ResponseEntity<?> getChartaPart(@PathVariable String id,
                                           @RequestParam @Min(1) @Max(5000) int width,
                                           @RequestParam @Min(1) @Max(5000) int height,
                                           @RequestParam @Min(0) int x,
                                           @RequestParam @Min(0) int y) {
        File testImage = new File("static/test_image.bmp");
        return ResponseEntity.ok().body(testImage);
    }

    @DeleteMapping("/chartas/{id}")
    public ResponseEntity<Void> deleteCharta() {
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>(String.format("Validation error: %s", e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
