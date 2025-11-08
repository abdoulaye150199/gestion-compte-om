package com.example.gesioncompteom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {

    @GetMapping("/swagger-ui")
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui/index.html";
    }

    @GetMapping("/swagger-ui.html")
    public String redirectToSwaggerUiHtml() {
        return "redirect:/swagger-ui/index.html";
    }
}

