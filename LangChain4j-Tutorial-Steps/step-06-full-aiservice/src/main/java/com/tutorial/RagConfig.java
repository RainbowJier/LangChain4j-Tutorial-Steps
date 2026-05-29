package com.tutorial;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.nio.file.Path;

public class RagConfig {

    private static final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    private static final EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
    private static final ContentRetriever retriever;

    static {
        try {
            ingestDocument();
            retriever = createRetriever();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RAG pipeline", e);
        }
    }

    private static void ingestDocument() throws Exception {
        DocumentParser parser = new TextDocumentParser();
        var kbUrl = RagConfig.class.getClassLoader().getResource("knowledge-base.txt");
        if (kbUrl == null) {
            throw new IllegalStateException("knowledge-base.txt not found in classpath");
        }
        Path docPath = Path.of(kbUrl.toURI());
        Document doc = FileSystemDocumentLoader.loadDocument(docPath, parser);

        EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(DocumentSplitters.recursive(500, 100))
                .build()
                .ingest(doc);
    }

    public static ContentRetriever createRetriever() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .build();
    }
}