package com.nelson.riskmanager.service;

import org.apache.pdfbox.Loader;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter;




    public DocumentIngestionService(@Autowired(required = false) VectorStore vectorStore, TokenTextSplitter splitter) {
        this.splitter = splitter;
        this.vectorStore = vectorStore;
    }

    public void ingestFile(Path filePath) throws IOException {
        System.out.println("Loading PDF from: " + filePath.toAbsolutePath());
        System.out.println("Exists: " + Files.exists(filePath));
        if (filePath.toString().toLowerCase().endsWith(".pdf")) {
            try (PDDocument pdf = Loader.loadPDF(filePath.toFile())) {
                int totalPages = pdf.getNumberOfPages();
                PDFTextStripper stripper = new PDFTextStripper();
                for (int i = 1; i <= totalPages; i++) {
                    stripper.setStartPage(i);
                    stripper.setEndPage(i);
                    stripper.setLineSeparator("\n");
                    stripper.setAddMoreFormatting(true);
                    String pageText = stripper.getText(pdf);
                    if (!pageText.isBlank()){
                        Document document = new Document(pageText, Map.of(
                                "filename", filePath.getFileName().toString(),
                                "page", String.valueOf(i)
                        ));
                        List<Document> chunks = splitter.apply(List.of(document));
                        vectorStore.add(chunks);
                    }
                }
            }
        } else {
            String content = Files.readString(filePath);
            Document document = new Document(content, Map.of("filename", filePath.getFileName().toString()));
            List<Document> chunks = splitter.apply(List.of(document));
            vectorStore.add(chunks);
        }
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
                .similarityThreshold(0.7)
                .build());
    }
}
