package com.smartdoc.chat;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天会话管理器
 *
 * 【Step 06 对照】Step 06 中我们用 ConcurrentHashMap 管理会话：
 *
 *   // Step 06 的写法：
 *   Map<String, ChatMemory> sessionMap = new ConcurrentHashMap<>();
 *   ChatMemory memory = sessionMap.computeIfAbsent(sessionId,
 *       id -> MessageWindowChatMemory.withMaxMessages(20));
 *
 * Spring Boot 方式：封装为 @Component，通过 @Value 读取最大消息数。
 * 整个应用共享同一个 ChatSessionManager 实例（单例 Bean）。
 *
 * 线程安全：ConcurrentHashMap 保证多线程环境下的会话隔离。
 * 每个 sessionId 对应一个独立的 ChatMemory，互不干扰。
 *
 * @see MessageWindowChatMemory
 */
@Component
public class ChatSessionManager {

    /** 单个会话保留的最大消息数（超出后自动丢弃最早的消息） */
    private final int maxMessages;

    /** 会话存储：sessionId → ChatMemory */
    private final Map<String, ChatMemory> sessions = new ConcurrentHashMap<>();

    /**
     * 构造会话管理器
     *
     * @param maxMessages 最大记忆消息数，配置路径 chat.max-memory-messages，默认 20
     *                    【Step 06 对照】Step 06 中硬编码为 20，
     *                    Spring Boot 方式支持通过 application.yml 调整
     */
    public ChatSessionManager(@Value("${chat.max-memory-messages:20}") int maxMessages) {
        this.maxMessages = maxMessages;
    }

    /**
     * 获取或创建会话记忆
     *
     * 【Step 06 对照】与 Step 06 的 computeIfAbsent 逻辑完全一致。
     * Spring Boot 的改进：这个方法可以被多个 Controller 共享，
     * 无需在每处重复创建 ConcurrentHashMap。
     *
     * @param sessionId 会话 ID
     * @return 该会话对应的 ChatMemory 实例
     */
    public ChatMemory getOrCreate(String sessionId) {
        return sessions.computeIfAbsent(sessionId,
                id -> MessageWindowChatMemory.withMaxMessages(maxMessages));
    }

    /**
     * 清除指定会话的记忆
     *
     * 对应 DELETE /api/chat/session/{sessionId} 端点
     *
     * @param sessionId 要清除的会话 ID
     */
    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * 获取所有活跃会话（用于监控和管理）
     *
     * @return 所有会话的 Map 视图
     */
    public Map<String, ChatMemory> getAllSessions() {
        return sessions;
    }
}
