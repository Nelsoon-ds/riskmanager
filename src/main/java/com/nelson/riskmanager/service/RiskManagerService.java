package com.nelson.riskmanager.service;

import com.nelson.riskmanager.model.RiskAssessment;
import com.nelson.riskmanager.repository.RiskManagerRepository;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RiskManagerService {

    private final AnthropicChatModel chatModel;
    private final DocumentIngestionService service;
    private final RiskManagerRepository riskManagerRepository;

    @Value("classpath:prompts/augmented-analysis.st")
    private Resource augmentedPromptResource;
    @Value("classpath:prompts/initial-analysis.st")
    private Resource initialPromptResource;

RiskManagerService(AnthropicChatModel chatModel, DocumentIngestionService service, RiskManagerRepository riskManagerRepository) {
    this.chatModel = chatModel;
    this.service = service;
    this.riskManagerRepository = riskManagerRepository;
}


public Media parseImg(Path imgPath) throws IOException {
    byte[] imageBytes = Files.readAllBytes(imgPath);
    String fileName = imgPath.getFileName().toString();
    String mimeType = fileName.endsWith(".png") ? "image/png" : "image/jpeg";
    var imageResource = new ByteArrayResource(imageBytes) {
        @Override
        public String getFilename() {
            return fileName;
        }
    };

    return new Media(
            MimeTypeUtils.parseMimeType(mimeType),
            imageResource
    );
}

public RiskAssessment analyzeImage(Path imagePath, int userId) throws IOException {
    BufferedImage img = ImageIO.read(imagePath.toFile());
    int width = img.getWidth();
    int height = img.getHeight();

    var imageMedia = parseImg(imagePath);
    // Step 1: Initial vision analysis
    PromptTemplate initialTemplate = new PromptTemplate(initialPromptResource);
    String initialPrompt = initialTemplate.render(Map.of(
            "width", String.valueOf(width),
            "height", String.valueOf(height)
    ));

    UserMessage initialMessage = UserMessage.builder()
            .text(initialPrompt)
            .media(imageMedia)
            .build();



    ChatResponse initialResponse = chatModel.call(new Prompt(initialMessage));
    String analysisText = Objects.requireNonNull(initialResponse.getResult()).getOutput().getText();

    // Step 2: Vector search using the initial analysis
    List<Document> relevantDocs = service.search(analysisText, 5);
    String context = relevantDocs.stream()
            .map(Document::getText)
            .distinct()
            .collect(Collectors.joining("\n---\n"));

    // Step 3: Augmented call with structured output
    var converter = new BeanOutputConverter<>(RiskAssessment.class);

    PromptTemplate promptTemplate = new PromptTemplate(augmentedPromptResource);

    String promptText = promptTemplate.render(Map.of(
            "analysisText", analysisText,
            "context", context,
            "format", converter.getFormat()
    ));

    UserMessage augmentedMessage = UserMessage.builder()
            .text(promptText)
            .media(imageMedia)
            .build();

    ChatResponse augmentedResponse = chatModel.call(
            new Prompt(List.of(
                    new SystemMessage("You are a workplace safety expert. Respond ONLY with valid JSON. No preamble, no explanation, no markdown fences."),
                    augmentedMessage
            ))
    );
    
    System.out.println("RAW RESPONSE: " + augmentedResponse.getResult());
    String rawText = Objects.requireNonNull(augmentedResponse.getResult()).getOutput().getText();
    rawText = rawText.replaceAll("(?s)^```json\\s*", "").replaceAll("(?s)\\s*```$", "");
    RiskAssessment riskAssessment = converter.convert(rawText);
    riskManagerRepository.save(riskAssessment, userId);


    return riskAssessment;
}
}

