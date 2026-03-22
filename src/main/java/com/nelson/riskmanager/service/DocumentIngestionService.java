package com.nelson.riskmanager.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingestFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        Document document = new Document(content, Map.of("filename", filePath.getFileName().toString()));
        vectorStore.add(List.of(document));
    }

    public void ingestDirectory(Path directory) throws IOException {
        try (var files = Files.walk(directory)) {
            files.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            ingestFile(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to ingest file: " + path, e);
                        }
                    });
        }
    }

    public List<Document> search(String query, int topK) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build());
    }
}
