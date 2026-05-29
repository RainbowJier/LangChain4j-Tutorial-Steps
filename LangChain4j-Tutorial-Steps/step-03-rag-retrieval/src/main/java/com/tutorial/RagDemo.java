package com.tutorial;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Step 03: RAG Retrieval-Augmented Generation — Let LLM answer questions based on your own documents.
 * <p>
 * Complete workflow (9 stages):
 * 1. EmbeddingModel    — Text to vector model (local AllMiniLmL6-v2, 384 dimensions, no API key needed)
 * 2. EmbeddingStore    — Vector storage (in-memory, sufficient for development)
 * 3. DocumentParser    — Document parser (supports TXT/PDF/DOCX)
 * 4. DocumentSplitter  — Text chunker (500 chars per chunk, 100 chars overlap)
 * 5. EmbeddingStoreIngestor — Chunk + Vectorize + Store (all in one step)
 * 6. ContentRetriever  — Retriever (finds most relevant 5 segments from vector store)
 * 7. ChatModel         — LLM model (used to generate final answers)
 * 8. AiServices        — Assembly (inject retriever into AI service)
 * 9. Interactive conversation
 */
public class RagDemo {

    @SystemMessage("""
            You are 「Smart Document Assistant」, helping the team query the development handbook.
            Answer requirements:
            - Answer accurately based on reference materials, cite clause numbers when quoting
            - If reference materials don't contain relevant information, honestly inform "Not found in handbook"
            - Answers should be concise and clear
            """)
    interface RagAssistant {
        String chat(String message);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Step 03: RAG Retrieval Enhancement Generation ===\n");

        // ===== Stage 1: Create Embedding model (local, no API key needed) =====
        System.out.println("[1/9] Creating Embedding model...");
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        System.out.println("      AllMiniLmL6-v2 ready (384 dimensions, local ONNX inference)");

        // ===== Stage 2: Create vector store =====
        System.out.println("[2/9] Creating vector store...");
        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        System.out.println("      InMemoryEmbeddingStore ready (data lost after restart)");

        // ===== Stage 3: Parse documents =====
        System.out.println("[3/9] Loading documents...");
        DocumentParser parser = new TextDocumentParser();
        var kbUrl = RagDemo.class.getClassLoader().getResource("knowledge-base.txt");
        if (kbUrl == null) {
            throw new IllegalStateException("knowledge-base.txt not found in classpath");
        }
        Path docPath = Path.of(kbUrl.toURI());
        Document doc = FileSystemDocumentLoader.loadDocument(docPath, parser);
        System.out.println("      Document loaded: " + docPath.getFileName());

        // ===== Stage 4: Configure chunking strategy =====
        // 500 chars per chunk, 100 chars overlap — ensures no information lost across chunks
        System.out.println("[4/9] Configuring chunking strategy...");
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);
        System.out.println("      Recursive chunking: 500 chars/chunk, 100 chars overlap");

        // ===== Stage 5: Ingest (chunk + vectorize + store) =====
        System.out.println("[5/9] Document ingestion (chunk → vectorize → store)...");
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(splitter)
                .build();
        ingestor.ingest(doc);
        System.out.println("      Ingestion complete!");

        // ===== Stage 6: Create retriever =====
        // maxResults=5 means each retrieval returns the most relevant 5 text segments
        System.out.println("[6/9] Creating retriever...");
        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .build();
        System.out.println("      Retriever ready (maxResults=5)");

        // ===== Stage 7: Create ChatModel =====
        System.out.println("[7/9] Creating ChatModel...");
        ChatModel chatModel = createChatModel();
        System.out.println("      ChatModel ready");

        // ===== Stage 8: Assemble AI service (core!) =====
        // Key difference: compared to Step 01, has .contentRetriever(retriever)
        System.out.println("[8/9] Assembling AI service...");
        RagAssistant assistant = AiServices.builder(RagAssistant.class)
                .chatModel(chatModel)
                .contentRetriever(retriever)   // ← This line gives AI access to knowledge base
                .build();
        System.out.println("      AI service assembled!\n");

        // ===== Stage 9: Interactive conversation =====
        System.out.println("[9/9] Starting conversation!\n");
        System.out.println("=== RAG Conversation ===");
        System.out.println("Try these questions:");
        System.out.println("  - How should class names be named in coding conventions?");
        System.out.println("  - What are the default values for API pagination parameters?");
        System.out.println("  - What's the process for deploying to production?");
        System.out.println("  - How many approvals needed for code review?");
        System.out.println("Enter exit to quit\n");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String question = scanner.nextLine().trim();
            if (question.isEmpty()) continue;
            if ("exit".equalsIgnoreCase(question)) {
                System.out.println("Goodbye!");
                break;
            }
            try {
                // On each call, retriever automatically:
                // 1. Converts question to vector
                // 2. Searches vector store for most similar 5 segments
                // 3. Combines these segments as context into prompt
                // 4. LLM generates answer based on context
                String answer = assistant.chat(question);
                System.out.println("Assistant: " + answer);
                System.out.println();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static ChatModel createChatModel() {
        Map<String, Object> config = loadConfig();
        @SuppressWarnings("unchecked")
        Map<String, Object> llmConfig = (Map<String, Object>) config.get("llm");
        if (llmConfig == null) {
            throw new IllegalStateException("'llm' section not found in application.yml");
        }
        String provider = (String) llmConfig.get("provider");
        if (provider == null) {
            throw new IllegalStateException("'llm.provider' not configured in application.yml");
        }
        @SuppressWarnings("unchecked")
        Map<String, String> providerConfig = (Map<String, String>) llmConfig.get(provider);

        String apiKey = providerConfig.get("api-key");
        if (apiKey == null || apiKey.contains("your-api-key-here")) {
            throw new IllegalStateException("Please configure API Key first! See step-00-setup");
        }

        return OpenAiChatModel.builder()
                .baseUrl(providerConfig.get("base-url"))
                .apiKey(apiKey)
                .modelName(providerConfig.get("model-name"))
                .build();
    }

    private static Map<String, Object> loadConfig() {
        Yaml yaml = new Yaml();
        try (InputStream is = RagDemo.class.getClassLoader()
                .getResourceAsStream("application.yml")) {
            if (is == null) {
                throw new IllegalStateException("application.yml not found");
            }
            return yaml.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read application.yml", e);
        }
    }
}
