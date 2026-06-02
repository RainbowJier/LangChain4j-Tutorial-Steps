package com.smartdoc.api;

import com.smartdoc.common.entity.AjaxResult;
import com.smartdoc.rag.DocumentIngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentIngestionService ingestionService;
    private final Path registryFile;
    private final List<String> uploadedDocuments = new CopyOnWriteArrayList<>();

    public DocumentController(
            DocumentIngestionService ingestionService,
            @Value("${rag.document-registry:data/document-registry.txt}") String registryPath) {
        this.ingestionService = ingestionService;
        this.registryFile = Path.of(registryPath);
        loadRegistry();
    }

    private void loadRegistry() {
        try {
            if (Files.exists(registryFile)) {
                List<String> lines = Files.readAllLines(registryFile);
                uploadedDocuments.addAll(lines.stream().filter(l -> !l.isBlank()).toList());
                log.info("Loaded {} documents from registry", uploadedDocuments.size());
            }
        } catch (IOException e) {
            log.warn("Failed to load document registry", e);
        }
    }

    private void saveRegistry() {
        try {
            Files.createDirectories(registryFile.getParent());
            Files.write(registryFile, uploadedDocuments, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.warn("Failed to save document registry", e);
        }
    }

    @PostMapping("/upload")
    public AjaxResult<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        Path tempDir = Path.of("data/temp");
        Path tempFile = null;
        try {
            Files.createDirectories(tempDir);
            String originalFilename = file.getOriginalFilename();
            String safeFilename = originalFilename != null ? Path.of(originalFilename).getFileName().toString() : "uploaded-file";
            tempFile = tempDir.resolve(System.currentTimeMillis() + "_" + safeFilename);

            file.transferTo(tempFile.toAbsolutePath().toFile());

            ingestionService.ingestDocument(tempFile);

            uploadedDocuments.add(originalFilename != null ? originalFilename : safeFilename);
            saveRegistry();
            log.info("Uploaded and ingested: {}", originalFilename);

            return AjaxResult.success(originalFilename);
        } catch (Exception e) {
            log.error("Failed to upload document", e);
            return AjaxResult.failed("Failed to process document. Please check the file format and try again.");
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temp file: {}", tempFile, e);
                }
            }
        }
    }

    @GetMapping
    public AjaxResult<List<String>> listDocuments() {
        return AjaxResult.success(new ArrayList<>(uploadedDocuments));
    }
}
