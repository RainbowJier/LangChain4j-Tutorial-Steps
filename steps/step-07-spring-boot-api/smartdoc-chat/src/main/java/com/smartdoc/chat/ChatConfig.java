package com.smartdoc.chat;

import com.smartdoc.agent.tools.KnowledgeSearchTool;
import com.smartdoc.agent.tools.TaskStatusTool;
import com.smartdoc.rag.RetrievalService;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天配置类 - AiService 装配核心
 *
 * 【Step 06 对照】这是整个项目最核心的对照点。
 * Step 06 中我们在 main() 方法里手动装配 AiServices：
 *
 *   // Step 06 的写法（全部手动创建和传递）：
 *   Assistant assistant = AiServices.builder(Assistant.class)
 *       .chatModel(chatModel)
 *       .streamingChatModel(streamingChatModel)
 *       .contentRetriever(contentRetriever)
 *       .chatMemoryProvider(memoryId -> sessionMap.getOrCreate(memoryId.toString()))
 *       .tools(toolObject1, toolObject2)
 *       .build();
 *
 * Spring Boot 方式：用 @Configuration + @Bean 声明式装配。
 * 所有依赖通过构造函数注入自动获取（Spring IoC 容器管理）。
 *
 * 核心变化：
 * - 手动创建所有对象 → Spring 自动注入所有 Bean
 * - 一次性硬编码 → 可通过配置文件调整参数
 * - 无法测试 → 可以 Mock Bean 进行单元测试
 *
 * AiServices.builder() 的每个参数都是 Spring Bean：
 * - chatModel: 由 LlmConfig 创建
 * - streamingChatModel: 由 LlmConfig 创建
 * - contentRetriever: 由 RetrievalService 创建
 * - chatMemoryProvider: 使用 ChatSessionManager 动态创建
 * - tools: 由 @Component 注解的工具类
 *
 * @see AiServices
 * @see DocAssistant
 */
@Configuration
public class ChatConfig {

    /**
     * 创建 DocAssistant AiService 代理对象
     *
     * 【Step 06 对照】这是 Step 06 中 AiServices.builder() 调用的直接映射。
     * 所有参数都通过 Spring 依赖注入获取，而非手动创建。
     *
     * Spring 保证在调用此方法时，所有参数对应的 Bean 已经创建完毕。
     * 依赖注入的顺序由 Spring 自动管理，无需关心。
     *
     * @param chatModel         同步聊天模型（用于 Tool Call 等场景）
     * @param streamingChatModel 流式聊天模型（用于 SSE 流式输出）
     * @param retrievalService  检索服务（提供 RAG 内容检索器）
     * @param sessionManager    会话管理器（管理多用户会话记忆）
     * @param knowledgeSearchTool 知识搜索工具
     * @param taskStatusTool     任务状态工具
     * @return DocAssistant 代理对象，注册为 Spring Bean
     */
    @Bean
    public DocAssistant docAssistant(
            ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            RetrievalService retrievalService,
            ChatSessionManager sessionManager,
            KnowledgeSearchTool knowledgeSearchTool,
            TaskStatusTool taskStatusTool) {

        // 【Step 06 对照】以下 builder 调用与 Step 06 完全相同，
        // 只是参数来源从"手动创建"变为"Spring 注入"
        return AiServices.builder(DocAssistant.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .contentRetriever(retrievalService.getContentRetriever())
                // ChatMemoryProvider：根据 memoryId（即 sessionId）动态创建/获取 ChatMemory
                .chatMemoryProvider((ChatMemoryProvider) memoryId ->
                        sessionManager.getOrCreate(memoryId.toString()))
                // 注册 Agent 工具：LLM 可以在对话中自动调用这些工具
                .tools(knowledgeSearchTool, taskStatusTool)
                .build();
    }
}
