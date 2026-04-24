package com.nelson.riskmanager.controller;

import com.nelson.riskmanager.model.RiskAssessment;
import com.nelson.riskmanager.model.User;
import com.nelson.riskmanager.service.FileStorageService;
import com.nelson.riskmanager.service.RiskManagerService;
import com.nelson.riskmanager.service.UserLoginService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Controller
public class ViewController {

    private final RiskManagerService riskManagerService;
    private final FileStorageService fileStorageService;
    private final UserLoginService userLoginService;

    public ViewController(RiskManagerService riskManagerService, FileStorageService fileStorageService1, UserLoginService userLoginService) {
        this.riskManagerService = riskManagerService;
        this.fileStorageService = fileStorageService1;
        this.userLoginService = userLoginService;
    }

    @GetMapping("/")
    public String startPage() {
        return "login";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {
        addUserToModel(principal, model, "analyze");
        return "analyze";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/analyze")
    public String getAnalyze() {
        return "analyze";
    }

    @PostMapping("/analyze")
    public String analyze(@RequestParam("file") MultipartFile file, Model model,
                           Authentication authentication ) throws IOException {

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
        Path savedFile = fileStorageService.save(file);
        int userId = userLoginService.saveOrGetUser((OAuth2AuthenticationToken) authentication).getId();

        RiskAssessment riskAssessment = riskManagerService.analyzeImage(savedFile, userId);
        model.addAttribute("assessment", riskAssessment);
        model.addAttribute("imageSrc", "/" + savedFile.getFileName());
        return "analyze";
    }




    private void addUserToModel(OAuth2User principal, Model model, String currentPage) {
        if (principal != null) {
            model.addAttribute("userName", principal.getAttribute("name"));
            model.addAttribute("userEmail", principal.getAttribute("email"));
        }
        model.addAttribute("currentPage", currentPage);
    }

    @GetMapping("/admin")
    public String admin(@AuthenticationPrincipal OAuth2User principal, Model model) {
        addUserToModel(principal, model, "admin");
        return "admin";
    }




}
