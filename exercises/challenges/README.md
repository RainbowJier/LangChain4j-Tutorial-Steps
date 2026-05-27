# 挑战题

这些练习没有标准答案，鼓励你独立思考和探索。

## 初级挑战

### 1. 多格式文档支持
当前 Step 03 只支持 TXT 文件。扩展它支持 PDF 和 DOCX：
- 提示：`langchain4j-document-parser-apache-pdfbox` 和 `langchain4j-document-parser-apache-poi`
- 根据文件扩展名选择不同的 `DocumentParser`

### 2. 对话导出
在 Step 08 的前端中，添加一个"导出对话"按钮：
- 点击后将当前会话的所有消息导出为 Markdown 文件
- 使用浏览器的 Blob API 触发下载

### 3. 流式 Token 计数
在 Step 02 中添加 Token 计数功能：
- 在 `onPartialResponse` 中统计生成的 Token 数量
- 在 `onCompleteResponse` 中显示总 Token 数

## 中级挑战

### 4. 自定义 ChatMemoryStore
实现一个基于文件系统的 `ChatMemoryStore`：
- 会话数据保存到 JSON 文件
- 应用重启后会话不丢失
- 注意并发安全和文件锁

### 5. RAG 重新排序
实现简单的检索结果重新排序：
- 在 `ContentRetriever` 返回结果后，对结果按关键词匹配度重新排序
- 比较重排前后的回答质量

### 6. 多文件批量上传
扩展 Step 07/08 的文档上传功能：
- 支持一次选择多个文件
- 显示每个文件的上传进度
- 支持拖拽上传

## 高级挑战

### 7. 混合检索
实现向量检索 + 关键词检索的混合模式：
- 向量检索：语义相似度
- 关键词检索：精确匹配
- 合并两种结果并去重

### 8. 流式工具调用
让工具调用也支持流式：
- 工具执行时实时报告进度
- 前端显示工具调用状态

### 9. 多轮工具调用
设计一个场景需要 LLM 连续调用多个工具：
- 例如：先查知识库 → 再搜索文档 → 最后查任务状态
- 分析 LangChain4j 如何处理多步工具调用
