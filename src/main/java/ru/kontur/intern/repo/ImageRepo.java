package ru.kontur.intern.repo;

import com.google.common.util.concurrent.Striped;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;

@Repository
public class ImageRepo {
    @Value("#{springApplicationArguments.nonOptionArgs.get(0)}")
    private String folderPath;
    private Striped<ReadWriteLock> striped = Striped.lazyWeakReadWriteLock(200);

    @PostConstruct
    private void setUp() {
        if (!Files.exists(Path.of(folderPath))) {
            try {
                Files.createDirectory(Path.of(folderPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String saveImage(BufferedImage image) {
        String id = UUID.randomUUID().toString();
        var lock = striped.get(id).writeLock();
        try {
            lock.lock();
            ImageIO.write(image, "bmp", new File(folderPath + "/" + id + ".bmp"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return id;
    }

    public BufferedImage readImage(String id) {
        String imagePath = getImagePathById(id);
        var lock = striped.get(id).readLock();
        try {
            lock.lock();
            return ImageIO.read(new File(imagePath));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void updateImage(String id, BufferedImage target) {
        String imagePath = getImagePathById(id);
        var lock = striped.get(id).writeLock();
        try {
            lock.lock();
            ImageIO.write(target, "bmp", new File(imagePath));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void deleteImage(String id) {
        Path imagePath = Path.of(getImagePathById(id));
        var lock = striped.get(id).writeLock();
        try {
            lock.lock();
            if (Files.exists(imagePath)) {
                Files.delete(imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private String getImagePathById(String id) {
        return String.format("%s/%s.bmp", folderPath, id);
    }
}
