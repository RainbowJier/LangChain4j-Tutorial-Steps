package com.smartdoc.chat;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface DocAssistant {


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
    TokenStream chat(@UserMessage String message, @MemoryId String sessionId);
}
