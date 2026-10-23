package com.example.aquaventure.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over where uploaded files (e.g. school verification documents) are stored.
 * {@link LocalFileStorageService} is the only implementation today; swapping to S3 later
 * means adding a new implementation and flipping {@code app.storage.provider}.
 */
public interface FileStorageService {

    StoredFile store(MultipartFile file, String category, String ownerId);

    Resource load(String storedPath);

    void delete(String storedPath);
}
