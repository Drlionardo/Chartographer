package ru.kontur.intern.repo;

import com.google.common.util.concurrent.Striped;
import lombok.extern.log4j.Log4j2;
import ru.kontur.intern.exception.ImageNotFoundException;
import ru.kontur.intern.exception.NoAppropriateWriterException;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 *
 * This class implements thread-safe CRUD operations with BufferedImages using local storage.
 * It uses Google Guava Striped to dynamically manage read/write locks
 */
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

    /**
     * Reads image from disk
     *
     * @param id image id
     */
    public BufferedImage readImage(String id) {
        String imagePath = getImagePathById(id);
        var lock = striped.get(id).readLock();
        try {
            acquireLock(lock);
            return ImageIO.read(new File(imagePath));
        } catch (Exception e) {
            throw new ImageNotFoundException(imagePath);
        } finally {
            releaseLock(lock);
        }
    }

    /**
     * Deletes image from disk.
     *
     * @param id image id
     */
    public void deleteImage(String id) {
        Path imagePath = Path.of(getImagePathById(id));
        var lock = striped.get(id).writeLock();
        try {
            acquireLock(lock);
            if (Files.exists(imagePath)) {
                Files.delete(imagePath);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            releaseLock(lock);
        }
    }

    /**
     * Saves image on disk.
     *
     * @param image image to save
     * @return uniq created image identifier
     * @throws NoAppropriateWriterException if image format does not match bmp RGB
     */
    public String saveImage(BufferedImage image) {
        String id = UUID.randomUUID().toString();
        var lock = striped.get(id).writeLock();
        try {
            acquireLock(lock);
            saveImageToDisk(image, getImagePathById(id));
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            releaseLock(lock);
        }
        return id;
    }

    /**
     * Saves image with specified id and overrides previous image.
     *
     * @param id    image id
     * @param image image to save
     * @throws NoAppropriateWriterException if image format does not match bmp RGB
     */
    public void updateImage(String id, BufferedImage image) {
        String imagePath = getImagePathById(id);
        var lock = striped.get(id).writeLock();
        try {
            acquireLock(lock);
            saveImageToDisk(image, imagePath);
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            releaseLock(lock);
        }
    }

    private void saveImageToDisk(BufferedImage target, String imagePath) throws IOException {
        var isSuccess = ImageIO.write(target, "bmp", new File(imagePath));
        if (!isSuccess) {
            throw new NoAppropriateWriterException(target, "bmp");
        }
    }

    private String getImagePathById(String id) {
        return String.format("%s/%s.bmp", STORAGE_PATH, id);
    }

    private void releaseLock(Lock lock) {
        log.debug(Thread.currentThread() + " releases lock " + lock);
        lock.unlock();
    }

    private void acquireLock(Lock lock) {
        log.debug(Thread.currentThread() + " acquires lock " + lock);
        lock.lock();
    }
}
