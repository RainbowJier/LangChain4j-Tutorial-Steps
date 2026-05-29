package com.smartdoc.chat;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * 智能文档助手接口 - LangChain4j AiService 定义
 *
 * 【Step 06 对照】Step 06 中我们定义了类似的接口：
 *
 *   // Step 06 的写法：
 *   interface Assistant {
 *       @SystemMessage("你是...")
 *       TokenStream chat(@UserMessage String message, @MemoryId String sessionId);
 *   }
 *
 * Spring Boot 中完全一样！这是 LangChain4j 的契约，
 * 与 Spring 无关。Spring 的角色是在 ChatConfig 中管理这个接口的实现（代理对象）。
 *
 * 关键注解：
 * - @SystemMessage：定义 AI 的角色和行为规则（系统提示词）
 * - @UserMessage：标记用户输入参数
 * - @MemoryId：标记会话 ID，用于区分不同用户的对话记忆
 * - 返回 TokenStream：支持流式输出（SSE）
 *
 * LangChain4j 会在运行时为这个接口生成代理对象（动态代理），
 * 自动处理 RAG 检索、工具调用、记忆管理等横切关注点。
 */
public interface DocAssistant {

    /**
     * 系统提示词 - 定义 AI 助手的人设和行为规则
     *
     * 【Step 06 对照】与 Step 06 的系统提示词结构相同，
     * 但内容适配了"智能文档助手"的领域。
     *
     * 提示词设计要点：
     * 1. 明确角色定位
     * 2. 列出具体职责
     * 3. 规定回答要求（准确性、引用规范等）
     */
    @SystemMessage("""
            你是「智能文档助手」，专门帮助用户解答文档管理、知识检索和业务办理相关问题。

            你的职责：
            1. 知识检索：根据用户的问题，搜索知识库中的相关文档和政策法规。
            2. 业务查询：查询任务办理状态，如审批进度、许可证申请等。
            3. 文档问答：基于已上传的文档内容，回答用户的具体问题。

            回答要求：
            - 专业准确，引用政策文件需注明文件名称和文号。
            - 如果不确定，请如实告知，不要编造信息。
            - 使用清晰、简洁的中文回答。
            """)
    /**
     * 流式对话方法
     *
     * 【Step 06 对照】与 Step 06 的接口方法签名完全一致。
     * Spring Boot 的优势在于：这个代理对象由 ChatConfig 创建为 Bean，
     * 可以在 Controller 中通过依赖注入获取，而不需要手动传递。
     *
     * @param message   用户输入的消息
     * @param sessionId 会话 ID，用于区分不同用户的对话记忆
     * @return TokenStream 流式响应，逐 Token 输出
     */
    TokenStream chat(@UserMessage String message, @MemoryId String sessionId);
}
