package com.nelson.riskmanager.controller;

import com.nelson.riskmanager.model.RiskAssessment;
import com.nelson.riskmanager.service.FileStorageService;
import com.nelson.riskmanager.service.RiskManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Controller
public class ViewController {

    private final RiskManagerService riskManagerService;
    private final FileStorageService fileStorageService;

    public ViewController(RiskManagerService riskManagerService, FileStorageService fileStorageService1) {
        this.riskManagerService = riskManagerService;
        this.fileStorageService = fileStorageService1;
    }

    @GetMapping("/home")
    public String home() {
        return "analyze";
    }
    @PostMapping("/analyze")
    public String analyze(@RequestParam("file") MultipartFile file, Model model) throws IOException {
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
    public String admin() {
        return "admin";
    }


}
