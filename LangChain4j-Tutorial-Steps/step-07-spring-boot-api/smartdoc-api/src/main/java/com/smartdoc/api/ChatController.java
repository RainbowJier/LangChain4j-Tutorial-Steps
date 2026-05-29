package com.smartdoc.api;

import com.smartdoc.api.dto.ChatRequest;
import com.smartdoc.chat.ChatSessionManager;
import com.smartdoc.chat.DocAssistant;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天 REST 控制器
 *
 * 【Step 06 对照】Step 06 中用户交互通过控制台：
 *
 *   // Step 06 的写法：
 *   Scanner scanner = new Scanner(System.in);
 *   while (true) {
 *       String input = scanner.nextLine();
 *       TokenStream tokenStream = assistant.chat(input, sessionId);
 *       tokenStream.onPartialResponse(token -> System.out.print(token))
 *                  .start();
 *   }
 *
 * Spring Boot 方式：通过 REST API 暴露 HTTP 端点，支持 SSE 流式输出。
 * 前端通过 fetch() + ReadableStream 读取流式数据。
 *
 * 端点：
 * - POST /api/chat          → SSE 流式对话（核心端点）
 * - GET  /api/chat/history/{sessionId}  → 获取历史记录
 * - DELETE /api/chat/session/{sessionId} → 清除会话
 *
 * SSE（Server-Sent Events）工作原理：
 * 1. 客户端发送 POST 请求，Accept: text/event-stream
 * 2. 服务端返回 SseEmitter（保持连接不断开）
 * 3. 在异步线程中，逐 Token 通过 emitter.send() 推送
 * 4. LLM 生成完毕后调用 emitter.complete() 关闭连接
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    /** 异步线程池：每个 SSE 连接在独立线程中处理，避免阻塞 HTTP 线程 */
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /** DocAssistant AiService 代理对象（由 ChatConfig 创建的 Bean） */
    private final DocAssistant assistant;

    /** 会话管理器 */
    private final ChatSessionManager sessionManager;

    /**
     * SSE 流式对话端点
     *
     * 【Step 06 对照】Step 06 中 System.out.print(token) 输出到控制台。
     * Spring Boot 方式通过 SseEmitter 将每个 Token 推送给前端。
     *
     * 流程：
     * 1. 接收 JSON 请求 → ChatRequest(message, sessionId)
     * 2. 创建 SseEmitter（超时 120 秒）
     * 3. 在异步线程中调用 assistant.chat() 获取 TokenStream
     * 4. 逐 Token 通过 SSE 推送给客户端
     *
     * @param request 聊天请求，包含 message 和 sessionId
     * @return SseEmitter 流式响应
     */
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        // 创建 SSE 发射器，设置 120 秒超时
        SseEmitter emitter = new SseEmitter(120_000L);

        // 如果未提供 sessionId，使用默认值
        String sessionId = request.sessionId() != null ? request.sessionId() : "default";

        // 在异步线程中处理流式响应，避免阻塞 Tomcat HTTP 线程
        executor.execute(() -> {
            try {
                // 调用 AiService 获取 TokenStream
                // 【Step 06 对照】与 Step 06 的 assistant.chat() 调用完全一致
                TokenStream tokenStream = assistant.chat(request.message(), sessionId);

                tokenStream
                        // 每收到一个 Token，通过 SSE 推送给前端
                        .onPartialResponse(token -> {
                            try {
                                emitter.send(SseEmitter.event().data(token));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        })
                        // LLM 生成完毕，关闭 SSE 连接
                        .onCompleteResponse(response -> emitter.complete())
                        // 发生错误，终止 SSE 连接
                        .onError(error -> {
                            log.error("Chat streaming error", error);
                            emitter.completeWithError(error);
                        })
                        .start();
            } catch (Exception e) {
                log.error("Failed to start chat stream", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 获取会话历史记录
     *
     * @param sessionId 会话 ID
     * @return 会话信息
     */
    @GetMapping("/history/{sessionId}")
    public Map<String, Object> getHistory(@PathVariable String sessionId) {
        return Map.of("sessionId", sessionId);
    }

    /**
     * 清除指定会话的记忆
     *
     * 【Step 06 对照】Step 06 中通过 sessionMap.remove(sessionId) 清除。
     * Spring Boot 方式：通过 REST API 调用，前端可主动清除会话。
     *
     * @param sessionId 要清除的会话 ID
     * @return 操作结果
     */
    @DeleteMapping("/session/{sessionId}")
    public Map<String, String> clearSession(@PathVariable String sessionId) {
        sessionManager.clearSession(sessionId);
        return Map.of("status", "cleared", "sessionId", sessionId);
    }
}
