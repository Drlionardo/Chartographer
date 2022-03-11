package ru.kontur.intern.repo;

import com.google.common.util.concurrent.Striped;
import lombok.extern.log4j.Log4j2;
import ru.kontur.intern.exception.ImageNotFoundException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;

@Log4j2
public class ImageRepo {
    private final String STORAGE_PATH;
    private Striped<ReadWriteLock> striped;

    public ImageRepo(String storagePath, Integer stripedSize) {
        this.STORAGE_PATH = storagePath;
        this.striped = Striped.lazyWeakReadWriteLock(stripedSize);

        setup();
    }

    private void setup() {
        if (!Files.exists(Path.of(STORAGE_PATH))) {
            try {
                Files.createDirectory(Path.of(STORAGE_PATH));
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    public String saveImage(BufferedImage image) {
        String id = UUID.randomUUID().toString();
        var lock = striped.get(id).writeLock();
        try {
            lock.lock();
            ImageIO.write(image, "bmp", new File(STORAGE_PATH + "/" + id + ".bmp"));
        } catch (Exception e) {
            log.error(e.getMessage());
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
            throw new ImageNotFoundException(imagePath);
        } finally {
            lock.unlock();
        }
    }

    public void updateImage(String id, BufferedImage target) {
        String imagePath = getImagePathById(id);
        var lock = striped.get(id).writeLock();
        try {
            lock.lock();
            ImageIO.write(target, "bmp", new File(imagePath));
        } catch (Exception e) {
            log.error(e.getMessage());
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
            log.error(e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private String getImagePathById(String id) {
        return String.format("%s/%s.bmp", STORAGE_PATH, id);
    }
}
