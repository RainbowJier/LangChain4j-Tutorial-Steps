package com.smartdoc.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

/**
 * 文档摄入服务
 *
 * 【Step 06 对照】Step 06 中我们在 main() 里手动构建摄入管道：
 *
 *   // Step 06 的写法：
 *   EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
 *       .embeddingStore(embeddingStore)
 *       .embeddingModel(embeddingModel)
 *       .documentSplitter(DocumentSplitters.recursive(500, 100))
 *       .build();
 *   ingestor.ingest(document);
 *
 * Spring Boot 方式：封装为 @Service，通过构造函数注入依赖，
 * 分块参数从 application.yml 读取（rag.chunk-size, rag.chunk-overlap）。
 *
 * 核心变化：
 * - 手动传递依赖 → Spring 自动注入 EmbeddingStore 和 EmbeddingModel
 * - 硬编码分块参数 → @Value 从配置文件读取，支持运行时调整
 * - main() 中一次性调用 → 可通过 REST API 重复调用（上传文档端点）
 *
 * @see EmbeddingStoreIngestor
 */
@Slf4j
@Service
public class DocumentIngestionService {

    /** LangChain4j 文档摄入器，封装了完整的处理管道 */
    private final EmbeddingStoreIngestor ingestor;

    /**
     * 构造文档摄入服务
     *
     * 【Step 06 对照】Step 06 中参数都是硬编码的，
     * 这里通过 Spring 依赖注入获取 EmbeddingStore、EmbeddingModel，
     * 通过 @Value 注解从 application.yml 读取分块参数。
     *
     * @param store          向量存储 Bean（由 EmbeddingConfig 创建）
     * @param embeddingModel 向量模型 Bean（由 LlmConfig 创建）
     * @param chunkSize      分块大小，配置路径 rag.chunk-size，默认 500
     * @param chunkOverlap   分块重叠，配置路径 rag.chunk-overlap，默认 100
     */
    public DocumentIngestionService(EmbeddingStore<TextSegment> store,
                                    EmbeddingModel embeddingModel,
                                    @Value("${rag.chunk-size:500}") int chunkSize,
                                    @Value("${rag.chunk-overlap:100}") int chunkOverlap) {
        this.ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(DocumentSplitters.recursive(chunkSize, chunkOverlap))
                .build();
        log.info("DocumentIngestionService initialized: chunkSize={}, chunkOverlap={}",
                chunkSize, chunkOverlap);
    }

    /**
     * 摄入单个文档
     *
     * 【Step 06 对照】Step 06 中直接调用 ingestor.ingest(document)，
     * 这里增加了自动文件类型检测和日志记录。
     *
     * @param documentPath 文档文件路径
     */
    public void ingestDocument(Path documentPath) {
        String fileName = documentPath.getFileName().toString();
        DocumentParser parser = resolveParser(fileName);
        Document document = FileSystemDocumentLoader.loadDocument(documentPath, parser);
        ingestor.ingest(document);
        log.info("Ingested document: {} ({} chars)", fileName, document.text().length());
    }

    /**
     * 根据文件扩展名选择文档解析器
     *
     * 【Step 06 对照】Step 06 中只用了 TextDocumentParser，
     * 这里支持 PDF（PdfBox）、Word（POI）、纯文本三种格式。
     */
    private DocumentParser resolveParser(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return new ApachePdfBoxDocumentParser();
        } else if (fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
            return new ApachePoiDocumentParser();
        } else {
            return new TextDocumentParser();
        }
    }
}
