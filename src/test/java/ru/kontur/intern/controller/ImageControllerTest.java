package ru.kontur.intern.controller;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(args = "src/test/resources/test")
@AutoConfigureMockMvc
class ImageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    static final String TEMP_FOLDER_PATH = "src/test/resources/test";
    private final String NOT_EXISTING_ID = "notExistingId";
    private final Integer WIDTH_LIMIT = 20000;
    private final Integer HEIGHT_LIMIT = 50000;

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
    void fillImageWithIncorrectDimensionReturnsBadRequest() throws Exception {
        //Before:
        String imageId = this.mockMvc.perform(post("/chartas/?width=40&height=40"))
                .andDo(print()).
                andReturn().getResponse().getContentAsString();

        var imageContent = Files.readAllBytes(Path.of("src/test/resources/TestImage/input/input1.bmp"));
        String incorrectWidth = String.valueOf(200);
        String incorrectHeight = String.valueOf(150);
        MockMultipartFile image = new MockMultipartFile("image", "imageName", "image/bmp", imageContent);

        mockMvc.perform(MockMvcRequestBuilders.multipart(String.format("/chartas/%s/", imageId))
                        .file(image)
                        .param("width", incorrectWidth)
                        .param("height", incorrectHeight)
                        .param("x", "0")
                        .param("y", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void fillImageTest1() throws Exception {
        //Before:
        String imageId = this.mockMvc.perform(post("/chartas/?width=40&height=40"))
                .andDo(print()).
                andReturn().getResponse().getContentAsString();

        var imageContent = Files.readAllBytes(Path.of("src/test/resources/TestImage/input/input1.bmp"));
        MockMultipartFile image = new MockMultipartFile("image", "imageName", "image/bmp", imageContent);
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
    void getImagePartReturnsOk() throws Exception {
        int width = 100;
        int height = 50;
        //Before:
        String imageId = this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", width, height)))
                .andDo(print()).
                andReturn().getResponse().getContentAsString();

        //When
        //getFullImage
        int x = 0;
        int y = 0;
        this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, width, height, x ,y)))
                .andDo(print())
                .andExpect(status().isOk());

        //getFullImageWithOffset
        x = 20;
        y = 50;
        this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, width, height, x ,y)))
                .andDo(print())
                .andExpect(status().isOk());

        //getFullImageOutOfRangeOffset
        x = width + 1;
        y = height + 1;
        this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, width, height, x ,y)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getNotExistingImageReturnsNotFound() throws Exception {
        int width = 100;
        int height = 50;
        int x = 0;
        int y = 0;
        this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", NOT_EXISTING_ID, width, height, x ,y)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void createImageReturnsCreated() throws Exception {
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", 1, 1)))
                .andDo(print())
                .andExpect(status().isCreated());
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", 100, 50)))
                .andDo(print())
                .andExpect(status().isCreated());
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", WIDTH_LIMIT, HEIGHT_LIMIT)))
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
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", 20, -50)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void createImageWithTooBigSizeReturnsBadRequest() throws Exception {
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", WIDTH_LIMIT + 1, 50)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", 100, HEIGHT_LIMIT + 1)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", WIDTH_LIMIT + 1, HEIGHT_LIMIT + 1)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUnknownImageReturnsOk() throws Exception {
        this.mockMvc.perform(delete(String.format("/chartas/%s", NOT_EXISTING_ID)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void deleteImageReturnsOk() throws Exception {
        //Before:
        String imageId = this.mockMvc.perform(post("/chartas/?width=100&height=100"))
                .andDo(print()).
                andReturn().getResponse().getContentAsString();
        //When
        this.mockMvc.perform(delete(String.format("/chartas/%s", imageId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
