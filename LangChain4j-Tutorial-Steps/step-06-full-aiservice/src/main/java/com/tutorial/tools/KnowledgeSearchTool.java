package com.tutorial.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * 知识库搜索工具 — 模拟从外部系统搜索文档。
 * <p>
 * 在真实项目中，这里会调用数据库或搜索引擎。
 * 当前返回模拟数据用于演示。
 */
public class KnowledgeSearchTool {

    @Tool("搜索团队知识库。当用户问关于公司内部文档、流程、规范等问题时使用。")
    public String searchKnowledge(
            @P("搜索关键词") String keyword) {
        // 模拟返回搜索结果
        return """
                搜索 "%s" 的结果：
                1. [匹配] Java 编码规范 v2.1 — 包含命名、格式、注释规范
                2. [匹配] API 设计指南 — RESTful 接口设计最佳实践
                3. [相关] 部署流程文档 — CI/CD 和环境管理
                """.formatted(keyword);
    }
}
