# Step 03: RAG 检索增强生成

## 学习目标

- 理解 RAG（Retrieval-Augmented Generation）的核心原理
- 掌握 Embedding、向量存储、文档摄入、内容检索的完整流程
- 理解文本分块策略（为什么 500 字？为什么 100 字重叠？）
- 体验 RAG 如何让 LLM 基于你的私有文档回答问题

## 前置条件

- 完成 [Step 01: 你好 LLM](../step-01-hello-llm/)

## 核心概念

### RAG 流程

```
用户提问: "API 分页参数的默认值是什么？"
    ↓
1. 问题转向量     EmbeddingModel.embed("API 分页参数...")
2. 向量检索       在 EmbeddingStore 中找最相似的 5 段文本
3. 拼接上下文     把 5 段文本 + 问题一起发给 LLM
4. LLM 生成回答   基于上下文生成："默认 page=0, size=20"
```

### 关键组件

| 组件 | 作用 | 本步使用 |
|------|------|---------|
| `EmbeddingModel` | 文本转向量 | AllMiniLmL6-v2（本地，384 维） |
| `EmbeddingStore` | 存储向量 | InMemoryEmbeddingStore（内存） |
| `DocumentSplitter` | 文本分块 | recursive(500, 100) |
| `EmbeddingStoreIngestor` | 分块+向量化+存储 | 一步完成摄入 |
| `ContentRetriever` | 检索相关内容 | maxResults=5 |

### 为什么需要分块？

- LLM 上下文窗口有限，不能把整个文档塞进去
- 分块后检索更精准（只返回最相关的几段）
- 重叠（overlap=100）保证跨块边界的信息不丢失

## 运行方式

```bash
cd steps/step-03-rag-retrieval
mvn compile exec:java
```

> 首次运行会下载 AllMiniLmL6-v2 ONNX 模型（约 23MB），请耐心等待。

## 你会看到什么

```
=== Step 03: RAG 检索增强生成 ===

[1/9] 创建 Embedding 模型...
      AllMiniLmL6-v2 就绪（384 维，本地 ONNX 推理）
[2/9] 创建向量存储...
      InMemoryEmbeddingStore 就绪
...
[8/9] 组装 AI 服务...
      AI 服务组装完成！

=== RAG 对话 ===
你: 编码规范中类名应该怎么命名？
助手: 根据手册第一条，类名应使用大驼峰命名法（PascalCase）...
```

## 与上一步的区别

- Step 02：LLM 只能用训练数据回答
- **Step 03**：LLM 能基于你的文档（knowledge-base.txt）回答 — 关键区别是 `.contentRetriever(retriever)`

## 练习

- [ ] 修改分块大小为 50，再问具体问题，观察检索质量变化
- [ ] 在 `resources/` 中添加第二个 txt 文件，修改代码加载多个文档
- [ ] 对比有无 RAG 时对同一问题的回答（去掉 `.contentRetriever()` 再试）

## 下一步

[Step 04: Agent 工具调用](../step-04-agent-tools/) — 让 LLM 调用你写的 Java 方法
