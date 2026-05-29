package com.tutorial.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * 任务状态查询工具 — 模拟查询 CI/CD 流水线或部署任务状态。
 */
public class TaskStatusTool {

    @Tool("查询任务或部署的状态。当用户问'部署进度'、'任务完成了吗'等问题时使用。")
    public String getTaskStatus(@P("任务 ID 或项目名称") String taskId) {
        // 模拟返回任务状态
        return """
                任务状态报告：
                - 任务 ID: %s
                - 状态: 运行中
                - 当前步骤: 单元测试 (3/5)
                - 已完成: 编译 → 代码检查 → 安全扫描
                - 待执行: 单元测试 → 集成测试 → 部署
                - 预计完成: 5 分钟后
                """.formatted(taskId);
    }
}
