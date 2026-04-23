package com.nelson.riskmanager.controller;

import com.nelson.riskmanager.service.DocumentIngestionService;

import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@RestController
public class RiskManagerController {

    private final DocumentIngestionService documentIngestionService;


    public RiskManagerController(DocumentIngestionService documentIngestionService) {
        this.documentIngestionService = documentIngestionService;
    }


    @PostMapping("/ingest")
    public String ingest(@RequestParam String path) throws IOException {
        Path target = Path.of(path);
        if (Files.isDirectory(target)) {
            documentIngestionService.ingestDirectory(target);
            return "Ingested all files in directory: " + path;
        } else {
            documentIngestionService.ingestFile(target);
            return "Ingested file: " + path;
        }
    }

    @GetMapping("/search")
    public List<String> search(@RequestParam String query,
                               @RequestParam(defaultValue = "5") int topK) {
        return documentIngestionService.search(query, topK)
                .stream()
                .map(Document::getText)
                .distinct()
                .toList();
    }

    @PostMapping("/upload")
    @CrossOrigin
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        Path uploadDir = Path.of("uploads").toAbsolutePath();
        Files.createDirectories(uploadDir);
        Path dest = uploadDir.resolve(Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(dest.toFile());
        return dest.toString();
    }

}
