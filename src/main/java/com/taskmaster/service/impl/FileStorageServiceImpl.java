package com.taskmaster.service.impl;

import com.taskmaster.exception.BadRequestException;
import com.taskmaster.exception.ResourceNotFoundException; // Or a specific FileStorageException
import com.taskmaster.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path fileStorageLocation;

    @PostConstruct // Called after dependency injection is done
    public void init() {
        try {
            fileStorageLocation = Paths.get(this.uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
            logger.info("Initialized file storage directory at: {}", fileStorageLocation);
        } catch (Exception ex) {
            logger.error("Could not create the directory where the uploaded files will be stored.", ex);
            // Depending on requirements, you might want to throw an exception to prevent app startup
            throw new RuntimeException("Could not initialize storage!", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty.");
        }
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check for invalid characters
            if (originalFileName.contains("..")) {
                throw new BadRequestException("Filename contains invalid path sequence " + originalFileName);
            }

            // Create a unique filename to avoid collisions
            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0) {
                fileExtension = originalFileName.substring(dotIndex);
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Resolve the target location including subdirectory
            Path targetDir = this.fileStorageLocation.resolve(subDirectory).normalize();
            if (!targetDir.startsWith(this.fileStorageLocation)) {
                throw new BadRequestException("Cannot store file outside configured directory.");
            }
            Files.createDirectories(targetDir); // Ensure subdirectory exists

            Path targetLocation = targetDir.resolve(uniqueFileName);

            // Copy file to the target location (Replacing existing file with the same name if any)
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Stored file '{}' to '{}'", originalFileName, targetLocation);

            // Return the relative path to be stored in the database
            return Paths.get(subDirectory, uniqueFileName).toString().replace("\\", "/"); // Ensure consistent path separators

        } catch (IOException ex) {
            logger.error("Could not store file {} under subdirectory {}. Please try again!", originalFileName, subDirectory, ex);
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        try {
            Path resolvedFilePath = this.fileStorageLocation.resolve(filePath).normalize();
            if (!resolvedFilePath.startsWith(this.fileStorageLocation)) {
                throw new BadRequestException("Cannot access file outside configured directory.");
            }

            Resource resource = new UrlResource(resolvedFilePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                logger.warn("File not found or not readable: {}", filePath);
                throw new ResourceNotFoundException("File not found " + filePath);
            }
        } catch (MalformedURLException ex) {
            logger.error("Error forming URL for file: {}", filePath, ex);
            throw new ResourceNotFoundException("File not found " + filePath, ex);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Path resolvedFilePath = this.fileStorageLocation.resolve(filePath).normalize();
            if (!resolvedFilePath.startsWith(this.fileStorageLocation)) {
                logger.warn("Attempt to delete file outside configured directory: {}", filePath);
                throw new BadRequestException("Cannot delete file outside configured directory.");
            }

            if (Files.exists(resolvedFilePath)) {
                Files.delete(resolvedFilePath);
                logger.info("Deleted file: {}", filePath);
            } else {
                logger.warn("Attempted to delete non-existent file: {}", filePath);
            }
        } catch (NoSuchFileException ex) {
            logger.warn("Attempted to delete non-existent file: {}", filePath);
            // Optionally ignore or throw specific exception
        } catch (IOException ex) {
            logger.error("Could not delete file: {}", filePath, ex);
            throw new RuntimeException("Could not delete file " + filePath + ". Please try again!", ex);
        }
    }

    @Override
    public Path getBasePath() {
        return fileStorageLocation;
    }
}