package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final S3Service s3Service;

    public UploadController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();

        try {
            String fileDownloadUri = s3Service.uploadFile(file);
            String actualFileName = fileDownloadUri.substring(fileDownloadUri.lastIndexOf("/") + 1);

            Map<String, String> response = new HashMap<>();
            response.put("url", "/api/upload/files/" + actualFileName);

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Could not upload file " + fileName + " to S3. Please try again! Error: " + ex.getMessage());
        }
    }

    @GetMapping("/files/{fileName}")
    public ResponseEntity<?> getFile(@PathVariable String fileName) {
        try {
            var stream = s3Service.getFile(fileName);
            byte[] bytes = stream.readAllBytes();

            String contentType = "image/jpeg";
            if (fileName.toLowerCase().endsWith(".png")) contentType = "image/png";
            else if (fileName.toLowerCase().endsWith(".gif")) contentType = "image/gif";
            else if (fileName.toLowerCase().endsWith(".svg")) contentType = "image/svg+xml";
            else if (fileName.toLowerCase().endsWith(".webp")) contentType = "image/webp";

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, contentType)
                    .body(bytes);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

}
