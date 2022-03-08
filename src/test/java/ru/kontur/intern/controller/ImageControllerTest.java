package ru.kontur.intern.controller;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.kontur.intern.config.ImageSizeConfig.FULL_IMAGE_WIDTH_LIMIT;
import static ru.kontur.intern.config.ImageSizeConfig.FULL_IMAGE_HEIGHT_LIMIT;
import static ru.kontur.intern.config.ImageSizeConfig.IMAGE_SEGMENT_WIDTH_LIMIT;
import static ru.kontur.intern.config.ImageSizeConfig.IMAGE_SEGMENT_HEIGHT_LIMIT;

@SpringBootTest(args = "src/test/resources/test")
@AutoConfigureMockMvc
class ImageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private static final String TEMP_FOLDER_PATH = "src/test/resources/test";
    private final String NOT_EXISTING_ID = "notExistingId";

    @AfterAll
    static void cleanUp() throws IOException {
        if(Files.exists(Path.of(TEMP_FOLDER_PATH))) {
            Files.walk(Path.of(TEMP_FOLDER_PATH))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    void insertImageReturnsOk() throws Exception {
        String imageId = createImage(40, 40);
        MockMultipartFile image = getMockMultipartFile("src/test/resources/TestImage/input/input1.bmp");

        mockMvc.perform(MockMvcRequestBuilders.multipart(String.format("/chartas/%s/", imageId))
                        .file(image)
                        .param("width", "20")
                        .param("height", "40")
                        .param("x", "0")
                        .param("y", "0"))
                .andDo(print())
                .andExpect(status().isOk());

        var expectedOutput = Files.newInputStream(Path.of("src/test/resources/TestImage/output/output1.bmp"));
        var actualOutput =  Files.newInputStream(Path.of(String.format("%s/%s.bmp", TEMP_FOLDER_PATH, imageId)));
        Assertions.assertTrue(IOUtils.contentEquals(expectedOutput, actualOutput));
    }

    @Test
    void insertImageInvalidIdReturnsNotFound() throws Exception {
        MockMultipartFile image = getMockMultipartFile("src/test/resources/TestImage/input/input1.bmp");

        mockMvc.perform(MockMvcRequestBuilders.multipart(String.format("/chartas/%s/", NOT_EXISTING_ID))
                        .file(image)
                        .param("width", "20")
                        .param("height", "40")
                        .param("x", "0")
                        .param("y", "0"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void insertImageWithIncorrectDimensionReturnsBadRequest() throws Exception {
        String imageId = createImage(40, 40);
        MockMultipartFile image = getMockMultipartFile("src/test/resources/TestImage/input/input1.bmp");

        mockMvc.perform(MockMvcRequestBuilders.multipart(String.format("/chartas/%s/", imageId))
                        .file(image)
                        .param("width", "50")
                        .param("height", "50")
                        .param("x", "0")
                        .param("y", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getImageFullNoOffsetReturnsOk() throws Exception {
        String imageId = createImage(100, 50);
        this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, 100, 50, 0 ,0)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getImageOverstepBorderReturnsOk() throws Exception {
        ///Fill image
        String imageId = createImage(20, 40);
        MockMultipartFile image = getMockMultipartFile("src/test/resources/TestImage/input/input1.bmp");
        mockMvc.perform(MockMvcRequestBuilders.multipart(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, 20, 40, 0, 0))
                        .file(image)).andDo(print());

        //Get lower right corner
        var content = this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, 20, 40, 10, 20)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        var expectedOutput = Files.newInputStream(Path.of("src/test/resources/TestImage/output/overlapOutput.bmp"));
        var actualOutput =  new ByteArrayInputStream(content);
        Assertions.assertTrue(IOUtils.contentEquals(expectedOutput, actualOutput));
    }

    @Test
    void getImageLargerSizeReturnsOk() throws Exception {
        String imageId = createImage(20, 40);

        this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, 2 * 20, 2 * 40, 0, 0)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getImageInvalidIdReturnNotFound() throws Exception {
        this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", NOT_EXISTING_ID, 50, 50, 0, 0)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getImageTooBigReturnBadRequest() throws Exception {
        String imageId = createImage(100, 100);

        this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, IMAGE_SEGMENT_WIDTH_LIMIT + 1, IMAGE_SEGMENT_HEIGHT_LIMIT + 1, 0 ,0)))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }
    @Test
    void getImageZeroSizeReturnsBadRequest() throws Exception {
        String imageId = createImage(100, 100);

        this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, 0, 100, 0, 0)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getImageNegativeSizeReturnsBadRequest() throws Exception {
        String imageId = createImage(100, 100);

        this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, -100, 100, 0, 0)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    @Test
    void getImageNegativeOffsetReturnsBadRequest() throws Exception {
        String imageId = createImage(100, 100);

        this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, 50, 50, -10, -10)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getImageOffsetOutOfImageReturnsBadRequest() throws Exception {
        int imageWidth = 100;
        int imageHeight = 50;
        String imageId = createImage(imageWidth, imageHeight);

        int widthOffset = imageWidth + 1;
        int heightOffset = imageHeight + 1;
        this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, imageWidth, imageHeight, widthOffset ,heightOffset)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void createImageMinimalSizeReturnsCreated() throws Exception {
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", 1, 1)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void createImageMaxSizeReturnsCreated() throws Exception {
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", FULL_IMAGE_WIDTH_LIMIT, FULL_IMAGE_HEIGHT_LIMIT)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void createImageWithInvalidParamsReturnsBadRequest() throws Exception {
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", 0, 50)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", 100, 0)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", -20, 50)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void createImageWithTooBigSizeReturnsBadRequest() throws Exception {
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", FULL_IMAGE_WIDTH_LIMIT + 1, 50)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", 100, FULL_IMAGE_HEIGHT_LIMIT + 1)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", FULL_IMAGE_WIDTH_LIMIT + 1, FULL_IMAGE_HEIGHT_LIMIT + 1)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteImageNotExistingReturnsOkEmptyBody() throws Exception {
        this.mockMvc.perform(delete(String.format("/chartas/%s", NOT_EXISTING_ID)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void deleteImageReturnsOkEmptyBody() throws Exception {
        String imageId = createImage(100, 100);

        this.mockMvc.perform(delete(String.format("/chartas/%s", imageId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    private String createImage(int width, int height) throws Exception {
        return this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", width, height)))
                .andDo(print()).
                andReturn().getResponse().getContentAsString();

    }

    private MockMultipartFile getMockMultipartFile(String path) throws IOException {
        var imageContent = Files.readAllBytes(Path.of(path));
        return new MockMultipartFile("image", "imageName", "image/bmp", imageContent);
    }
}
