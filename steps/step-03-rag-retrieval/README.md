# Step 03: RAG Search Enhancement Generation

## Table of Contents

- [Learning Target](#learning-target)
- [Prerequisites](#prerequisites)
- [Core Concepts](#core-concepts)
  - [RAG Workflow](#rag-workflow)
  - [Key Components](#key-components)
  - [Why Text Chunking?](#why-text-chunking)
  - [What is Overlap?](#what-is-overlap)
- [Operation Mode](#operation-mode)
- [What you will see](#what-you-will-see)
- [Differences from Previous Step](#differences-from-previous-step)
- [Exercises](#exercises)
- [Next Steps](#next-steps)

---

## Learning Target

- Understand the core concepts of RAG (Retrieval-Augmented Generation).
- Master the complete process of Embedding, Vector Store, Document Intake, Content Retrieval.
- Understand the text chunking strategy (why 500 words? why 100 words `overlap`[重叠]?).
- Experience how RAG enables LLM to answer questions based on your private documents.

--- 

## Prerequisites

- Complete [Step 01: Hello LLM](../step-01-hello-llm/)

--- 

## Core Concepts

### RAG Workflow

```
Ask: "What is the default value of page params for API?"
    ↓
1. Question to Vector: EmbeddingModel.embed("Page Params for API...")
2. Vector Retrieval: Finding the most similar 5 pieces of text in the EmbeddingStore.
3. Merge Context: Send 5 peices of text and questions to LLM.
4. LLM Generate Answer: Based on the context, generate: "The default value of page params for API is page=0, size=20"
```

### Key Components

| Component                | Purpose                     | Used in this step              |
|--------------------------|-----------------------------|--------------------------------|
| `EmbeddingModel`         | Text to Vector              | AllMiniLmL6-v2（local, 384-dim） |
| `EmbeddingStore`         | Vector Store                | InMemoryEmbeddingStore（memory） |
| `DocumentSplitter`       | Text Chunk                  | recursive(500, 100)            |
| `EmbeddingStoreIngestor` | Chunk + Vector + Store      | One-step ingestion             |
| `ContentRetriever`       | Search for relevant content | maxResults=5                   |

### Why Text Chunking?

- LLM context window is limited; you cannot feed the entire document
- Retrieval is more `precise`[精确] after chunking (returns only the most relevant segments)
- Overlap (overlap=100) ensures information across chunk boundaries is not lost

### What is Overlap?

Overlap means shared text between consecutive chunks. When splitting a document, the last N tokens of Chunk 1 appear at the start of Chunk 2.

**Example:**
- Original text: "API parameters include page, size, sort, and filter..."
- Chunk size: 500 tokens | Overlap: 100 tokens
- Chunk 1: tokens 1-500
- Chunk 2: tokens 401-900 (includes tokens 401-500 from Chunk 1)
- Chunk 3: tokens 801-1300 (includes tokens 801-900 from Chunk 2)

```
┌─────────────────────────────────────────┐
│ Chunk 1: [tokens 1-500]                 │
│ └─ overlap: tokens 401-500 ─┬───────┐   │
├─────────────────────────────┼───────┤   │
│ Chunk 2: [tokens 401-900]   │       │   │
│ └─ overlap: tokens 801-900 ─┼───────┼─┐ │
├─────────────────────────────┼───────┼─│ │
│ Chunk 3: [tokens 801-1300]  │       │ │ │
└─────────────────────────────┴───────┴─│ │
                                    ↑ │ │
                                    └─┴─┘  Overlap ensures context continuity
```

Benefits**:**
1. **Preserves[保留] context**: Sentences split across chunks remain connected
2. **Better retrieval**: Relevant information appears in multiple chunks
3. **Reduces edge cases**: Important concepts less likely to be cut off

--- 

## Operation Mode

```bash
cd steps/step-03-rag-retrieval
mvn compile exec:java
```

## What you will see 

```
=== Step 03: RAG Retrieval-Augmented Generation ===

[1/9] Creating Embedding model...
      AllMiniLmL6-v2 ready (384 dimensions, local ONNX inference)
[2/9] Creating vector store...
      InMemoryEmbeddingStore ready
...
[8/9] Assembling[组装] AI service...
      AI service assembled!

=== RAG Conversation ===
You: How should class names be named in coding conventions?
Assistant: According to the handbook rule 1, class names should use PascalCase...
```

--- 

## Differences from Previous Step

- Step 02: LLM can only answer using training data
- **Step 03**: LLM can answer based on your documents (knowledge-base.txt) — key difference is `.contentRetriever(retriever)`

---

## Exercises

- [ ] Modify chunk size to 50, ask specific questions, observe changes in retrieval quality
- [ ] Add a second txt file in `resources/`, modify code to load multiple documents
- [ ] Compare answers with and without RAG for the same question (remove `.contentRetriever()` and try again)

---

## Next Steps

[Step 04: Agent Tool Calling](../step-04-agent-tools/) — Let LLM call your Java methods
