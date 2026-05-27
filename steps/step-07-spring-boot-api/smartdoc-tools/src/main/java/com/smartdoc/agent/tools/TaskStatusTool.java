package com.smartdoc.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 任务状态查询工具
 *
 * 【Step 06 对照】Step 06 中我们定义了类似的工具方法来查询业务状态。
 * 这里用 @Component 注册为 Spring Bean，由 ChatConfig 自动注入。
 *
 * 工具调用流程：
 * 1. 用户问："我的审批进度怎样？编号 GH-2024-001"
 * 2. LLM 识别出需要调用工具，提取参数 businessId="GH-2024-001"
 * 3. LangChain4j 自动调用 queryTaskStatus("GH-2024-001")
 * 4. 方法返回 JSON 结果，LLM 基于此生成自然语言回答
 *
 * 当前返回模拟数据。生产环境中应替换为真实的业务系统 API 调用。
 */
@Component
public class TaskStatusTool {

    /**
     * 查询任务/业务状态
     *
     * @Tool 注解的描述文字非常重要，它决定了 LLM 何时选择调用此工具。
     * 描述要清晰说明工具的功能和适用场景。
     *
     * @P 注解为参数提供描述，帮助 LLM 理解应该传入什么样的值。
     */
    @Tool("查询任务或业务的办理状态，如审批进度、许可证申请等")
    public String queryTaskStatus(@P("业务编号，如：GH-2024-001") String businessId) {
        return """
                {
                  "businessId": "%s",
                  "businessType": "建设用地规划许可证",
                  "status": "审核中",
                  "currentStep": "规划审查",
                  "totalSteps": 5,
                  "completedSteps": 3,
                  "applicant": "XX开发有限公司",
                  "submitDate": "2024-03-15",
                  "estimatedCompletionDate": "2024-05-01",
                  "nextStep": "公示阶段",
                  "remarks": "已通过专家评审，待公示"
                }
                """.formatted(businessId);
    }
}
