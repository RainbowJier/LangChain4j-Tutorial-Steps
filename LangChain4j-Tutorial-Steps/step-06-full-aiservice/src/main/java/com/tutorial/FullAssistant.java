package com.tutorial;

import com.tutorial.tools.KnowledgeSearchTool;
import com.tutorial.tools.TaskStatusTool;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Step 06: Full AiService Assembly — integrates all previous step concepts into one service.
 * <p>
 * This step combines:
 * - Step 01: ChatModel (LLM invocation)
 * - Step 02: StreamingChatModel (streaming output)
 * - Step 03: ContentRetriever (RAG retrieval)
 * - Step 04: @Tool (Agent tool calling)
 * - Step 05: ChatMemoryProvider (session memory)
 * <p>
 * Mapping (Plain Java → Spring Boot):
 * - main() method here        →  ChatConfig.java in Spring Boot
 * - Manual object creation     →  Spring IoC auto-injection
 * - Map<String, ChatMemory>   →  ChatSessionManager.java
 * <p>
 * The AiServices.builder() call is the core of the entire application.
 * In a Spring Boot project, it corresponds to a @Bean method in ChatConfig.java.
 */
public class FullAssistant {

    // ===== AI Service Interface (Full) =====
    @SystemMessage("""
            You are a "Smart Document Assistant" that helps the team query development handbook content and manage tasks.
            
            Capabilities:
            1. Answer development standard questions based on knowledge base (auto-retrieval)
            2. Search team knowledge base for more information
            3. Query task and deployment status
            
            Response guidelines:
            - Prioritize answers from the knowledge base; cite clause numbers when referencing
            - For information not in the knowledge base, search the team knowledge base
            - If unsure, honestly say so
            """)
    interface DocAssistant {
        TokenStream chat(@MemoryId String sessionId, @UserMessage String message);
    }

    public static void main(String[] args) throws Exception {
        // ===== 1. ChatModel + StreamingChatModel (from Step 01 + 02) =====
        Map<String, String> providerConfig = ProviderConfig.loadProviderConfig();

        // ChatModel for tool calling
        ChatModel chatModel = createChatModel(providerConfig);
        StreamingChatModel streamingModel = createStreamingChatModel(providerConfig);

        // RAG
        ContentRetriever retriever = RagConfig.createRetriever();

        // Tools
        KnowledgeSearchTool knowledgeTool = new KnowledgeSearchTool();
        TaskStatusTool taskTool = new TaskStatusTool();

        // Session memory
        ChatMemoryProvider memoryProvider = SessionMemoryConfig.createMemoryProvider();

        // Assemble
        DocAssistant assistant = AiServices.builder(DocAssistant.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingModel)
                .contentRetriever(retriever)
                .tools(knowledgeTool, taskTool)
                .chatMemoryProvider(memoryProvider)
                .build();

        System.out.println("=== Full-featured Conversation ===");
        System.out.println("Try these questions:");
        System.out.println("  RAG:   What are the naming conventions for class names?");
        System.out.println("  Tool:  Search the knowledge base for deployment process docs");
        System.out.println("  Tool:  Query the deployment status of project smartdoc-api");
        System.out.println("  Mem:   First say 'I am Xiao Ming from the backend team', then ask 'Who am I?'");
        System.out.println("Type 'exit' to quit\n");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String question = scanner.nextLine().trim();
            if (question.isEmpty()) continue;
            if ("exit".equalsIgnoreCase(question)) {
                System.out.println("Goodbye!");
                break;
            }

            CountDownLatch latch = new CountDownLatch(1);
            System.out.print("Assistant: ");

            assistant.chat("interactive", question)
                    .onPartialResponse(System.out::print)
                    .onCompleteResponse(response -> {
                        System.out.println("\n");
                        latch.countDown();
                    })
                    .onError(error -> {
                        System.err.println("\nError: " + error.getMessage());
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

    private static ChatModel createChatModel(Map<String, String> providerConfig) {
        return OpenAiChatModel.builder()
                .baseUrl(providerConfig.get("base-url"))
                .apiKey(providerConfig.get("api-key"))
                .modelName(providerConfig.get("model-name"))
                .build();
    }

    private static StreamingChatModel createStreamingChatModel(Map<String, String> providerConfig) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(providerConfig.get("base-url"))
                .apiKey(providerConfig.get("api-key"))
                .modelName(providerConfig.get("model-name"))
                .build();
    }
}
