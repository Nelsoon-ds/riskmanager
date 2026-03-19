package com.nelson.riskmanager.controller;

import com.nelson.riskmanager.service.RiskManagerService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@RestController
public class RiskManagerController {

    private final RiskManagerService riskManagerService;

    public RiskManagerController(RiskManagerService riskManagerService) {
        this.riskManagerService = riskManagerService;
    }



    @PostMapping("/analyze")
    public String analyze(@RequestParam String imagePath) throws IOException {
        return riskManagerService.callApi("Analyze this image for safety and operational risks. For each risk identified, provide:\n" +
                "1. A severity level (Primary, Secondary, Tertiary)\n" +
                "2. A heading in the format: ## Primary Risk: [Name]\n" +
                "3. A line: **Coordinates: [x_min, y_min, x_max, y_max]** with values normalized 0-1 relative to image dimensions, where [0,0] is the top-left corner and [1,1] is the bottom-right corner\n" +
                "4. A brief description of the risk and recommended action\n" +
                "\n" +
                "Be as precise as possible with the bounding box coordinates.", imagePath);
    }

    @PostMapping("/upload")
    @CrossOrigin
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        Path uploadDir = Path.of("uploads").toAbsolutePath();
        Files.createDirectories(uploadDir);
        Path dest = uploadDir.resolve(Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(dest.toFile());
        return dest.toString();  // e.g. "uploads/risk2.jpg"
    }

}
