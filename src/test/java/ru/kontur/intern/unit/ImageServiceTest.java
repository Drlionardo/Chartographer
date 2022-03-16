package ru.kontur.intern.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kontur.intern.exception.IllegalImageSizeException;
import ru.kontur.intern.exception.OffsetOutOfRangeException;
import ru.kontur.intern.repo.ImageRepo;
import ru.kontur.intern.service.ImageService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.*;

@SpringBootTest
public class ImageServiceTest extends AbstractTest {
    @Autowired
    private ImageService service;
    @MockBean
    private ImageRepo repo;


    @Test
    void createImageTest() {
        doReturn(TARGET_IMAGE_ID).when(repo).saveImage(any(BufferedImage.class));
        Assertions.assertEquals(TARGET_IMAGE_ID, service.createImage(20, 40));
    }

    @Test
    void getFullImage() throws IOException {
        var fullImage = ImageIO.read(new File(TEST_IMAGE));
        doReturn(fullImage).when(repo).readImage(anyString());

        //TEST_IMAGE has 20x40 size
       Assertions.assertTrue(compareBufferedImages(fullImage, service.getImagePart(TARGET_IMAGE_ID,20, 40, 0, 0)));
    }
    @Test
    void getImageLargerThanSource() throws IOException {
        var fullImage = ImageIO.read(new File(TEST_IMAGE));
        doReturn(fullImage).when(repo).readImage(anyString());

        String path = "src/test/resources/TestImage/output/partLargerThanSource.bmp";
        var expected = ImageIO.read(new File(path));
        Assertions.assertTrue(compareBufferedImages(expected, service.getImagePart(TARGET_IMAGE_ID,25, 50, 0, 0)));
    }

    @Test
    void getFullImageOffset() throws IOException {
        var fullImage = ImageIO.read(new File(TEST_IMAGE));
        var expectedImage = ImageIO.read(new File("src/test/resources/TestImage/output/overlapOutput.bmp"));
        doReturn(fullImage).when(repo).readImage(anyString());

        Assertions.assertTrue(compareBufferedImages(expectedImage, service.getImagePart(TARGET_IMAGE_ID,20, 40, 10, 20)));
    }

    @Test
    void getFullImageOffsetOutOfTarget() throws IOException {
        var fullImage = ImageIO.read(new File(TEST_IMAGE));
        doReturn(fullImage).when(repo).readImage(anyString());

        //TEST_IMAGE has 20x40 size
        Assertions.assertThrows(OffsetOutOfRangeException.class,
                () -> service.getImagePart(TARGET_IMAGE_ID,20, 40, 20, 40));
    }

    @Test
    void deleteTest() {
        doNothing().when(repo).deleteImage(anyString());

        service.deleteImage(TARGET_IMAGE_ID);

        verify(repo).deleteImage(anyString());
    }

    @Test
    void insertImageTest() throws IOException {
        var fullImage = ImageIO.read(new File(BLANK_IMAGE));
        var sourceImage = ImageIO.read(new File(TEST_IMAGE));
        doReturn(fullImage).when(repo).readImage(anyString());
        doNothing().when(repo).updateImage(anyString(), any(BufferedImage.class));

        service.insertImage(TARGET_IMAGE_ID, 20, 40, 0, 0, sourceImage);

        var expectedImage = ImageIO.read(new File("src/test/resources/TestImage/output/output1.bmp"));
        verify(repo).updateImage(eq(TARGET_IMAGE_ID), argThat((actual) -> compareBufferedImages(expectedImage, actual)));
    }

    @Test
    void insertImageWithOffsetTest() throws IOException {
    }

    @Test
    void insertImageIncorrectSizeTest() {
        //TEST_IMAGE has 20x40 size
        Assertions.assertThrows(IllegalImageSizeException.class,
                () -> service.insertImage(TARGET_IMAGE_ID, 100, 100, 0, 0,
                        ImageIO.read(new File(TEST_IMAGE))));
    }
}

