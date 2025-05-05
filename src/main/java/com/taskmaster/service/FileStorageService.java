package com.taskmaster.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;

public interface FileStorageService {

    /**
     * Stores a file.
     * @param file The file to store.
     * @param subDirectory A subdirectory within the base upload path (e.g., project ID / task ID).
     * @return The unique path/identifier where the file is stored (relative to base or cloud key).
     */
    String storeFile(MultipartFile file, String subDirectory);

    /**
     * Loads a file as a Spring Resource.
     * @param filePath The unique path/identifier returned by storeFile.
     * @return Resource representing the file.
     */
    Resource loadFileAsResource(String filePath);

    /**
     * Deletes a file.
     * @param filePath The unique path/identifier returned by storeFile.
     */
    void deleteFile(String filePath);

    /**
     * Gets the base path for file storage (relevant for local storage).
     * @return The Path object for the base directory.
     */
    Path getBasePath();
}