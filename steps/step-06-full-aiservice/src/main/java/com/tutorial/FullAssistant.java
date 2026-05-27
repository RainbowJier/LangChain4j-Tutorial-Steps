package com.tutorial;

import com.tutorial.tools.KnowledgeSearchTool;
import com.tutorial.tools.TaskStatusTool;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryProvider;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Step 06: 完整 AiService 装配 — 将前面所有步骤的概念整合到一个服务中。
 * <p>
 * 本步整合：
 * - Step 01: ChatModel（LLM 调用）
 * - Step 02: StreamingChatModel（流式输出）
 * - Step 03: ContentRetriever（RAG 检索）
 * - Step 04: @Tool（Agent 工具）
 * - Step 05: ChatMemoryProvider（会话记忆）
 * <p>
 * 对应关系（纯 Java → Spring Boot）：
 * - 本文件中的 main() 方法   →  Spring Boot 的 ChatConfig.java
 * - 手动创建所有对象         →  Spring IoC 自动注入
 * - Map<String, ChatMemory>  →  ChatSessionManager.java
 * <p>
 * 这个 AiServices.builder() 调用就是整个应用的核心。
 * 在 Spring Boot 项目中，它等价于 ChatConfig.java 中 @Bean 注解的方法。
 */
public class FullAssistant {

    // ===== AI 服务接口（完整版） =====
    @SystemMessage("""
            你是「智能文档助手」，帮助团队查询开发手册内容和管理开发任务。

            你的能力：
            1. 基于知识库回答开发规范相关问题（自动检索）
            2. 搜索团队知识库获取更多信息
            3. 查询任务和部署状态

            回答要求：
            - 优先基于知识库内容回答，引用时注明条款编号
            - 知识库中没有的信息，可以搜索团队知识库补充
            - 不确定时如实告知
            """)
    interface DocAssistant {
        TokenStream chat(@MemoryId String sessionId, @UserMessage String message);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Step 06: 完整 AiService 装配 ===\n");

        // ===== 1. ChatModel + StreamingChatModel（来自 Step 01 + 02） =====
        System.out.println("[1/5] 创建 LLM 模型...");
        Map<String, String> providerConfig = loadProviderConfig();
        ChatModel chatModel = OpenAiChatModel.builder()
                .baseUrl(providerConfig.get("base-url"))
                .apiKey(providerConfig.get("api-key"))
                .modelName(providerConfig.get("model-name"))
                .build();
        StreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
                .baseUrl(providerConfig.get("base-url"))
                .apiKey(providerConfig.get("api-key"))
                .modelName(providerConfig.get("model-name"))
                .build();
        System.out.println("      ChatModel + StreamingChatModel 就绪");

        // ===== 2. RAG 管道（来自 Step 03） =====
        System.out.println("[2/5] 初始化 RAG 管道...");
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

        DocumentParser parser = new TextDocumentParser();
        Path docPath = Path.of(FullAssistant.class.getClassLoader()
                .getResource("knowledge-base.txt").toURI());
        Document doc = FileSystemDocumentLoader.loadDocument(docPath, parser);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(DocumentSplitters.recursive(500, 100))
                .build();
        ingestor.ingest(doc);

        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .build();
        System.out.println("      RAG 管道就绪（文档已摄入，检索器就绪）");

        // ===== 3. Agent 工具（来自 Step 04） =====
        System.out.println("[3/5] 注册 Agent 工具...");
        KnowledgeSearchTool knowledgeTool = new KnowledgeSearchTool();
        TaskStatusTool taskTool = new TaskStatusTool();
        System.out.println("      工具就绪: KnowledgeSearchTool, TaskStatusTool");

        // ===== 4. 会话记忆（来自 Step 05） =====
        System.out.println("[4/5] 配置会话记忆...");
        ChatMemoryProvider memoryProvider = new ChatMemoryProvider() {
            private final InMemoryChatMemoryStore store = new InMemoryChatMemoryStore();

            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId.toString())
                        .chatMemoryStore(store)
                        .maxMessages(20)
                        .build();
            }
        };
        System.out.println("      会话记忆就绪（maxMessages=20）");

        // ===== 5. 组装！（核心时刻） =====
        System.out.println("[5/5] 组装 AiService...");
        DocAssistant assistant = AiServices.builder(DocAssistant.class)
                .chatModel(chatModel)                      // Step 01
                .streamingChatModel(streamingModel)         // Step 02
                .contentRetriever(retriever)                // Step 03
                .tools(knowledgeTool, taskTool)             // Step 04
                .chatMemoryProvider(memoryProvider)         // Step 05
                .build();
        System.out.println("      AI 服务组装完成！\n");

        System.out.println("所有组件已就绪：ChatModel + Streaming + RAG + Tools + Memory");
        System.out.println();
        System.out.println("=== 完整功能对话 ===");
        System.out.println("试试以下问题：");
        System.out.println("  RAG:   编码规范中类名应该怎么命名？");
        System.out.println("  Tool:  帮我搜索一下部署流程的文档");
        System.out.println("  Tool:  查询项目 smartdoc-api 的部署状态");
        System.out.println("  记忆:  先说'我是后端开发组的小明'，然后再问'我是谁'");
        System.out.println("输入 exit 退出\n");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("你: ");
            String question = scanner.nextLine().trim();
            if (question.isEmpty()) continue;
            if ("exit".equalsIgnoreCase(question)) {
                System.out.println("再见！");
                break;
            }

            CountDownLatch latch = new CountDownLatch(1);
            System.out.print("助手: ");

            assistant.chat("interactive", question)
                .onPartialResponse(token -> System.out.print(token))
                .onCompleteResponse(response -> {
                    System.out.println("\n");
                    latch.countDown();
                })
                .onError(error -> {
                    System.err.println("\n错误: " + error.getMessage());
                    latch.countDown();
                })
                .start();

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        scanner.close();
    }

    private static Map<String, String> loadProviderConfig() {
        Yaml yaml = new Yaml();
        InputStream is = FullAssistant.class.getClassLoader()
                .getResourceAsStream("application.yml");
        if (is == null) {
            throw new IllegalStateException("application.yml 未找到");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> config = yaml.load(is);
        @SuppressWarnings("unchecked")
        Map<String, Object> llmConfig = (Map<String, Object>) config.get("llm");
        String provider = (String) llmConfig.get("provider");
        @SuppressWarnings("unchecked")
        Map<String, String> providerConfig = (Map<String, String>) llmConfig.get(provider);

        String apiKey = providerConfig.get("api-key");
        if (apiKey == null || apiKey.contains("your-api-key-here")) {
            System.err.println("请先配置 API Key！参见 step-00-setup");
            System.exit(1);
        }
        return providerConfig;
    }
}
