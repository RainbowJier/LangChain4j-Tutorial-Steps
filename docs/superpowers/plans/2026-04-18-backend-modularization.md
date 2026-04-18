# Backend Modularization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将单体 Maven 项目重构为多模块架构，分为 llm、rag、tools、chat、api 五个模块

**Architecture:** 父 POM 管理依赖和版本，子模块按功能职责分离。单向依赖：api → rag/tools/chat/llm，rag → llm。模块间通过 Spring Bean 注入交互，包名保持不变以减少代码修改。

**Tech Stack:** Maven 多模块项目, Spring Boot 3.5.0, LangChain4j 1.13.0

---

## Task 1: 备份当前代码并创建父 POM 结构

**Files:**
- Create: `backend/pom.xml` (重写为父 POM)
- Create: `backend/geosmart-llm/pom.xml`
- Create: `backend/geosmart-chat/pom.xml`
- Create: `backend/geosmart-tools/pom.xml`
- Create: `backend/geosmart-rag/pom.xml`
- Create: `backend/geosmart-api/pom.xml`

- [ ] **Step 1: 备份当前 pom.xml**

```bash
cd backend
cp pom.xml pom.xml.backup
```

- [ ] **Step 2: 创建父 POM**

创建 `backend/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
        <relativePath/>
    </parent>

    <groupId>com.geosmart</groupId>
    <artifactId>geosmart-agent</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>GeoSmart Agent</name>
    <description>Land Spatial Planning Intelligent Assistant - Parent POM</description>

    <properties>
        <java.version>17</java.version>
        <langchain4j.version>1.13.0</langchain4j.version>
        <langchain4j-beta.version>1.13.0-beta23</langchain4j-beta.version>
    </properties>

    <modules>
        <module>geosmart-llm</module>
        <module>geosmart-rag</module>
        <module>geosmart-tools</module>
        <module>geosmart-chat</module>
        <module>geosmart-api</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <!-- LangChain4j BOM -->
            <dependency>
                <groupId>dev.langchain4j</groupId>
                <artifactId>langchain4j-bom</artifactId>
                <version>${langchain4j.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <configuration>
                        <excludes>
                            <exclude>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                            </exclude>
                        </excludes>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

- [ ] **Step 3: 创建 geosmart-llm 模块目录和 pom.xml**

```bash
mkdir -p backend/geosmart-llm/src/main/java/com/geosmart/llm
mkdir -p backend/geosmart-llm/src/test/java/com/geosmart/llm
```

创建 `backend/geosmart-llm/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.geosmart</groupId>
        <artifactId>geosmart-agent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>geosmart-llm</artifactId>
    <name>GeoSmart LLM Module</name>
    <description>LLM provider configuration and switching</description>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-open-ai</artifactId>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-community-zhipu-ai</artifactId>
            <version>${langchain4j-beta.version}</version>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-embeddings-all-minilm-l6-v2</artifactId>
            <version>${langchain4j-beta.version}</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 4: 创建 geosmart-chat 模块目录和 pom.xml**

```bash
mkdir -p backend/geosmart-chat/src/main/java/com/geosmart/chat
mkdir -p backend/geosmart-chat/src/test/java/com/geosmart/chat
```

创建 `backend/geosmart-chat/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.geosmart</groupId>
        <artifactId>geosmart-agent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>geosmart-chat</artifactId>
    <name>GeoSmart Chat Module</name>
    <description>Chat session management</description>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 5: 创建 geosmart-tools 模块目录和 pom.xml**

```bash
mkdir -p backend/geosmart-tools/src/main/java/com/geosmart/agent/tools
mkdir -p backend/geosmart-tools/src/test/java/com/geosmart/agent/tools
```

创建 `backend/geosmart-tools/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.geosmart</groupId>
        <artifactId>geosmart-agent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>geosmart-tools</artifactId>
    <name>GeoSmart Tools Module</name>
    <description>Agent tools for spatial queries and business status</description>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 6: 创建 geosmart-rag 模块目录和 pom.xml**

```bash
mkdir -p backend/geosmart-rag/src/main/java/com/geosmart/rag
mkdir -p backend/geosmart-rag/src/test/java/com/geosmart/rag
```

创建 `backend/geosmart-rag/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.geosmart</groupId>
        <artifactId>geosmart-agent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>geosmart-rag</artifactId>
    <name>GeoSmart RAG Module</name>
    <description>Document ingestion, embedding, and retrieval</description>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>com.geosmart</groupId>
            <artifactId>geosmart-llm</artifactId>
            <version>${project.version}</version>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-document-parser-apache-pdfbox</artifactId>
            <version>${langchain4j-beta.version}</version>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-document-parser-apache-poi</artifactId>
            <version>${langchain4j-beta.version}</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 7: 创建 geosmart-api 模块目录和 pom.xml**

```bash
mkdir -p backend/geosmart-api/src/main/java/com/geosmart
mkdir -p backend/geosmart-api/src/main/resources
mkdir -p backend/geosmart-api/src/test/java/com/geosmart
```

创建 `backend/geosmart-api/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.geosmart</groupId>
        <artifactId>geosmart-agent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <artifactId>geosmart-api</artifactId>
    <name>GeoSmart API Module</name>
    <description>Main API module with REST controllers and assembly</description>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- 依赖所有子模块 -->
        <dependency>
            <groupId>com.geosmart</groupId>
            <artifactId>geosmart-llm</artifactId>
            <version>${project.version}</version>
        </dependency>

        <dependency>
            <groupId>com.geosmart</groupId>
            <artifactId>geosmart-rag</artifactId>
            <version>${project.version}</version>
        </dependency>

        <dependency>
            <groupId>com.geosmart</groupId>
            <artifactId>geosmart-tools</artifactId>
            <version>${project.version}</version>
        </dependency>

        <dependency>
            <groupId>com.geosmart</groupId>
            <artifactId>geosmart-chat</artifactId>
            <version>${project.version}</version>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 8: 验证父 POM 结构正确构建**

```bash
cd backend
mvn clean validate
```

Expected output: 包含所有 5 个子模块的构建列表

- [ ] **Step 9: 提交父 POM 结构**

```bash
cd backend
git add pom.xml geosmart-llm/pom.xml geosmart-chat/pom.xml geosmart-tools/pom.xml geosmart-rag/pom.xml geosmart-api/pom.xml
git commit -m "feat: create multi-module Maven parent POM structure

- Create parent POM with 5 child modules
- geosmart-llm: LLM provider configuration
- geosmart-chat: Session management
- geosmart-tools: Agent tools
- geosmart-rag: Document ingestion and retrieval
- geosmart-api: Main executable module

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 2: 迁移 LLM 模块

**Files:**
- Move: `src/main/java/com/geosmart/llm/LlmConfig.java` → `geosmart-llm/src/main/java/com/geosmart/llm/LlmConfig.java`
- Move: `src/main/java/com/geosmart/llm/LlmProperties.java` → `geosmart-llm/src/main/java/com/geosmart/llm/LlmProperties.java`
- Move: `src/test/java/com/geosmart/llm/*` → `geosmart-llm/src/test/java/com/geosmart/llm/`

- [ ] **Step 1: 移动 LlmProperties.java**

```bash
cd backend
mv src/main/java/com/geosmart/llm/LlmProperties.java geosmart-llm/src/main/java/com/geosmart/llm/
```

- [ ] **Step 2: 移动 LlmConfig.java**

```bash
cd backend
mv src/main/java/com/geosmart/llm/LlmConfig.java geosmart-llm/src/main/java/com/geosmart/llm/
```

- [ ] **Step 3: 移动测试文件（如果有）**

```bash
cd backend
if [ -d "src/test/java/com/geosmart/llm" ]; then
    mv src/test/java/com/geosmart/llm/* geosmart-llm/src/test/java/com/geosmart/llm/
fi
```

- [ ] **Step 4: 验证 LLM 模块构建**

```bash
cd backend/geosmart-llm
mvn clean compile
```

Expected: 编译成功

- [ ] **Step 5: 验证父 POM 构建**

```bash
cd backend
mvn clean compile
```

Expected: llm 模块编译成功

- [ ] **Step 6: 提交 LLM 模块迁移**

```bash
cd backend
git add geosmart-llm/src
git add -u src/main/java/com/geosmart/llm
git add -u src/test/java/com/geosmart/llm
git commit -m "refactor(llm): migrate LLM module to separate Maven module

- Move LlmConfig and LlmProperties to geosmart-llm module
- Move corresponding tests to geosmart-llm module

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 3: 迁移 Chat 模块

**Files:**
- Move: `src/main/java/com/geosmart/chat/ChatSessionManager.java` → `geosmart-chat/src/main/java/com/geosmart/chat/ChatSessionManager.java`
- Move: `src/test/java/com/geosmart/chat/ChatSessionManagerTest.java` → `geosmart-chat/src/test/java/com/geosmart/chat/`

- [ ] **Step 1: 移动 ChatSessionManager.java**

```bash
cd backend
mv src/main/java/com/geosmart/chat/ChatSessionManager.java geosmart-chat/src/main/java/com/geosmart/chat/
```

- [ ] **Step 2: 移动测试文件（如果有）**

```bash
cd backend
if [ -f "src/test/java/com/geosmart/chat/ChatSessionManagerTest.java" ]; then
    mv src/test/java/com/geosmart/chat/ChatSessionManagerTest.java geosmart-chat/src/test/java/com/geosmart/chat/
fi
```

- [ ] **Step 3: 验证 Chat 模块构建**

```bash
cd backend/geosmart-chat
mvn clean compile
```

Expected: 编译成功

- [ ] **Step 4: 提交 Chat 模块迁移**

```bash
cd backend
git add geosmart-chat/src
git add -u src/main/java/com/geosmart/chat
git add -u src/test/java/com/geosmart/chat
git commit -m "refactor(chat): migrate ChatSessionManager to separate Maven module

- Move ChatSessionManager to geosmart-chat module
- Move corresponding tests to geosmart-chat module

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 4: 迁移 Tools 模块

**Files:**
- Move: `src/main/java/com/geosmart/agent/tools/RegulationSearchTool.java` → `geosmart-tools/src/main/java/com/geosmart/agent/tools/`
- Move: `src/main/java/com/geosmart/agent/tools/SpatialQueryTool.java` → `geosmart-tools/src/main/java/com/geosmart/agent/tools/`
- Move: `src/main/java/com/geosmart/agent/tools/BusinessStatusTool.java` → `geosmart-tools/src/main/java/com/geosmart/agent/tools/`
- Move: `src/test/java/com/geosmart/agent/tools/*` → `geosmart-tools/src/test/java/com/geosmart/agent/tools/`

- [ ] **Step 1: 移动所有工具类**

```bash
cd backend
mv src/main/java/com/geosmart/agent/tools/RegulationSearchTool.java geosmart-tools/src/main/java/com/geosmart/agent/tools/
mv src/main/java/com/geosmart/agent/tools/SpatialQueryTool.java geosmart-tools/src/main/java/com/geosmart/agent/tools/
mv src/main/java/com/geosmart/agent/tools/BusinessStatusTool.java geosmart-tools/src/main/java/com/geosmart/agent/tools/
```

- [ ] **Step 2: 移动测试文件（如果有）**

```bash
cd backend
if [ -d "src/test/java/com/geosmart/agent/tools" ]; then
    mv src/test/java/com/geosmart/agent/tools/* geosmart-tools/src/test/java/com/geosmart/agent/tools/
fi
```

- [ ] **Step 3: 验证 Tools 模块构建**

```bash
cd backend/geosmart-tools
mvn clean compile
```

Expected: 编译成功

- [ ] **Step 4: 提交 Tools 模块迁移**

```bash
cd backend
git add geosmart-tools/src
git add -u src/main/java/com/geosmart/agent/tools
git add -u src/test/java/com/geosmart/agent/tools
git commit -m "refactor(tools): migrate Agent tools to separate Maven module

- Move RegulationSearchTool, SpatialQueryTool, BusinessStatusTool
- Move corresponding tests to geosmart-tools module

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 5: 迁移 RAG 模块

**Files:**
- Move: `src/main/java/com/geosmart/rag/DocumentIngestionService.java` → `geosmart-rag/src/main/java/com/geosmart/rag/`
- Move: `src/main/java/com/geosmart/rag/EmbeddingConfig.java` → `geosmart-rag/src/main/java/com/geosmart/rag/`
- Move: `src/main/java/com/geosmart/rag/RetrievalService.java` → `geosmart-rag/src/main/java/com/geosmart/rag/`
- Move: `src/test/java/com/geosmart/rag/*` → `geosmart-rag/src/test/java/com/geosmart/rag/`

- [ ] **Step 1: 移动 DocumentIngestionService.java**

```bash
cd backend
mv src/main/java/com/geosmart/rag/DocumentIngestionService.java geosmart-rag/src/main/java/com/geosmart/rag/
```

- [ ] **Step 2: 移动 EmbeddingConfig.java**

```bash
cd backend
mv src/main/java/com/geosmart/rag/EmbeddingConfig.java geosmart-rag/src/main/java/com/geosmart/rag/
```

- [ ] **Step 3: 移动 RetrievalService.java**

```bash
cd backend
mv src/main/java/com/geosmart/rag/RetrievalService.java geosmart-rag/src/main/java/com/geosmart/rag/
```

- [ ] **Step 4: 移动测试文件（如果有）**

```bash
cd backend
if [ -d "src/test/java/com/geosmart/rag" ]; then
    mv src/test/java/com/geosmart/rag/* geosmart-rag/src/test/java/com/geosmart/rag/
fi
```

- [ ] **Step 5: 验证 RAG 模块构建**

```bash
cd backend/geosmart-rag
mvn clean compile
```

Expected: 编译成功（依赖 llm 模块的 EmbeddingModel）

- [ ] **Step 6: 提交 RAG 模块迁移**

```bash
cd backend
git add geosmart-rag/src
git add -u src/main/java/com/geosmart/rag
git add -u src/test/java/com/geosmart/rag
git commit -m "refactor(rag): migrate RAG module to separate Maven module

- Move DocumentIngestionService, EmbeddingConfig, RetrievalService
- Add dependency on geosmart-llm module
- Move corresponding tests to geosmart-rag module

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 6: 迁移 API 模块 - 移动核心文件

**Files:**
- Move: `src/main/java/com/geosmart/GeoSmartApplication.java` → `geosmart-api/src/main/java/com/geosmart/`
- Move: `src/main/java/com/geosmart/config/AppConfig.java` → `geosmart-api/src/main/java/com/geosmart/config/`
- Move: `src/main/java/com/geosmart/chat/GeoSmartAssistant.java` → `geosmart-api/src/main/java/com/geosmart/chat/`
- Move: `src/main/java/com/geosmart/chat/ChatConfig.java` → `geosmart-api/src/main/java/com/geosmart/chat/`
- Move: `src/main/java/com/geosmart/api/*` → `geosmart-api/src/main/java/com/geosmart/api/`
- Move: `src/main/resources/*` → `geosmart-api/src/main/resources/`
- Move: `src/test/java/com/geosmart/*` → `geosmart-api/src/test/java/com/geosmart/`

- [ ] **Step 1: 移动启动类 GeoSmartApplication.java**

```bash
cd backend
mkdir -p geosmart-api/src/main/java/com/geosmart
mv src/main/java/com/geosmart/GeoSmartApplication.java geosmart-api/src/main/java/com/geosmart/
```

- [ ] **Step 2: 移动配置类 AppConfig.java**

```bash
cd backend
mkdir -p geosmart-api/src/main/java/com/geosmart/config
mv src/main/java/com/geosmart/config/AppConfig.java geosmart-api/src/main/java/com/geosmart/config/
```

- [ ] **Step 3: 移动 Chat 相关类到 API 模块**

```bash
cd backend
mkdir -p geosmart-api/src/main/java/com/geosmart/chat
mv src/main/java/com/geosmart/chat/GeoSmartAssistant.java geosmart-api/src/main/java/com/geosmart/chat/
mv src/main/java/com/geosmart/chat/ChatConfig.java geosmart-api/src/main/java/com/geosmart/chat/
```

- [ ] **Step 4: 移动 API Controller 类**

```bash
cd backend
mkdir -p geosmart-api/src/main/java/com/geosmart/api
mkdir -p geosmart-api/src/main/java/com/geosmart/api/dto
mv src/main/java/com/geosmart/api/ChatController.java geosmart-api/src/main/java/com/geosmart/api/
mv src/main/java/com/geosmart/api/DocumentController.java geosmart-api/src/main/java/com/geosmart/api/
mv src/main/java/com/geosmart/api/dto/ChatRequest.java geosmart-api/src/main/java/com/geosmart/api/dto/
```

- [ ] **Step 5: 移动资源文件**

```bash
cd backend
mv src/main/resources/* geosmart-api/src/main/resources/
```

- [ ] **Step 6: 移动测试文件**

```bash
cd backend
if [ -d "src/test/java/com/geosmart" ]; then
    cp -r src/test/java/com/geosmart/* geosmart-api/src/test/java/com/geosmart/
fi
```

- [ ] **Step 7: 删除旧的空目录**

```bash
cd backend
rm -rf src/main/java/com/geosmart/agent
rm -rf src/main/java/com/geosmart/llm
rm -rf src/main/java/com/geosmart/rag
rmdir src/main/java/com/geosmart 2>/dev/null || true
rmdir src/main/java/com 2>/dev/null || true
rmdir src/main/java 2>/dev/null || true
```

- [ ] **Step 8: 验证 API 模块构建**

```bash
cd backend/geosmart-api
mvn clean compile
```

Expected: 编译成功

- [ ] **Step 9: 验证父 POM 完整构建**

```bash
cd backend
mvn clean compile
```

Expected: 所有模块编译成功

- [ ] **Step 10: 提交 API 模块迁移**

```bash
cd backend
git add geosmart-api/src
git add -u src/
git commit -m "refactor(api): migrate API module to separate Maven module

- Move GeoSmartApplication, ChatConfig, GeoSmartAssistant to geosmart-api
- Move ChatController, DocumentController, DTOs to geosmart-api
- Move AppConfig and resources to geosmart-api
- Move all tests to geosmart-api
- Remove old empty source directories

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 7: 配置 Spring Boot 插件和主类

**Files:**
- Modify: `geosmart-api/pom.xml`

- [ ] **Step 1: 编辑 geosmart-api/pom.xml 确保包含正确的 Spring Boot 插件配置**

检查 `backend/geosmart-api/pom.xml` 中的 `<build>` 部分，确保包含：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <mainClass>com.geosmart.GeoSmartApplication</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

- [ ] **Step 2: 验证可执行 JAR 打包**

```bash
cd backend
mvn clean package -DskipTests
```

Expected: 生成 `geosmart-api/target/geosmart-api-0.1.0-SNAPSHOT.jar`

- [ ] **Step 3: 验证 JAR 可执行**

```bash
cd backend/geosmart-api
java -jar target/geosmart-api-0.1.0-SNAPSHOT.jar --spring.profiles.active=test &
PID=$!
sleep 5
kill $PID 2>/dev/null || true
```

Expected: 应用能够启动（注意：可能会因为缺少配置而失败，但应该能看到 Spring 启动日志）

- [ ] **Step 4: 如果需要，更新 pom.xml**

如果步骤 3 中启动失败是因为找不到主类，编辑 `backend/geosmart-api/pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <mainClass>com.geosmart.GeoSmartApplication</mainClass>
                <classifier>exec</classifier>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

- [ ] **Step 5: 提交配置**

```bash
cd backend
git add geosmart-api/pom.xml
git commit -m "fix(api): configure Spring Boot plugin with main class

- Ensure spring-boot-maven-plugin is properly configured
- Set mainClass to com.geosmart.GeoSmartApplication

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 8: 运行完整测试套件

**Files:**
- Test: All module tests

- [ ] **Step 1: 运行所有模块测试**

```bash
cd backend
mvn test
```

Expected: 所有测试通过

- [ ] **Step 2: 如果测试失败，查看失败原因并记录**

```bash
cd backend
mvn test 2>&1 | tee test-results.log
```

- [ ] **Step 3: 修复测试路径问题（如果有）**

如果有测试因为路径问题失败，检查并更新导入语句。所有模块的包名应保持 `com.geosmart.*` 不变。

- [ ] **Step 4: 重新运行测试**

```bash
cd backend
mvn test
```

Expected: 所有测试通过

- [ ] **Step 5: 提交测试修复**

```bash
cd backend
git add .
git commit -m "test: fix test imports after modularization

- Update import statements for modularized structure
- All tests passing

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 9: 验证应用启动和基本功能

**Files:**
- Verify: Application startup

- [ ] **Step 1: 启动应用**

```bash
cd backend/geosmart-api
mvn spring-boot:run &
PID=$!
echo $PID
```

Expected: 应用在 8080 端口启动

- [ ] **Step 2: 等待启动完成**

```bash
sleep 10
```

- [ ] **Step 3: 测试健康检查或文档列表接口**

```bash
curl http://localhost:8080/api/documents
```

Expected: 返回文档列表或空数组

- [ ] **Step 4: 测试聊天历史接口**

```bash
curl http://localhost:8080/api/chat/history/test-session
```

Expected: 返回会话信息

- [ ] **Step 5: 停止应用**

```bash
kill $PID 2>/dev/null || true
```

- [ ] **Step 6: 提交验证结果（如果有必要调整）**

```bash
cd backend
git add .
git commit -m "fix: adjust configuration for modularized application startup

- Update application.yml paths if needed
- Verify all endpoints work correctly

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 10: 最终验证和文档更新

**Files:**
- Update: CLAUDE.md, README.md

- [ ] **Step 1: 更新 CLAUDE.md 中的构建说明**

编辑 `backend/../CLAUDE.md`，更新构建命令部分：

```markdown
### 后端（在 `backend/` 目录下执行）

```bash
# 构建所有模块
mvn clean install

# 只构建特定模块
cd geosmart-llm && mvn clean install

# 运行所有测试
mvn test

# 运行单个模块测试
cd geosmart-api && mvn test

# 启动应用（必须从 geosmart-api 目录或 backend 目录）
cd geosmart-api && mvn spring-boot:run
# 或
cd backend && mvn -pl geosmart-api spring-boot:run

# 打包可执行 JAR
mvn clean package -DskipTests
java -jar geosmart-api/target/geosmart-api-0.1.0-SNAPSHOT.jar
```
```

- [ ] **Step 2: 更新 README.md（如果需要）**

编辑 `backend/../README.md`，添加模块说明部分：

```markdown
## 模块架构

GeoSmart-Agent 采用 Maven 多模块架构：

```
geosmart-agent (父 POM)
├── geosmart-llm    — LLM 提供商配置和切换
├── geosmart-rag    — 文档摄入和向量检索
├── geosmart-tools  — Agent 工具集合
├── geosmart-chat   — 会话记忆管理
└── geosmart-api    — REST API 和装配中心（可执行）
```

### 模块职责

| 模块 | 职责 |
|------|------|
| geosmart-llm | LLM 提供商切换（智谱、DeepSeek、OpenAI） |
| geosmart-rag | PDF/DOCX 文档解析、分块、向量化、检索 |
| geosmart-tools | 法规检索、空间查询、业务状态查询工具 |
| geosmart-chat | 多会话 ChatMemory 管理 |
| geosmart-api | SSE 聊天、文档上传、AI 服务装配 |
```

- [ ] **Step 3: 运行完整的构建验证**

```bash
cd backend
mvn clean install -DskipTests
mvn test
```

Expected: 构建成功，所有测试通过

- [ ] **Step 4: 验证可执行 JAR 生成**

```bash
cd backend
ls -lh geosmart-api/target/geosmart-api-0.1.0-SNAPSHOT.jar
```

Expected: 文件存在且大小合理

- [ ] **Step 5: 最终功能验证**

```bash
cd backend/geosmart-api
mvn spring-boot:run &
PID=$!
sleep 10

# 测试文档列表
curl http://localhost:8080/api/documents

# 测试会话管理
curl -X DELETE http://localhost:8080/api/chat/session/test

kill $PID
```

Expected: 所有接口正常响应

- [ ] **Step 6: 提交文档更新**

```bash
cd backend
git add ../CLAUDE.md ../README.md
git commit -m "docs: update build and module documentation

- Update CLAUDE.md with multi-module build commands
- Add module architecture description to README.md
- Document module responsibilities and dependencies

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

- [ ] **Step 7: 最终提交完成标记**

```bash
cd backend
git tag -a v0.1.0-modular -m "Backend modularization complete
- 5 Maven modules: llm, rag, tools, chat, api
- All tests passing
- Application verified to start and function correctly"
```

---

## 验收标准

所有任务完成后，验证以下标准：

### 构建验收
- [ ] `mvn clean install` 在 `backend/` 目录成功构建
- [ ] `mvn test` 所有测试通过
- [ ] `geosmart-api/target/geosmart-api-0.1.0-SNAPSHOT.jar` 可独立运行

### 功能验收
- [ ] 应用启动成功，端口 8080 响应
- [ ] GET `/api/documents` 返回文档列表
- [ ] POST `/api/chat` SSE 流式输出工作
- [ ] DELETE `/api/chat/session/{id}` 清除会话工作

### 代码质量验收
- [ ] 无循环依赖
- [ ] 每个模块职责单一
- [ ] 包名保持 `com.geosmart.*` 不变
