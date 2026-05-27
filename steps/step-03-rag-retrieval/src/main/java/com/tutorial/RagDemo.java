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

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Step 03: RAG 检索增强生成 — 让 LLM 基于你自己的文档回答问题。
 * <p>
 * 完整流程（9 个阶段）：
 * 1. EmbeddingModel    — 文本转向量的模型（本地 AllMiniLmL6-v2，384 维，无需 API Key）
 * 2. EmbeddingStore    — 向量存储（内存版，开发够用）
 * 3. DocumentParser    — 文档解析器（支持 TXT/PDF/DOCX）
 * 4. DocumentSplitter  — 文本分块器（500 字一块，100 字重叠）
 * 5. EmbeddingStoreIngestor — 分块 + 向量化 + 存储（一步到位）
 * 6. ContentRetriever  — 检索器（从向量库找最相关的 5 段）
 * 7. ChatModel         — LLM 模型（用于生成最终回答）
 * 8. AiServices        — 组装（把检索器注入 AI 服务）
 * 9. 交互式对话
 */
public class RagDemo {

    @SystemMessage("""
            你是「智能文档助手」，帮助团队查询开发手册内容。
            回答要求：
            - 基于参考资料准确回答，引用时注明条款编号
            - 如果参考资料中没有相关信息，请如实告知"手册中暂无此内容"
            - 回答要简洁清晰
            """)
    interface RagAssistant {
        String chat(String message);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Step 03: RAG 检索增强生成 ===\n");

        // ===== 阶段 1: 创建 Embedding 模型（本地，无需 API Key） =====
        System.out.println("[1/9] 创建 Embedding 模型...");
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        System.out.println("      AllMiniLmL6-v2 就绪（384 维，本地 ONNX 推理）");

        // ===== 阶段 2: 创建向量存储 =====
        System.out.println("[2/9] 创建向量存储...");
        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        System.out.println("      InMemoryEmbeddingStore 就绪（重启后数据丢失）");

        // ===== 阶段 3: 解析文档 =====
        System.out.println("[3/9] 加载文档...");
        DocumentParser parser = new TextDocumentParser();
        Path docPath = Path.of(RagDemo.class.getClassLoader()
                .getResource("knowledge-base.txt").toURI());
        Document doc = FileSystemDocumentLoader.loadDocument(docPath, parser);
        System.out.println("      文档加载完成: " + docPath.getFileName());

        // ===== 阶段 4: 配置分块策略 =====
        // 500 字一块，100 字重叠 — 保证跨块信息不丢失
        System.out.println("[4/9] 配置分块策略...");
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);
        System.out.println("      递归分块: 500 字/块, 100 字重叠");

        // ===== 阶段 5: 摄入（分块 + 向量化 + 存储） =====
        System.out.println("[5/9] 文档摄入（分块 → 向量化 → 存储）...");
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(splitter)
                .build();
        ingestor.ingest(doc);
        System.out.println("      摄入完成！");

        // ===== 阶段 6: 创建检索器 =====
        // maxResults=5 表示每次检索返回最相关的 5 段文本
        System.out.println("[6/9] 创建检索器...");
        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .build();
        System.out.println("      检索器就绪（maxResults=5）");

        // ===== 阶段 7: 创建 ChatModel =====
        System.out.println("[7/9] 创建 ChatModel...");
        ChatModel chatModel = createChatModel();
        System.out.println("      ChatModel 就绪");

        // ===== 阶段 8: 组装 AI 服务（核心！） =====
        // 关键区别：比 Step 01 多了 .contentRetriever(retriever)
        System.out.println("[8/9] 组装 AI 服务...");
        RagAssistant assistant = AiServices.builder(RagAssistant.class)
                .chatModel(chatModel)
                .contentRetriever(retriever)   // ← 这一行让 AI 拥有了知识库
                .build();
        System.out.println("      AI 服务组装完成！\n");

        // ===== 阶段 9: 交互式对话 =====
        System.out.println("[9/9] 开始对话！\n");
        System.out.println("=== RAG 对话 ===");
        System.out.println("试试以下问题：");
        System.out.println("  - 编码规范中类名应该怎么命名？");
        System.out.println("  - API 分页参数的默认值是什么？");
        System.out.println("  - 部署到生产环境需要什么流程？");
        System.out.println("  - 代码审查需要几个人批准？");
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
            try {
                // 每次调用时，retriever 自动：
                // 1. 把问题转成向量
                // 2. 在向量库中搜索最相似的 5 段
                // 3. 把这些段作为上下文拼入 prompt
                // 4. LLM 基于上下文生成回答
                String answer = assistant.chat(question);
                System.out.println("助手: " + answer);
                System.out.println();
            } catch (Exception e) {
                System.err.println("错误: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static ChatModel createChatModel() {
        Yaml yaml = new Yaml();
        InputStream is = RagDemo.class.getClassLoader()
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

        return OpenAiChatModel.builder()
                .baseUrl(providerConfig.get("base-url"))
                .apiKey(apiKey)
                .modelName(providerConfig.get("model-name"))
                .build();
    }
}
