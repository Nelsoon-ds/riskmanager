package com.nelson.riskmanager.controller;

import com.nelson.riskmanager.model.RiskAssessment;
import com.nelson.riskmanager.service.FileStorageService;
import com.nelson.riskmanager.service.RiskManagerService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
public class ViewController {

    private final RiskManagerService riskManagerService;
    private final FileStorageService fileStorageService;

    public ViewController(RiskManagerService riskManagerService, FileStorageService fileStorageService1) {
        this.riskManagerService = riskManagerService;
        this.fileStorageService = fileStorageService1;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/home")
    public String home() {
        return "analyze";
    }

    @GetMapping("/analyze")
    public String startPage() {
        return "analyze";
    }
    @PostMapping("/analyze")
    public String analyze(@RequestParam("file") MultipartFile file, Model model) throws IOException {

        // Validate
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No image provided");
        }
        long maxBytes = 5 * 1024 * 1024; // 5 mb
        if (file.getSize() > maxBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image too large (max 5MB)");
        }
        String contentType = file.getContentType();
        List<String> allowed = List.of("image/jpeg", "image/png", "image/webp");
        if (!allowed.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported image type");
        }

        String fileName = fileStorageService.save(file);
        String imagePath = "./uploads/" + fileName;

        BufferedImage img = ImageIO.read(new File(imagePath));
        int width = img.getWidth();
        int height = img.getHeight();

        RiskAssessment riskAssessment = riskManagerService.analyzeImage(imagePath, width, height);
        model.addAttribute("assessment", riskAssessment);
        model.addAttribute("imageSrc", "/" + fileName);
        return "analyze";
    }


    @GetMapping("/admin")
    public String admin(@AuthenticationPrincipal OAuth2User principal, Model model) {
        String userName = principal.getAttribute("name");
        String userEmail = principal.getAttribute("email");
        model.addAttribute("userName", userName);
        model.addAttribute("userEmail", userEmail);
        return "admin";
    }


}
