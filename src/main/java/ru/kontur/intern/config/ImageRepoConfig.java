package ru.kontur.intern.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.kontur.intern.repo.ImageRepo;

@Configuration
public class ImageRepoConfig {
    @Value("${server.tomcat.threads.max}")
    public Integer stripedSize;
    public static String storagePath;

    @Bean
    public ImageRepo createImageRepo() {
        return new ImageRepo(storagePath, stripedSize);
    }
}
