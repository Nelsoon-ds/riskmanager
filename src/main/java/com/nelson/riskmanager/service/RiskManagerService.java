package com.nelson.riskmanager.service;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class RiskManagerService {

    private final AnthropicChatModel chatModel;


RiskManagerService(AnthropicChatModel chatModel) {
    this.chatModel = chatModel;
}
    public String callApi(String message, String imgPath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Path.of(imgPath));

        String mimeType = imgPath.endsWith(".png") ? "image/png" : "image/jpeg";

        var imageResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return Path.of(imgPath).getFileName().toString();
            }
        };

        Media imageMedia = new Media(
                MimeTypeUtils.parseMimeType(mimeType),
                imageResource
        );

        UserMessage userMessage = UserMessage.builder()
                .text(message)
                .media(imageMedia)
                .build();

        ChatResponse response = chatModel.call(new Prompt(userMessage));
        return response.getResult().getOutput().getText();
    }




}