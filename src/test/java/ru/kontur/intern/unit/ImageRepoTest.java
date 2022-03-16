package ru.kontur.intern.unit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.kontur.intern.exception.ImageNotFoundException;
import ru.kontur.intern.exception.NoAppropriateWriterException;
import ru.kontur.intern.repo.ImageRepo;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

@SpringBootTest(args = "src/test/resources/test")
public class ImageRepoTest extends AbstractTest {
    @Autowired
    private ImageRepo imageRepo;

    @AfterAll
    static void cleanUp() throws IOException {
        if (Files.exists(Path.of(TEMP_FOLDER_PATH))) {
            Files.walk(Path.of(TEMP_FOLDER_PATH))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    void crudTest() throws IOException {
        var image = ImageIO.read(new File(BLANK_IMAGE));
        String id = imageRepo.saveImage(image);

        var imageFromRepo = imageRepo.readImage(id);
        Assertions.assertTrue(compareBufferedImages(image, imageFromRepo));

        var updatedImage = ImageIO.read(new File(TEST_IMAGE));
        imageRepo.updateImage(id, updatedImage);
        var updatedImageFromRepo = imageRepo.readImage(id);
        Assertions.assertTrue(compareBufferedImages(updatedImage, updatedImageFromRepo));

        imageRepo.deleteImage(id);
        Assertions.assertThrows(ImageNotFoundException.class, () -> imageRepo.readImage(id));
    }

    @Test
    void notFoundTest() {
        String id = UUID.randomUUID().toString();
        Assertions.assertThrows(ImageNotFoundException.class, () -> imageRepo.readImage(id));
    }

    @Test
    void noAppropriateWriterTest() throws IOException {
        var notBmpRgbImage = ImageIO.read(new File("src/test/resources/TestImage/input/bmpWithAlpha.bmp"));
        Assertions.assertThrows(NoAppropriateWriterException.class, () -> imageRepo.saveImage(notBmpRgbImage));
    }
}
