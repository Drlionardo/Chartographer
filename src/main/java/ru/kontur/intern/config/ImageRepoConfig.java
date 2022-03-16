package ru.kontur.intern.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.kontur.intern.repo.ImageRepo;

@Configuration
public class ImageRepoConfig {
    private ApplicationArguments args;
    @Value("${server.tomcat.threads.max:200}")
    public Integer stripedSize;
    @Value("${storage.path}")
    public String storagePath;

    public ImageRepoConfig(ApplicationArguments args) {
        this.args = args;
    }

    @Bean
    public ImageRepo createImageRepo() {
        //Try to read path from args
        if(!args.getNonOptionArgs().isEmpty()) {
            storagePath = args.getNonOptionArgs().get(0);
        }
        return new ImageRepo(storagePath, stripedSize);
    }
}
