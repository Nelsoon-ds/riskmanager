package com.nelson.riskmanager.service;

import com.nelson.riskmanager.model.RiskAssessment;
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

    @Value("classpath:prompts/augmented-analysis.st")
    private Resource augmentedPromptResource;
    @Value("classpath:prompts/initial-analysis.st")
    private Resource initialPromptResource;

RiskManagerService(AnthropicChatModel chatModel, DocumentIngestionService service) {
    this.chatModel = chatModel;
    this.service = service;
}


public Media parseImg(String imgPath) throws IOException {
    byte[] imageBytes = Files.readAllBytes(Path.of(imgPath));
    String mimeType = imgPath.endsWith(".png") ? "image/png" : "image/jpeg";
    var imageResource = new ByteArrayResource(imageBytes) {
        @Override
        public String getFilename() {
            return Path.of(imgPath).getFileName().toString();
        }
    };

    return new Media(
            MimeTypeUtils.parseMimeType(mimeType),
            imageResource
    );
}

public RiskAssessment analyzeImage(String imagePath) throws IOException {
    var imageMedia = parseImg(imagePath);
    // Step 1: Initial vision analysis — free text is fine here,
    // we just need enough detail to drive vector search
    PromptTemplate initialTemplate = new PromptTemplate(initialPromptResource);
    String initialPrompt = initialTemplate.render();

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
    String rawText = augmentedResponse.getResult().getOutput().getText();
    rawText = rawText.replaceAll("(?s)^```json\\s*", "").replaceAll("(?s)\\s*```$", "");
    return converter.convert(rawText);
}
}

