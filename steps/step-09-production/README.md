# Step 09: 生产优化

## 学习目标

- 理解从 InMemoryEmbeddingStore 到 Milvus 的迁移路径
- 掌握超时控制、重试机制、降级策略
- 理解监控指标（Token 消耗、响应延迟）的重要性
- 了解生产环境的安全和部署考量

## 前置条件

- 完成 [Step 07: Spring Boot REST API](../step-07-spring-boot-api/)
- （可选）安装 Docker 用于运行 Milvus

## 核心概念

### 开发 vs 生产的关键差异

| 方面 | 开发环境 | 生产环境 |
|------|---------|---------|
| 向量存储 | InMemoryEmbeddingStore | Milvus / pgvector / Weaviate |
| 会话记忆 | InMemoryChatMemoryStore | Redis / 数据库 |
| 错误处理 | 打印异常 | 重试 + 降级 + 告警 |
| 监控 | 控制台日志 | Prometheus + Grafana |
| 安全 | 无认证 | JWT / OAuth2 |

### 向量数据库对比

| 数据库 | 特点 | 适用场景 |
|--------|------|---------|
| Milvus | 高性能、分布式 | 大规模生产环境 |
| pgvector | PostgreSQL 扩展 | 已有 PG 的项目 |
| Weaviate | 内置混合搜索 | 需要语义搜索 |
| Chroma | 轻量级 Python | 快速原型 |

### 关键优化代码

```java
// 超时 + 重试
ChatModel chatModel = OpenAiChatModel.builder()
    .timeout(Duration.ofSeconds(30))
    .maxRetries(3)
    .build();

// 优雅降级：Milvus 不可用时回退到 InMemory
EmbeddingStore<TextSegment> store = createEmbeddingStore();
```

## 运行方式

```bash
# 方式 1: 无 Milvus（自动回退到 InMemory）
cd steps/step-09-production
mvn compile exec:java

# 方式 2: 有 Milvus（完整生产体验）
docker-compose up -d          # 启动 Milvus
mvn compile exec:java         # 运行演示
docker-compose down           # 停止 Milvus
```

## 你会看到什么

```
=== Step 09: 生产优化 ===

[优化 1] 超时和重试控制
      ChatModel 配置: timeout=30s, maxRetries=3
[优化 2] 向量数据库
      Milvus 不可用，回退到 InMemoryEmbeddingStore
[优化 3] 文档摄入（带监控）
      摄入耗时: 1234ms

=== 带监控的查询测试 ===
问: 编码规范中类名应该怎么命名？
答: 根据手册第一条，类名应使用大驼峰命名法...
耗时: 856ms
```

## 与上一步的区别

- Step 07/08：功能完整的 Spring Boot + Vue 应用
- **Step 09**：关注生产环境的非功能性需求（性能、可靠性、安全）

## 练习

- [ ] 使用 Docker 启动 Milvus，验证数据持久化（重启后无需重新摄入）
- [ ] 在 Step 07 的 Spring Boot 项目中集成 Micrometer，暴露 Prometheus 指标
- [ ] 实现一个自定义的 `ChatMemoryStore`，使用文件系统持久化会话

## 下一步

恭喜你完成了所有学习步骤！查看 `final/` 目录获取完整的项目参考实现。
