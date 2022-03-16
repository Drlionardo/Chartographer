package ru.kontur.intern;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

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

@SpringBootTest(args = "src/test/resources/test")
@AutoConfigureMockMvc
class IntegrationalTest {
    @Autowired
    private MockMvc mockMvc;

    private static final String TEMP_FOLDER_PATH = "src/test/resources/test";

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
    void insertImageReturnsOk() throws Exception {
        String imageId = createImage(40, 40);
        var content = Files.readAllBytes(Path.of("src/test/resources/TestImage/input/input1.bmp"));

        mockMvc.perform(post(String.format("/chartas/%s/", imageId))
                        .content(content)
                        .contentType("image/bmp")
                        .param("width", "20")
                        .param("height", "40")
                        .param("x", "0")
                        .param("y", "0"))
                .andDo(print())
                .andExpect(status().isOk());

        var expectedOutput = Files.newInputStream(Path.of("src/test/resources/TestImage/output/output1.bmp"));
        var actualOutput = Files.newInputStream(Path.of(String.format("%s/%s.bmp", TEMP_FOLDER_PATH, imageId)));
        Assertions.assertTrue(IOUtils.contentEquals(expectedOutput, actualOutput));
    }

    @Test
    void getImageOverstepBorderReturnsOk() throws Exception {
        ///Fill image
        String imageId = createImage(20, 40);
        var content = Files.readAllBytes(Path.of("src/test/resources/TestImage/input/input1.bmp"));
        mockMvc.perform(post(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, 20, 40, 0, 0))
                        .content(content)
                        .contentType("image/bmp"))
                .andDo(print());

        //Get lower right corner
        var responseContent = this.mockMvc.perform(get(String.format("/chartas/%s/?width=%d&height=%d&x=%d&y=%d", imageId, 20, 40, 10, 20)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        var expectedOutput = Files.newInputStream(Path.of("src/test/resources/TestImage/output/overlapOutput.bmp"));
        var actualOutput = new ByteArrayInputStream(responseContent);
        Assertions.assertTrue(IOUtils.contentEquals(expectedOutput, actualOutput));
    }

    @Test
    void createImageMinimalSizeReturnsCreated() throws Exception {
        this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", 1, 1)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void deleteImageReturnsOkEmptyBody() throws Exception {
        String imageId = createImage(100, 100);

        this.mockMvc.perform(delete(String.format("/chartas/%s/", imageId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    private String createImage(int width, int height) throws Exception {
        return this.mockMvc.perform(post(String.format("/chartas/?width=%d&height=%d", width, height)))
                .andDo(print()).
                andReturn().getResponse().getContentAsString();
    }
}
