package com.smartdoc.api;

import com.smartdoc.rag.DocumentIngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 应用配置类 - 启动时加载示例文档
 *
 * 【Step 06 对照】Step 06 中在 main() 方法里手动加载文档：
 *
 *   // Step 06 的写法：
 *   Document doc = FileSystemDocumentLoader.loadDocument(Path.of("doc.txt"), parser);
 *   ingestor.ingest(doc);
 *
 * Spring Boot 方式：使用 CommandLineRunner 在应用启动后自动执行初始化逻辑。
 * CommandLineRunner 在所有 Bean 初始化完成后、应用开始接收请求前执行。
 *
 * 示例文档放在 resources/sample-docs/ 目录下，
 * 打包时会包含在 JAR 中，启动时自动加载到向量存储。
 */
@Slf4j
@Configuration
public class AppConfig {

    private static final String SAMPLE_DOCS_PATH = "classpath:sample-docs/";

    /**
     * 启动时自动加载示例文档
     *
     * 【Step 06 对照】Step 06 在 main() 中一次性加载，Spring Boot 中用
     * CommandLineRunner 实现，保证在所有 Bean 就绪后才执行。
     *
     * @param ingestionService 文档摄入服务（Spring 自动注入）
     * @return CommandLineRunner 实例
     */
    @Bean
    public CommandLineRunner loadSampleDocuments(DocumentIngestionService ingestionService) {
        return args -> {
            try {
                // 使用 Spring 的资源模式解析器扫描 sample-docs 目录
                Resource[] resources = new PathMatchingResourcePatternResolver()
                        .getResources(SAMPLE_DOCS_PATH + "*");

                for (Resource resource : resources) {
                    if (resource.exists() && resource.isFile()) {
                        try {
                            Path file = Path.of(resource.getURI());
                            ingestionService.ingestDocument(file);
                            log.info("Loaded sample document: {}", file.getFileName());
                        } catch (Exception e) {
                            log.warn("Failed to load sample document: {}", resource.getFilename(), e);
                        }
                    }
                }
                log.info("Sample documents loading completed");
            } catch (IOException e) {
                log.warn("Failed to access sample documents directory (this is OK if no sample docs exist)", e);
            }
        };
    }
}
