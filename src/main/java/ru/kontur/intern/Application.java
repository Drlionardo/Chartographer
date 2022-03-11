package ru.kontur.intern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.kontur.intern.config.ImageRepoConfig;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		String defaultPath = "/resources";
		ImageRepoConfig.storagePath = args.length > 0 ? args[0] : defaultPath;

		SpringApplication.run(Application.class, args);
	}
}
