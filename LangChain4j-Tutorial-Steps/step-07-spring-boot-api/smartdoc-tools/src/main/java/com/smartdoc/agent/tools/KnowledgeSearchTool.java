package com.smartdoc.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 知识搜索工具
 *
 * 【Step 06 对照】Step 06 中我们定义了带 @Tool 注解的方法，
 * 然后手动传递给 AiServices.builder().tools(...)：
 *
 *   // Step 06 的写法（在一个类中）：
 *   @Tool("搜索知识库...")
 *   public String searchKnowledge(String query) { ... }
 *
 *   // 然后在 AiServices 构建时注册：
 *   AiServices.builder(Assistant.class)
 *       .tools(this)   // 传入包含 @Tool 方法的对象
 *       .build();
 *
 * Spring Boot 方式：用 @Component 注册为 Spring Bean，
 * ChatConfig 中通过依赖注入获取并传给 AiServices。
 *
 * @Tool 注解告诉 LangChain4j：这个方法可以被 AI 调用。
 * @P 注解描述参数含义，帮助 LLM 正确传参。
 *
 * 当前返回模拟数据。生产环境中应替换为真实的知识库检索调用。
 */
@Component
public class KnowledgeSearchTool {

    /**
     * 搜索知识库
     *
     * LLM 在对话中遇到需要查询知识库的问题时，会自动调用此方法。
     * 方法描述（@Tool 的值）告诉 LLM 何时应该调用这个工具。
     *
     * 【Step 06 对照】Step 06 中直接返回字符串，Spring Boot 中完全一致，
     * 区别仅在于：由 Spring 管理 Bean 生命周期，而非手动创建实例。
     */
    @Tool("根据关键词搜索知识库中的政策法规和文档，返回匹配的文档标题和摘要")
    public String searchKnowledge(@P("搜索关键词，如：国土空间规划、建设用地、生态红线") String keyword) {
        return """
                {
                  "total": 2,
                  "documents": [
                    {
                      "title": "国土空间规划编制技术指南",
                      "docNumber": "自然资发〔2024〕XX号",
                      "summary": "详细规定了国土空间规划的编制要求和技术标准。与「%s」高度相关。",
                      "publishDate": "2024-01-15"
                    },
                    {
                      "title": "建设用地审批管理办法",
                      "docNumber": "自然资发〔2023〕XX号",
                      "summary": "规范建设用地的申请、审查、批准程序。包含「%s」相关条款。",
                      "publishDate": "2023-06-01"
                    }
                  ]
                }
                """.formatted(keyword, keyword);
    }
}
