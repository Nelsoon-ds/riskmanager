package com.nelson.riskmanager.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileStorageService {

    private final Path uploadDir = Path.of("./uploads");

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadDir);
    }

    public Path save(MultipartFile file) throws IOException {
        Path dest = uploadDir.resolve(System.currentTimeMillis() + "_" + file.getOriginalFilename());
        Files.copy(file.getInputStream(), dest);
        return dest;
    }
}