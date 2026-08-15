package com.priye.streamvault.storage.local;

import com.priye.streamvault.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalStorageServiceImpl implements StorageService {

    @Value("${storage.location}")
    private String storageLocation;

    @Override
    public String store(MultipartFile file) throws IOException {

        Path uploadDirectory = Paths.get(storageLocation).toAbsolutePath().normalize();

        Files.createDirectories(uploadDirectory);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        String extension = "";

        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFileName.substring(dotIndex);
        }

        String storedFileName = UUID.randomUUID() + extension;

        Path targetPath = uploadDirectory.resolve(storedFileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }

    @Override
    public Resource load(String storagePath) {

        Path path = Paths.get(storagePath);

        Resource resource = new FileSystemResource(path);

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Video file not found");
        }

        return resource;
    }
}