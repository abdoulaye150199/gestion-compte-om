package com.example.gesioncompteom.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webjars.WebJarAssetLocator;

import java.io.IOException;

@RestController
@RequestMapping("/java/abdoulaye.diallo/docs")
public class SwaggerAssetsController {

    private final ResourceLoader resourceLoader;
    private final WebJarAssetLocator locator = new WebJarAssetLocator();

    public SwaggerAssetsController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping({"/","/index.html"})
    public ResponseEntity<Resource> index() throws IOException {
        // Try the webjars index first
        try {
            String full = locator.getFullPath("index.html", "swagger-ui");
            Resource idx = resourceLoader.getResource("classpath:/" + full);
            if (idx.exists()) {
                return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE).body(idx);
            }
        } catch (Exception ignored) {
        }
        // Fallback to springdoc default
        Resource idx = resourceLoader.getResource("classpath:/META-INF/resources/swagger-ui/index.html");
        if (idx.exists()) {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE).body(idx);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{asset:.+}")
    public ResponseEntity<Resource> asset(@PathVariable String asset) throws IOException {
        // Try to locate the asset inside webjars (no hardcoded version)
        try {
            String fullPath = locator.getFullPath(asset);
            Resource res = resourceLoader.getResource("classpath:/" + fullPath);
            if (res.exists()) {
                MediaType contentType = detectContentType(asset);
                return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, contentType.toString()).body(res);
            }
        } catch (Exception e) {
            // ignore and try fallback
        }
        // fallback to generic swagger-ui path
        Resource res = resourceLoader.getResource("classpath:/META-INF/resources/swagger-ui/" + asset);
        if (res.exists()) {
            MediaType contentType = detectContentType(asset);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, contentType.toString()).body(res);
        }
        return ResponseEntity.notFound().build();
    }

    private MediaType detectContentType(String asset) {
        if (asset.endsWith(".js")) return MediaType.valueOf("application/javascript");
        if (asset.endsWith(".css")) return MediaType.valueOf("text/css");
        if (asset.endsWith(".png")) return MediaType.IMAGE_PNG;
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
