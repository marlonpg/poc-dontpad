package com.gambasoftware.poc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;

@Controller
public class PageController {
    private final Logger logger = LoggerFactory.getLogger(PageController.class);

    @GetMapping("/note/{path}")
    public ResponseEntity<String> servePage(@PathVariable String path) throws IOException {
        logger.info("page {}", path);
        Resource resource = new ClassPathResource("static/editor.html");
        String content = Files.readString(resource.getFile().toPath());
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(content);
    }
}