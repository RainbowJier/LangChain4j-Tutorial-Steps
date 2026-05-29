package com.tutorial;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class SessionMemoryConfig {
    public static ChatMemoryProvider createMemoryProvider() {

        return new ChatMemoryProvider() {
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
    }

}
