# Step 04: Agent 工具调用

## 学习目标

- 理解 `@Tool` 注解 — 让 LLM 调用你写的 Java 方法
- 理解 `@P` 注解 — 描述参数含义，帮助 LLM 正确传参
- 理解 Function Calling 机制 — LLM 自动决定何时调用哪个工具
- 学会创建自定义工具并注册到 AiServices

## 前置条件

- 完成 [Step 01: 你好 LLM](../step-01-hello-llm/)

## 核心概念

### Function Calling 工作流程

```
用户: "123 * 456 等于多少？"
  ↓
LLM 分析: 这是计算题 → 需要调用 calculate 工具
  ↓
LLM 输出: { "a": 123, "operator": "*", "b": 456 }
  ↓
框架调用: CalculatorTool.calculate(123, "*", 456)
  ↓
方法返回: "123.00 * 456.00 = 56088.00"
  ↓
LLM 生成: "123 乘以 456 等于 56088"
```

### @Tool 和 @P 注解

```java
@Tool("计算两个数的四则运算结果。当用户需要做数学计算时使用。")
public String calculate(
        @P("第一个数字") double a,
        @P("运算符：+、-、*、/") String operator,
        @P("第二个数字") double b) {
    // ...
}
```

- `@Tool` 的描述是给 **LLM 看的**，不是给人看的。描述越清晰，LLM 越能正确调用
- `@P` 的描述帮助 LLM 理解每个参数应该传什么值
- 方法返回 String，LLM 会理解返回的内容

### 注册工具

```java
AiServices.builder(ToolAssistant.class)
    .chatModel(chatModel)
    .tools(calcTool, dateTimeTool)   // ← 注册工具
    .build();
```

## 运行方式

```bash
cd steps/step-04-agent-tools
mvn compile exec:java
```

## 你会看到什么

```
[演示 1] 触发计算器工具
答: 123 乘以 456 等于 56088。

[演示 2] 触发日期时间工具
答: 现在是 2026年5月11日 15:30:45。

[演示 3] 不触发工具（普通对话）
答: 你好！我是智能助手...
```

## 关键 API

| API | 用途 |
|-----|------|
| `@Tool("描述")` | 标注方法为可调用工具 |
| `@P("描述")` | 描述参数含义 |
| `AiServices.builder().tools(...)` | 注册工具实例 |

## 与上一步的区别

- Step 03：`.contentRetriever(retriever)` — LLM 被动接收知识
- **Step 04**：`.tools(calcTool, dateTimeTool)` — LLM 主动调用方法

## 练习

- [ ] 创建一个 `WeatherTool`，模拟返回城市天气（不需要真实 API，返回硬编码数据即可）
- [ ] 故意把 `@Tool` 的描述写得模糊（如 "计算东西"），观察 LLM 是否还能正确调用
- [ ] 思考：工具方法可以返回哪些类型？试试返回一个 JSON 字符串

## 下一步

[Step 05: 会话记忆](../step-05-memory-session/) — 让 LLM 记住对话历史
