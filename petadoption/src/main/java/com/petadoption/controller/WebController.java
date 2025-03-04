package com.petadoption.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String redirectToLoginPage() {
        return "redirect:/login.html";  // Automatically redirects to login page
    }
}
