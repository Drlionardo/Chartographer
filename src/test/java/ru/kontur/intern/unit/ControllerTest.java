package ru.kontur.intern.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.kontur.intern.config.ConverterConfig;
import ru.kontur.intern.config.ImageSizeConfig;
import ru.kontur.intern.controller.ImageController;
import ru.kontur.intern.service.ImageService;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ImageController.class, ConverterConfig.class})
public class ControllerTest extends AbstractTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService service;

    @ParameterizedTest()
    @CsvSource({"1,1", "50,50",
            ImageSizeConfig.IMAGE_SEGMENT_HEIGHT_LIMIT+ "," + ImageSizeConfig.IMAGE_SEGMENT_HEIGHT_LIMIT})
    void getImageOk(String width, String height) throws Exception {
        var expectedResponseImage = ImageIO.read(new File(TEST_IMAGE));
        doReturn(expectedResponseImage).when(service).getImagePart(anyString(), anyInt(), anyInt(), anyInt(), anyInt());

        var responseContent = mockMvc.perform(get(String.format("/chartas/%s/", TARGET_IMAGE_ID))
                        .param("width", width)
                        .param("height", height)
                        .param("x", "0")
                        .param("y", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        var actualResponseImage = ImageIO.read(new ByteArrayInputStream(responseContent));
        Assertions.assertTrue(compareBufferedImages(expectedResponseImage, actualResponseImage));
    }

    @ParameterizedTest()
    @CsvSource({"0,50", "50,0", "-10,50", "50,-10",
            ImageSizeConfig.FULL_IMAGE_WIDTH_LIMIT+1 + "," + ImageSizeConfig.IMAGE_SEGMENT_HEIGHT_LIMIT+1})
    void getImageSizeBadRequest(String width, String height) throws Exception {
        var responseImage = ImageIO.read(new File(TEST_IMAGE));
        doReturn(responseImage).when(service).getImagePart(anyString(), anyInt(), anyInt(), anyInt(), anyInt());

        mockMvc.perform(get(String.format("/chartas/%s/", TARGET_IMAGE_ID))
                        .param("width", width)
                        .param("height", height)
                        .param("x", "0")
                        .param("y", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest()
    @CsvSource({"-1,0", "0,-1", "-10,-10"})
    void getImageNegativeOffsetBadRequest(String x, String y) throws Exception {
        var responseImage = ImageIO.read(new File(TEST_IMAGE));
        doReturn(responseImage).when(service).getImagePart(anyString(), anyInt(), anyInt(), anyInt(), anyInt());

        mockMvc.perform(get(String.format("/chartas/%s/", TARGET_IMAGE_ID))
                        .param("width", "20")
                        .param("height", "40")
                        .param("x", x)
                        .param("y", y))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest()
    @CsvSource({"1,1", "50,50",
            ImageSizeConfig.FULL_IMAGE_WIDTH_LIMIT + "," + ImageSizeConfig.FULL_IMAGE_HEIGHT_LIMIT})
    void insertImageOk(String width, String height) throws Exception {
        var content = Files.readAllBytes(Path.of(TEST_IMAGE));

        mockMvc.perform(post(String.format("/chartas/%s/", TARGET_IMAGE_ID))
                        .content(content)
                        .contentType("image/bmp")
                        .param("width", width)
                        .param("height", height)
                        .param("x", "0")
                        .param("y", "0"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @ParameterizedTest()
    @CsvSource({"0,50", "50,0", "-10,50", "50,-10",
            ImageSizeConfig.FULL_IMAGE_WIDTH_LIMIT + 1 + "," + ImageSizeConfig.FULL_IMAGE_HEIGHT_LIMIT + 1})
    void insertImageSizeBadRequest(String width, String height) throws Exception {
        var content = Files.readAllBytes(Path.of(TEST_IMAGE));

        mockMvc.perform(post(String.format("/chartas/%s/", TARGET_IMAGE_ID))
                        .content(content)
                        .contentType("image/bmp")
                        .param("width", width)
                        .param("height", height)
                        .param("x", "0")
                        .param("y", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest()
    @CsvSource({"-1,0", "0,-1", "-10,-10"})
    void insertImageNegativeOffsetBadRequest(String x, String y) throws Exception {
        String targetId = "targetId";
        var content = Files.readAllBytes(Path.of(TEST_IMAGE));

        mockMvc.perform(post(String.format("/chartas/%s/", targetId))
                        .content(content)
                        .contentType("image/bmp")
                        .param("width", "20")
                        .param("height", "40")
                        .param("x", x)
                        .param("y", y))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest()
    @CsvSource({"1,1", "50,50",
            ImageSizeConfig.FULL_IMAGE_WIDTH_LIMIT + "," + ImageSizeConfig.FULL_IMAGE_HEIGHT_LIMIT})
    void createImageCreated(String width, String height) throws Exception {
        String createdImageId = "newId";
        doReturn(createdImageId).when(service).createImage(anyInt(), anyInt());

        this.mockMvc.perform(post("/chartas/")
                        .param("width", width)
                        .param("height", height))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string(createdImageId));
    }

    @ParameterizedTest()
    @CsvSource({"0,50", "50,0", "-10,50", "50,-10",
            ImageSizeConfig.FULL_IMAGE_WIDTH_LIMIT + 1 + "," + ImageSizeConfig.FULL_IMAGE_HEIGHT_LIMIT + 1})
    void createImageBadRequest(String width, String height) throws Exception {
        String createdImageId = "newId";
        doReturn(createdImageId).when(service).createImage(anyInt(), anyInt());

        this.mockMvc.perform(post("/chartas/")
                        .param("width", width)
                        .param("height", height))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteImageOk() throws Exception {
        doNothing().when(service).deleteImage(anyString());
        this.mockMvc.perform(delete(String.format("/chartas/%s/", TARGET_IMAGE_ID)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
