# GeoSmart-Agent MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an MVP intelligent assistant for land spatial planning with RAG-based policy consultation and Agent tool calling, using Spring Boot + LangChain4j backend and Vue 3 frontend.

**Architecture:** Modular monolith with clear package boundaries (api/chat/rag/agent/llm). LangChain4j AiServices orchestrates RAG retrieval and Agent tools via streaming SSE. Frontend consumes SSE for real-time chat display.

**Tech Stack:** Java 17, Spring Boot 3.5, LangChain4j 1.13.0, Vue 3, Vite, Element Plus, Pinia

---

## File Structure

### Backend Files

| File | Purpose |
|------|---------|
| `.gitignore` | Updated for Java/Spring/Vue |
| `backend/pom.xml` | Maven build with Spring Boot + LangChain4j deps |
| `backend/src/main/java/com/geosmart/GeoSmartApplication.java` | Spring Boot main class |
| `backend/src/main/java/com/geosmart/llm/LlmConfig.java` | Pluggable LLM provider beans |
| `backend/src/main/java/com/geosmart/llm/LlmProperties.java` | LLM config properties binding |
| `backend/src/main/java/com/geosmart/agent/tools/RegulationSearchTool.java` | @Tool for regulation search |
| `backend/src/main/java/com/geosmart/agent/tools/SpatialQueryTool.java` | @Tool for spatial info query |
| `backend/src/main/java/com/geosmart/agent/tools/BusinessStatusTool.java` | @Tool for business status query |
| `backend/src/main/java/com/geosmart/rag/EmbeddingConfig.java` | Embedding model + in-memory store beans |
| `backend/src/main/java/com/geosmart/rag/DocumentIngestionService.java` | Document loading, parsing, chunking |
| `backend/src/main/java/com/geosmart/rag/RetrievalService.java` | Content retriever for RAG |
| `backend/src/main/java/com/geosmart/chat/GeoSmartAssistant.java` | AiService interface |
| `backend/src/main/java/com/geosmart/chat/ChatSessionManager.java` | Per-session ChatMemory |
| `backend/src/main/java/com/geosmart/chat/ChatConfig.java` | Wires AiServices with tools + RAG |
| `backend/src/main/java/com/geosmart/api/dto/ChatRequest.java` | Chat request DTO |
| `backend/src/main/java/com/geosmart/api/ChatController.java` | SSE chat endpoint |
| `backend/src/main/java/com/geosmart/api/DocumentController.java` | Document upload endpoint |
| `backend/src/main/resources/application.yml` | Main config |
| `backend/src/main/resources/application-dev.yml` | Dev profile overrides |
| `backend/src/main/resources/sample-docs/land-use-regulation.txt` | Sample regulation document |

### Backend Test Files

| File | Purpose |
|------|---------|
| `backend/src/test/java/com/geosmart/llm/LlmConfigTest.java` | Verify LLM beans load |
| `backend/src/test/java/com/geosmart/agent/tools/RegulationSearchToolTest.java` | Tool unit test |
| `backend/src/test/java/com/geosmart/agent/tools/SpatialQueryToolTest.java` | Tool unit test |
| `backend/src/test/java/com/geosmart/agent/tools/BusinessStatusToolTest.java` | Tool unit test |
| `backend/src/test/java/com/geosmart/rag/DocumentIngestionServiceTest.java` | Ingestion smoke test |
| `backend/src/test/java/com/geosmart/api/ChatControllerTest.java` | Controller test |

### Frontend Files

| File | Purpose |
|------|---------|
| `frontend/package.json` | npm deps |
| `frontend/vite.config.ts` | Vite config with proxy |
| `frontend/tsconfig.json` | TypeScript config |
| `frontend/tsconfig.app.json` | App TypeScript config |
| `frontend/index.html` | Entry HTML |
| `frontend/src/main.ts` | App bootstrap |
| `frontend/src/App.vue` | Root component |
| `frontend/src/types/index.ts` | TypeScript type definitions |
| `frontend/src/api/chat.ts` | API client (fetch SSE) |
| `frontend/src/stores/chat.ts` | Pinia chat store |
| `frontend/src/views/ChatView.vue` | Main chat page |
| `frontend/src/components/SessionList.vue` | Session sidebar |
| `frontend/src/components/MessageList.vue` | Message list with Markdown |
| `frontend/src/components/MessageInput.vue` | Input box + send |
| `frontend/src/components/ToolCallDisplay.vue` | Tool call display card |
| `frontend/src/components/DocUpload.vue` | Document upload |

---

## Task 1: Backend Project Foundation

**Files:**
- Modify: `.gitignore`
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/geosmart/GeoSmartApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/application-dev.yml`

- [ ] **Step 1: Update .gitignore for Java/Spring/Vue**

Replace the entire `.gitignore` with:

```gitignore
# Java
*.class
*.jar
*.war
*.ear
*.log
target/
!.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iml
.vscode/
.settings/
.project
.classpath

# OS
.DS_Store
Thumbs.db

# Environment
.env
*.env.local

# Node / Frontend
node_modules/
dist/
.vite/

# Logs
logs/
```

- [ ] **Step 2: Create backend directory structure**

Run:
```bash
cd D:/Projects/GeoSmart-Agent
mkdir -p backend/src/main/java/com/geosmart
mkdir -p backend/src/main/resources/sample-docs
mkdir -p backend/src/test/java/com/geosmart
```

- [ ] **Step 3: Create pom.xml**

Create `backend/pom.xml`:

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
    <name>GeoSmart Agent</name>
    <description>Land Spatial Planning Intelligent Assistant</description>

    <properties>
        <java.version>17</java.version>
        <langchain4j.version>1.13.0</langchain4j.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
            <version>${langchain4j.version}</version>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-open-ai</artifactId>
            <version>${langchain4j.version}</version>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-embeddings-all-minilm-l6-v2</artifactId>
            <version>1.12.2-beta22</version>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-document-parser-apache-pdfbox</artifactId>
            <version>${langchain4j.version}</version>
        </dependency>

        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-document-parser-apache-poi</artifactId>
            <version>${langchain4j.version}</version>
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
    </build>
</project>
```

- [ ] **Step 4: Create GeoSmartApplication.java**

Create `backend/src/main/java/com/geosmart/GeoSmartApplication.java`:

```java
package com.geosmart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GeoSmartApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeoSmartApplication.class, args);
    }
}
```

- [ ] **Step 5: Create application.yml**

Create `backend/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

llm:
  provider: ${LLM_PROVIDER:deepseek}
  deepseek:
    base-url: https://api.deepseek.com
    api-key: ${DEEPSEEK_API_KEY:your-api-key-here}
    model-name: deepseek-chat
  openai:
    base-url: https://api.openai.com
    api-key: ${OPENAI_API_KEY:your-api-key-here}
    model-name: gpt-4o

rag:
  chunk-size: 500
  chunk-overlap: 100
  max-results: 5

chat:
  max-memory-messages: 20

logging:
  level:
    dev.langchain4j: DEBUG
    com.geosmart: DEBUG
```

- [ ] **Step 6: Create application-dev.yml**

Create `backend/src/main/resources/application-dev.yml`:

```yaml
llm:
  provider: deepseek
  deepseek:
    api-key: ${DEEPSEEK_API_KEY:sk-your-deepseek-key}
```

- [ ] **Step 7: Verify compilation**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn compile
```
Expected: `BUILD SUCCESS`

- [ ] **Step 8: Commit**

```bash
cd D:/Projects/GeoSmart-Agent
git add .gitignore backend/
git commit -m "feat: initialize Spring Boot backend with LangChain4j dependencies"
```

---

## Task 2: LLM Configuration Layer

**Files:**
- Create: `backend/src/main/java/com/geosmart/llm/LlmConfig.java`
- Create: `backend/src/main/java/com/geosmart/llm/LlmProperties.java`
- Create: `backend/src/test/java/com/geosmart/llm/LlmConfigTest.java`

- [ ] **Step 1: Write the failing test**

Create `backend/src/test/java/com/geosmart/llm/LlmConfigTest.java`:

```java
package com.geosmart.llm;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class LlmConfigTest {

    @Autowired
    private ChatLanguageModel chatModel;

    @Autowired
    private StreamingChatModel streamingChatModel;

    @Test
    void chatModelBeanShouldBeCreated() {
        assertThat(chatModel).isNotNull();
    }

    @Test
    void streamingChatModelBeanShouldBeCreated() {
        assertThat(streamingChatModel).isNotNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn test -Dtest=LlmConfigTest
```
Expected: FAIL — `ChatLanguageModel` bean not found (no configuration class yet)

- [ ] **Step 3: Create LlmProperties**

Create `backend/src/main/java/com/geosmart/llm/LlmProperties.java`:

```java
package com.geosmart.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    private String provider = "deepseek";
    private ProviderConfig deepseek = new ProviderConfig();
    private ProviderConfig openai = new ProviderConfig();

    public ProviderConfig getActiveConfig() {
        return switch (provider.toLowerCase()) {
            case "openai" -> openai;
            default -> deepseek;
        };
    }

    @Data
    public static class ProviderConfig {
        private String baseUrl = "https://api.deepseek.com";
        private String apiKey = "your-api-key-here";
        private String modelName = "deepseek-chat";
    }
}
```

- [ ] **Step 4: Create LlmConfig**

Create `backend/src/main/java/com/geosmart/llm/LlmConfig.java`:

```java
package com.geosmart.llm;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LlmConfig {

    @Bean
    public ChatLanguageModel chatModel(LlmProperties properties) {
        LlmProperties.ProviderConfig config = properties.getActiveConfig();
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel(LlmProperties properties) {
        LlmProperties.ProviderConfig config = properties.getActiveConfig();
        return OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .timeout(Duration.ofSeconds(120))
                .build();
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn test -Dtest=LlmConfigTest
```
Expected: PASS — both beans created

- [ ] **Step 6: Commit**

```bash
cd D:/Projects/GeoSmart-Agent
git add backend/src/main/java/com/geosmart/llm/ backend/src/test/java/com/geosmart/llm/
git commit -m "feat: add pluggable LLM configuration layer (DeepSeek/OpenAI)"
```

---

## Task 3: Agent Tools (Mock Data)

**Files:**
- Create: `backend/src/main/java/com/geosmart/agent/tools/RegulationSearchTool.java`
- Create: `backend/src/main/java/com/geosmart/agent/tools/SpatialQueryTool.java`
- Create: `backend/src/main/java/com/geosmart/agent/tools/BusinessStatusTool.java`
- Create: `backend/src/test/java/com/geosmart/agent/tools/RegulationSearchToolTest.java`
- Create: `backend/src/test/java/com/geosmart/agent/tools/SpatialQueryToolTest.java`
- Create: `backend/src/test/java/com/geosmart/agent/tools/BusinessStatusToolTest.java`

- [ ] **Step 1: Write the failing test for RegulationSearchTool**

Create `backend/src/test/java/com/geosmart/agent/tools/RegulationSearchToolTest.java`:

```java
package com.geosmart.agent.tools;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegulationSearchToolTest {

    @Test
    void shouldReturnRegulationResultsForKeyword() {
        RegulationSearchTool tool = new RegulationSearchTool();
        String result = tool.searchRegulations("国土空间规划");
        assertThat(result).contains("国土空间规划");
    }

    @Test
    void shouldReturnNonEmptyResult() {
        RegulationSearchTool tool = new RegulationSearchTool();
        String result = tool.searchRegulations("用地审批");
        assertThat(result).isNotBlank();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn test -Dtest=RegulationSearchToolTest
```
Expected: FAIL — class not found

- [ ] **Step 3: Implement RegulationSearchTool**

Create `backend/src/main/java/com/geosmart/agent/tools/RegulationSearchTool.java`:

```java
package com.geosmart.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class RegulationSearchTool {

    @Tool("根据关键词检索国土空间规划相关法规政策文件，返回匹配的法规名称、文号和摘要")
    public String searchRegulations(@P("搜索关键词，如：国土空间规划、用地审批、生态红线") String keyword) {
        return """
                {
                  "total": 2,
                  "regulations": [
                    {
                      "name": "《国土空间规划法》",
                      "docNumber": "国发〔2024〕XX号",
                      "summary": "规定了国土空间规划的编制、审批、实施和监督管理，明确了各类空间用途管制要求。与「%s」高度相关。",
                      "effectiveDate": "2024-01-01"
                    },
                    {
                      "name": "《建设用地审批管理办法》",
                      "docNumber": "自然资发〔2023〕XX号",
                      "summary": "规范了建设用地的申请、审查、批准程序，涉及农用地转用、土地征收等事项。包含「%s」相关条款。",
                      "effectiveDate": "2023-06-01"
                    }
                  ]
                }
                """.formatted(keyword, keyword);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn test -Dtest=RegulationSearchToolTest
```
Expected: PASS

- [ ] **Step 5: Write failing test for SpatialQueryTool**

Create `backend/src/test/java/com/geosmart/agent/tools/SpatialQueryToolTest.java`:

```java
package com.geosmart.agent.tools;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpatialQueryToolTest {

    @Test
    void shouldReturnSpatialInfoForLocation() {
        SpatialQueryTool tool = new SpatialQueryTool();
        String result = tool.querySpatialInfo("滨江新城");
        assertThat(result).contains("滨江新城");
    }

    @Test
    void shouldContainLandUseInfo() {
        SpatialQueryTool tool = new SpatialQueryTool();
        String result = tool.querySpatialInfo("A-01地块");
        assertThat(result).contains("规划用途");
    }
}
```

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn test -Dtest=SpatialQueryToolTest
```
Expected: FAIL — class not found

- [ ] **Step 6: Implement SpatialQueryTool**

Create `backend/src/main/java/com/geosmart/agent/tools/SpatialQueryTool.java`:

```java
package com.geosmart.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class SpatialQueryTool {

    @Tool("查询指定区域或地块的空间规划信息，包括土地性质、规划用途、红线范围等")
    public String querySpatialInfo(@P("区域名称或地块编号，如：滨江新城、A-01地块") String location) {
        return """
                {
                  "location": "%s",
                  "spatialInfo": {
                    "landUseType": "城镇建设用地",
                    "planningPurpose": "商住混合用地",
                    "redlineStatus": "未涉及生态红线",
                    "zoning": "城市更新改造区",
                    "floorAreaRatio": "≤3.5",
                    "buildingDensity": "≤35%%",
                    "greeningRate": "≥30%%",
                    "restrictions": ["需进行环境影响评价", "符合城市总体规划要求"]
                  }
                }
                """.formatted(location);
    }
}
```

- [ ] **Step 7: Run SpatialQueryTool test**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn test -Dtest=SpatialQueryToolTest
```
Expected: PASS

- [ ] **Step 8: Write failing test for BusinessStatusTool**

Create `backend/src/test/java/com/geosmart/agent/tools/BusinessStatusToolTest.java`:

```java
package com.geosmart.agent.tools;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessStatusToolTest {

    @Test
    void shouldReturnBusinessStatus() {
        BusinessStatusTool tool = new BusinessStatusTool();
        String result = tool.queryBusinessStatus("GH-2024-001");
        assertThat(result).contains("GH-2024-001");
    }

    @Test
    void shouldContainStatusField() {
        BusinessStatusTool tool = new BusinessStatusTool();
        String result = tool.queryBusinessStatus("GH-2024-001");
        assertThat(result).contains("status");
    }
}
```

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn test -Dtest=BusinessStatusToolTest
```
Expected: FAIL — class not found

- [ ] **Step 9: Implement BusinessStatusTool**

Create `backend/src/main/java/com/geosmart/agent/tools/BusinessStatusTool.java`:

```java
package com.geosmart.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class BusinessStatusTool {

    @Tool("查询业务办理状态，如规划许可证、用地审批、建设工程规划许可等")
    public String queryBusinessStatus(@P("业务编号，如：GH-2024-001") String businessId) {
        return """
                {
                  "businessId": "%s",
                  "businessType": "建设工程规划许可证",
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
```

- [ ] **Step 10: Run BusinessStatusTool test**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn test -Dtest=BusinessStatusToolTest
```
Expected: PASS

- [ ] **Step 11: Run all tool tests together**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn test -Dtest="RegulationSearchToolTest,SpatialQueryToolTest,BusinessStatusToolTest"
```
Expected: All PASS

- [ ] **Step 12: Commit**

```bash
cd D:/Projects/GeoSmart-Agent
git add backend/src/main/java/com/geosmart/agent/ backend/src/test/java/com/geosmart/agent/
git commit -m "feat: add three Agent tools with mock data (regulation, spatial, business)"
```

---

## Task 4: RAG Pipeline

**Files:**
- Create: `backend/src/main/java/com/geosmart/rag/EmbeddingConfig.java`
- Create: `backend/src/main/java/com/geosmart/rag/DocumentIngestionService.java`
- Create: `backend/src/main/java/com/geosmart/rag/RetrievalService.java`
- Create: `backend/src/main/resources/sample-docs/land-use-regulation.txt`
- Create: `backend/src/test/java/com/geosmart/rag/DocumentIngestionServiceTest.java`

- [ ] **Step 1: Create sample regulation document**

Create `backend/src/main/resources/sample-docs/land-use-regulation.txt`:

```text
国土空间规划管理办法

第一章 总则

第一条 为了规范国土空间规划管理活动，优化国土空间开发保护格局，促进经济社会高质量发展，根据《中华人民共和国土地管理法》《中华人民共和国城乡规划法》等法律法规，制定本办法。

第二条 本办法适用于国土空间规划的编制、审批、实施、修改和监督管理等活动。

第三条 国土空间规划包括总体规划、详细规划和相关专项规划。总体规划是对一定区域范围内的国土空间开发保护作出的空间部署。详细规划是对总体规划的具体落实和细化。专项规划是对特定领域（如交通、水利、生态保护等）的空间安排。

第二章 规划编制

第四条 编制国土空间规划应当遵循以下原则：
（一）坚持生态优先、绿色发展；
（二）坚持以人民为中心，保障公共利益；
（三）坚持节约集约用地，提高土地利用效率；
（四）坚持区域协调发展，统筹城乡空间布局；
（五）坚持因地制宜，突出地方特色。

第五条 国土空间总体规划应当包括以下内容：
（一）规划目标和战略定位；
（二）国土空间开发保护总体格局；
（三）生态保护红线、永久基本农田、城镇开发边界；
（四）国土空间用途分区和管制规则；
（五）重大基础设施和公共服务设施布局。

第三章 用地审批

第六条 建设用地审批应当符合国土空间规划要求，未经批准不得改变土地用途。

第七条 农用地转为建设用地的，应当依照下列程序办理：
（一）由市、县人民政府提出农用地转用方案；
（二）按照法定权限报有批准权的人民政府批准；
（三）依法办理土地征收手续（涉及集体土地的）；
（四）核发建设用地批准书。

第八条 生态红线范围内禁止下列活动：
（一）工业项目建设；
（二）矿产资源开发；
（三）大规模农业开发；
（四）其他破坏生态环境的活动。

第四章 监督管理

第九条 县级以上人民政府自然资源主管部门应当建立国土空间规划实施监测评估预警机制，定期对规划实施情况进行评估。

第十条 任何单位和个人有权向自然资源主管部门举报违反国土空间规划的行为。接到举报的部门应当及时核查处理。
```

- [ ] **Step 2: Write the failing test for DocumentIngestionService**

Create `backend/src/test/java/com/geosmart/rag/DocumentIngestionServiceTest.java`:

```java
package com.geosmart.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatNoException;

class DocumentIngestionServiceTest {

    private DocumentIngestionService service;

    @BeforeEach
    void setUp() {
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        service = new DocumentIngestionService(store, embeddingModel, 500, 100);
    }

    @Test
    void shouldIngestTextDocumentWithoutError(@TempDir Path tempDir) throws Exception {
        Path docPath = tempDir.resolve("test-regulation.txt");
        java.nio.file.Files.writeString(docPath, """
                国土空间规划管理办法
                第一条 为了规范国土空间规划管理，制定本办法。
                第二条 国土空间规划包括总体规划和详细规划。
                """);

        assertThatNoException().isThrownBy(() -> service.ingestDocument(docPath));
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn test -Dtest=DocumentIngestionServiceTest
```
Expected: FAIL — class not found

- [ ] **Step 4: Create EmbeddingConfig**

Create `backend/src/main/java/com/geosmart/rag/EmbeddingConfig.java`:

```java
package com.geosmart.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
}
```

- [ ] **Step 5: Create DocumentIngestionService**

Create `backend/src/main/java/com/geosmart/rag/DocumentIngestionService.java`:

```java
package com.geosmart.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final EmbeddingStoreIngestor ingestor;

    public DocumentIngestionService(EmbeddingStore<TextSegment> store,
                                    EmbeddingModel embeddingModel,
                                    int chunkSize, int chunkOverlap) {
        this.ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(DocumentSplitters.recursive(chunkSize, chunkOverlap))
                .build();
    }

    public void ingestDocument(Path documentPath) {
        String fileName = documentPath.getFileName().toString();
        DocumentParser parser = resolveParser(fileName);
        Document document = FileSystemDocumentLoader.loadDocument(documentPath, parser);
        ingestor.ingest(document);
        log.info("Ingested document: {} ({} chars)", fileName, document.text().length());
    }

    private DocumentParser resolveParser(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return new ApachePdfBoxDocumentParser();
        } else if (fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
            return new ApachePoiDocumentParser();
        } else {
            return new TextDocumentParser();
        }
    }
}
```

- [ ] **Step 6: Run DocumentIngestionServiceTest**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn test -Dtest=DocumentIngestionServiceTest
```
Expected: PASS — document ingested without error (first run downloads ~23MB ONNX model)

- [ ] **Step 7: Create RetrievalService**

Create `backend/src/main/java/com/geosmart/rag/RetrievalService.java`:

```java
package com.geosmart.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RetrievalService {

    private final ContentRetriever contentRetriever;

    public RetrievalService(EmbeddingStore<TextSegment> store,
                            EmbeddingModel embeddingModel,
                            @Value("${rag.max-results:5}") int maxResults) {
        this.contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .build();
    }

    public ContentRetriever getContentRetriever() {
        return contentRetriever;
    }
}
```

- [ ] **Step 8: Commit**

```bash
cd D:/Projects/GeoSmart-Agent
git add backend/src/main/java/com/geosmart/rag/ backend/src/main/resources/sample-docs/ backend/src/test/java/com/geosmart/rag/
git commit -m "feat: add RAG pipeline with in-memory embeddings and document ingestion"
```

---

## Task 5: Chat Assistant + API Endpoints

**Files:**
- Create: `backend/src/main/java/com/geosmart/chat/GeoSmartAssistant.java`
- Create: `backend/src/main/java/com/geosmart/chat/ChatSessionManager.java`
- Create: `backend/src/main/java/com/geosmart/chat/ChatConfig.java`
- Create: `backend/src/main/java/com/geosmart/api/dto/ChatRequest.java`
- Create: `backend/src/main/java/com/geosmart/api/ChatController.java`
- Create: `backend/src/main/java/com/geosmart/api/DocumentController.java`
- Create: `backend/src/test/java/com/geosmart/api/ChatControllerTest.java`

- [ ] **Step 1: Create GeoSmartAssistant interface**

Create `backend/src/main/java/com/geosmart/chat/GeoSmartAssistant.java`:

```java
package com.geosmart.chat;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface GeoSmartAssistant {

    @SystemMessage("""
            你是「国土空间规划智能助手」，专门帮助用户解答国土空间规划相关的政策法规问题，
            以及查询土地空间信息和业务办理状态。

            你的职责：
            1. 政策法规咨询：回答关于国土空间规划法律法规的问题，引用政策时需注明文件名称。
            2. 空间信息查询：查询指定区域的土地性质、规划用途、红线范围等信息。
            3. 业务办理查询：查询规划许可、用地审批等业务的办理进度。

            回答要求：
            - 专业准确，引用政策文件需注明文件名称和文号。
            - 如果不确定，请如实告知，不要编造信息。
            - 使用清晰、简洁的中文回答。
            """)
    TokenStream chat(@UserMessage String message, @MemoryId String sessionId);
}
```

- [ ] **Step 2: Create ChatSessionManager**

Create `backend/src/main/java/com/geosmart/chat/ChatSessionManager.java`:

```java
package com.geosmart.chat;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSessionManager {

    private final int maxMessages;
    private final Map<String, ChatMemory> sessions = new ConcurrentHashMap<>();

    public ChatSessionManager(@Value("${chat.max-memory-messages:20}") int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public ChatMemory getOrCreate(String sessionId) {
        return sessions.computeIfAbsent(sessionId,
                id -> MessageWindowChatMemory.withMaxMessages(maxMessages));
    }

    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public Map<String, ChatMemory> getAllSessions() {
        return sessions;
    }
}
```

- [ ] **Step 3: Create ChatConfig to wire everything**

Create `backend/src/main/java/com/geosmart/chat/ChatConfig.java`:

```java
package com.geosmart.chat;

import com.geosmart.agent.tools.BusinessStatusTool;
import com.geosmart.agent.tools.RegulationSearchTool;
import com.geosmart.agent.tools.SpatialQueryTool;
import com.geosmart.rag.RetrievalService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Bean
    public GeoSmartAssistant geoSmartAssistant(
            ChatLanguageModel chatModel,
            StreamingChatModel streamingChatModel,
            RetrievalService retrievalService,
            ChatSessionManager sessionManager,
            RegulationSearchTool regulationSearchTool,
            SpatialQueryTool spatialQueryTool,
            BusinessStatusTool businessStatusTool) {

        return AiServices.builder(GeoSmartAssistant.class)
                .chatLanguageModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .contentRetriever(retrievalService.getContentRetriever())
                .chatMemoryProvider(memoryId -> {
                    String sessionId = memoryId.toString();
                    return sessionManager.getOrCreate(sessionId);
                })
                .tools(regulationSearchTool, spatialQueryTool, businessStatusTool)
                .build();
    }
}
```

- [ ] **Step 4: Create ChatRequest DTO**

Create `backend/src/main/java/com/geosmart/api/dto/ChatRequest.java`:

```java
package com.geosmart.api.dto;

public record ChatRequest(String message, String sessionId) {
}
```

- [ ] **Step 5: Create ChatController with SSE**

Create `backend/src/main/java/com/geosmart/api/ChatController.java`:

```java
package com.geosmart.api;

import com.geosmart.api.dto.ChatRequest;
import com.geosmart.chat.ChatSessionManager;
import com.geosmart.chat.GeoSmartAssistant;
import dev.langchain4j.service.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final GeoSmartAssistant assistant;
    private final ChatSessionManager sessionManager;

    public ChatController(GeoSmartAssistant assistant, ChatSessionManager sessionManager) {
        this.assistant = assistant;
        this.sessionManager = sessionManager;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        String sessionId = request.sessionId() != null ? request.sessionId() : "default";

        executor.execute(() -> {
            try {
                TokenStream tokenStream = assistant.chat(request.message(), sessionId);
                tokenStream
                        .onNext(token -> {
                            try {
                                emitter.send(SseEmitter.event().data(token));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        })
                        .onComplete(response -> emitter.complete())
                        .onError(error -> {
                            log.error("Chat streaming error", error);
                            emitter.completeWithError(error);
                        })
                        .start();
            } catch (Exception e) {
                log.error("Failed to start chat stream", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping("/history/{sessionId}")
    public Map<String, Object> getHistory(@PathVariable String sessionId) {
        return Map.of("sessionId", sessionId);
    }

    @DeleteMapping("/session/{sessionId}")
    public Map<String, String> clearSession(@PathVariable String sessionId) {
        sessionManager.clearSession(sessionId);
        return Map.of("status", "cleared", "sessionId", sessionId);
    }
}
```

- [ ] **Step 6: Create DocumentController**

Create `backend/src/main/java/com/geosmart/api/DocumentController.java`:

```java
package com.geosmart.api;

import com.geosmart.rag.DocumentIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentIngestionService ingestionService;
    private final List<String> uploadedDocuments = new ArrayList<>();

    public DocumentController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(
            @RequestParam("file") MultipartFile file) {
        try {
            Path tempFile = Files.createTempFile("doc-", file.getOriginalFilename());
            file.transferTo(tempFile.toFile());
            ingestionService.ingestDocument(tempFile);
            Files.deleteIfExists(tempFile);

            uploadedDocuments.add(file.getOriginalFilename());
            log.info("Uploaded and ingested: {}", file.getOriginalFilename());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "filename", file.getOriginalFilename()
            ));
        } catch (Exception e) {
            log.error("Failed to upload document", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listDocuments() {
        return ResponseEntity.ok(Map.of("documents", uploadedDocuments));
    }
}
```

- [ ] **Step 7: Write ChatController test**

Create `backend/src/test/java/com/geosmart/api/ChatControllerTest.java`:

```java
package com.geosmart.api;

import com.geosmart.chat.ChatSessionManager;
import com.geosmart.chat.GeoSmartAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@ActiveProfiles("dev")
@Import(ChatSessionManager.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeoSmartAssistant assistant;

    @Test
    void shouldReturnHistoryForSession() throws Exception {
        mockMvc.perform(get("/api/chat/history/test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("test-session"));
    }
}
```

- [ ] **Step 8: Run controller test**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn test -Dtest=ChatControllerTest
```
Expected: PASS (history endpoint works; chat endpoint needs running LLM)

- [ ] **Step 9: Commit**

```bash
cd D:/Projects/GeoSmart-Agent
git add backend/src/main/java/com/geosmart/chat/ backend/src/main/java/com/geosmart/api/ backend/src/test/java/com/geosmart/api/
git commit -m "feat: add chat assistant with SSE streaming, session management, and API endpoints"
```

---

## Task 6: Frontend Foundation

**Files:**
- Create: `frontend/` (entire Vue 3 + Vite project)
- Create: `frontend/src/types/index.ts`
- Create: `frontend/src/api/chat.ts`
- Create: `frontend/src/stores/chat.ts`

- [ ] **Step 1: Initialize Vue 3 project**

Run:
```bash
cd D:/Projects/GeoSmart-Agent
npm create vue@latest frontend -- --typescript --router --pinia
cd frontend && npm install
```

- [ ] **Step 2: Install additional dependencies**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/frontend
npm install element-plus markdown-it highlight.js @types/markdown-it
```

- [ ] **Step 3: Configure Vite proxy to backend**

Replace `frontend/vite.config.ts`:

```typescript
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 4: Create TypeScript types**

Create `frontend/src/types/index.ts`:

```typescript
export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
  isStreaming?: boolean
  toolCalls?: ToolCallInfo[]
}

export interface ToolCallInfo {
  name: string
  args: Record<string, string>
  result?: string
}

export interface ChatSession {
  id: string
  title: string
  createdAt: number
  lastMessageAt: number
}

export interface ChatRequest {
  message: string
  sessionId: string
}
```

- [ ] **Step 5: Create API client**

Create `frontend/src/api/chat.ts`:

```typescript
import type { ChatRequest } from '@/types'

export function streamChat(
  request: ChatRequest,
  onToken: (token: string) => void,
  onComplete: () => void,
  onError: (error: Error) => void
): AbortController {
  const controller = new AbortController()

  fetch('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
    signal: controller.signal
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      const reader = response.body!.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.substring(5)
            if (data.trim() === '[DONE]') {
              onComplete()
            } else {
              onToken(data)
            }
          }
        }
      }
      onComplete()
    })
    .catch((err) => {
      if (err.name !== 'AbortError') {
        onError(err)
      }
    })

  return controller
}

export async function uploadDocument(file: File): Promise<{ status: string; filename: string }> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch('/api/documents/upload', {
    method: 'POST',
    body: formData
  })

  if (!response.ok) {
    throw new Error(`Upload failed: ${response.statusText}`)
  }

  return response.json()
}

export async function listDocuments(): Promise<{ documents: string[] }> {
  const response = await fetch('/api/documents')
  return response.json()
}
```

- [ ] **Step 6: Create Pinia chat store**

Create `frontend/src/stores/chat.ts`:

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ChatMessage, ChatSession } from '@/types'
import { streamChat } from '@/api/chat'

function generateId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substring(2)
}

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<ChatSession[]>([])
  const currentSessionId = ref<string>('')
  const messages = ref<Map<string, ChatMessage[]>>(new Map())
  const isLoading = ref(false)
  let abortController: AbortController | null = null

  const currentMessages = computed(() => {
    return messages.value.get(currentSessionId.value) || []
  })

  function createSession(): string {
    const id = generateId()
    const session: ChatSession = {
      id,
      title: '新对话',
      createdAt: Date.now(),
      lastMessageAt: Date.now()
    }
    sessions.value.unshift(session)
    messages.value.set(id, [])
    currentSessionId.value = id
    return id
  }

  function selectSession(id: string) {
    currentSessionId.value = id
  }

  function sendMessage(content: string) {
    if (!currentSessionId.value) {
      createSession()
    }

    const sessionId = currentSessionId.value
    const userMsg: ChatMessage = {
      id: generateId(),
      role: 'user',
      content,
      timestamp: Date.now()
    }

    const current = messages.value.get(sessionId) || []
    messages.value.set(sessionId, [...current, userMsg])

    // Update session title from first message
    const session = sessions.value.find((s) => s.id === sessionId)
    if (session && current.length === 0) {
      session.title = content.substring(0, 20) + (content.length > 20 ? '...' : '')
    }

    // Add assistant placeholder
    const assistantMsg: ChatMessage = {
      id: generateId(),
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      isStreaming: true
    }
    messages.value.set(sessionId, [...(messages.value.get(sessionId) || []), assistantMsg])
    isLoading.value = true

    abortController = streamChat(
      { message: content, sessionId },
      (token) => {
        const msgs = messages.value.get(sessionId) || []
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant') {
          last.content += token
          messages.value.set(sessionId, [...msgs])
        }
      },
      () => {
        const msgs = messages.value.get(sessionId) || []
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant') {
          last.isStreaming = false
          messages.value.set(sessionId, [...msgs])
        }
        isLoading.value = false
      },
      (error) => {
        const msgs = messages.value.get(sessionId) || []
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant') {
          last.content = `错误: ${error.message}`
          last.isStreaming = false
          messages.value.set(sessionId, [...msgs])
        }
        isLoading.value = false
      }
    )
  }

  function stopStreaming() {
    abortController?.abort()
    isLoading.value = false
  }

  return {
    sessions,
    currentSessionId,
    messages,
    isLoading,
    currentMessages,
    createSession,
    selectSession,
    sendMessage,
    stopStreaming
  }
})
```

- [ ] **Step 7: Verify frontend builds**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/frontend && npm run build
```
Expected: Build succeeds with no errors

- [ ] **Step 8: Commit**

```bash
cd D:/Projects/GeoSmart-Agent
git add frontend/
git commit -m "feat: initialize Vue 3 frontend with Vite, Element Plus, Pinia, and API client"
```

---

## Task 7: Chat UI + Integration

**Files:**
- Modify: `frontend/src/main.ts`
- Modify: `frontend/src/App.vue`
- Create: `frontend/src/views/ChatView.vue`
- Create: `frontend/src/components/SessionList.vue`
- Create: `frontend/src/components/MessageList.vue`
- Create: `frontend/src/components/MessageInput.vue`
- Create: `frontend/src/components/DocUpload.vue`

- [ ] **Step 1: Update main.ts with Element Plus**

Replace `frontend/src/main.ts`:

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
```

- [ ] **Step 2: Update App.vue**

Replace `frontend/src/App.vue`:

```vue
<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>
```

- [ ] **Step 3: Create ChatView.vue (main layout)**

Create `frontend/src/views/ChatView.vue`:

```vue
<template>
  <el-container class="chat-container">
    <el-aside width="260px" class="sidebar">
      <SessionList />
    </el-aside>
    <el-container>
      <el-header class="chat-header">
        <h2>GeoSmart 智能助手</h2>
      </el-header>
      <el-main class="chat-main">
        <MessageList />
      </el-main>
      <el-footer height="auto" class="chat-footer">
        <DocUpload />
        <MessageInput />
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import SessionList from '@/components/SessionList.vue'
import MessageList from '@/components/MessageList.vue'
import MessageInput from '@/components/MessageInput.vue'
import DocUpload from '@/components/DocUpload.vue'
</script>

<style scoped>
.chat-container {
  height: 100vh;
}

.sidebar {
  border-right: 1px solid #e4e7ed;
  background-color: #f5f7fa;
}

.chat-header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fff;
}

.chat-header h2 {
  font-size: 18px;
  color: #303133;
}

.chat-main {
  padding: 20px;
  overflow-y: auto;
  background-color: #fafafa;
}

.chat-footer {
  padding: 12px 20px;
  border-top: 1px solid #e4e7ed;
  background-color: #fff;
}
</style>
```

- [ ] **Step 4: Update router**

Find and replace the router index file (e.g., `frontend/src/router/index.ts`) to use ChatView:

```typescript
import { createRouter, createWebHistory } from 'vue-router'
import ChatView from '@/views/ChatView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'chat',
      component: ChatView
    }
  ]
})

export default router
```

- [ ] **Step 5: Create SessionList.vue**

Create `frontend/src/components/SessionList.vue`:

```vue
<template>
  <div class="session-list">
    <div class="session-header">
      <span>历史对话</span>
      <el-button type="primary" size="small" @click="chatStore.createSession()">
        新建
      </el-button>
    </div>
    <div class="session-items">
      <div
        v-for="session in chatStore.sessions"
        :key="session.id"
        :class="['session-item', { active: session.id === chatStore.currentSessionId }]"
        @click="chatStore.selectSession(session.id)"
      >
        <span class="session-title">{{ session.title }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()

// Ensure at least one session exists
if (chatStore.sessions.length === 0) {
  chatStore.createSession()
}
</script>

<style scoped>
.session-list {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  font-weight: 600;
  color: #303133;
}

.session-items {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  padding: 12px 16px;
  cursor: pointer;
  border-radius: 6px;
  margin: 2px 8px;
  transition: background-color 0.2s;
}

.session-item:hover {
  background-color: #e4e7ed;
}

.session-item.active {
  background-color: #409eff;
  color: #fff;
}

.session-title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
```

- [ ] **Step 6: Create MessageList.vue**

Create `frontend/src/components/MessageList.vue`:

```vue
<template>
  <div class="message-list" ref="listRef">
    <div v-if="chatStore.currentMessages.length === 0" class="empty-state">
      <p>欢迎使用 GeoSmart 智能助手</p>
      <p class="hint">您可以询问国土空间规划相关政策、查询土地信息或业务办理进度</p>
    </div>
    <div
      v-for="msg in chatStore.currentMessages"
      :key="msg.id"
      :class="['message', msg.role]"
    >
      <div class="message-avatar">
        {{ msg.role === 'user' ? '我' : 'AI' }}
      </div>
      <div class="message-content">
        <div v-if="msg.role === 'assistant'" class="markdown-body" v-html="renderMarkdown(msg.content)"></div>
        <div v-else>{{ msg.content }}</div>
        <span v-if="msg.isStreaming" class="streaming-cursor">|</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'
import MarkdownIt from 'markdown-it'

const chatStore = useChatStore()
const listRef = ref<HTMLElement>()

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true
})

function renderMarkdown(content: string): string {
  return md.render(content)
}

function scrollToBottom() {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight
    }
  })
}

watch(() => chatStore.currentMessages.length, scrollToBottom)
watch(
  () => chatStore.currentMessages[chatStore.currentMessages.length - 1]?.content,
  scrollToBottom
)
</script>

<style scoped>
.message-list {
  height: 100%;
  overflow-y: auto;
}

.empty-state {
  text-align: center;
  padding-top: 120px;
  color: #909399;
}

.empty-state .hint {
  margin-top: 8px;
  font-size: 14px;
  color: #c0c4cc;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 80%;
}

.message.user {
  flex-direction: row-reverse;
  margin-left: auto;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background-color: #409eff;
  color: #fff;
}

.message.assistant .message-avatar {
  background-color: #f0f2f5;
  color: #606266;
}

.message-content {
  padding: 10px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.message.user .message-content {
  background-color: #409eff;
  color: #fff;
  border-top-right-radius: 4px;
}

.message.assistant .message-content {
  background-color: #fff;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-top-left-radius: 4px;
}

.streaming-cursor {
  animation: blink 1s infinite;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.markdown-body :deep(pre) {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}

.markdown-body :deep(code) {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}
</style>
```

- [ ] **Step 7: Create MessageInput.vue**

Create `frontend/src/components/MessageInput.vue`:

```vue
<template>
  <div class="message-input">
    <el-input
      v-model="inputText"
      type="textarea"
      :rows="2"
      placeholder="输入消息... (Enter 发送, Shift+Enter 换行)"
      resize="none"
      @keydown="handleKeydown"
      :disabled="chatStore.isLoading"
    />
    <el-button
      type="primary"
      @click="handleSend"
      :disabled="!inputText.trim() && !chatStore.isLoading"
    >
      {{ chatStore.isLoading ? '停止' : '发送' }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()
const inputText = ref('')

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function handleSend() {
  if (chatStore.isLoading) {
    chatStore.stopStreaming()
    return
  }

  const text = inputText.value.trim()
  if (!text) return

  chatStore.sendMessage(text)
  inputText.value = ''
}
</script>

<style scoped>
.message-input {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  margin-top: 8px;
}

.message-input .el-textarea {
  flex: 1;
}
</style>
```

- [ ] **Step 8: Create DocUpload.vue**

Create `frontend/src/components/DocUpload.vue`:

```vue
<template>
  <div class="doc-upload">
    <el-upload
      :auto-upload="false"
      :on-change="handleFileChange"
      :show-file-list="false"
      accept=".txt,.pdf,.docx,.doc"
    >
      <el-button size="small" type="success" plain>上传文档</el-button>
    </el-upload>
    <span v-if="uploading" class="upload-status">上传中...</span>
    <span v-if="uploadSuccess" class="upload-success">上传成功</span>
    <span v-if="uploadError" class="upload-error">{{ uploadError }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { uploadDocument } from '@/api/chat'
import type { UploadFile } from 'element-plus'

const uploading = ref(false)
const uploadSuccess = ref(false)
const uploadError = ref('')

async function handleFileChange(file: UploadFile) {
  if (!file.raw) return

  uploading.value = true
  uploadSuccess.value = false
  uploadError.value = ''

  try {
    await uploadDocument(file.raw)
    uploadSuccess.value = true
    setTimeout(() => (uploadSuccess.value = false), 3000)
  } catch (e: any) {
    uploadError.value = e.message || '上传失败'
    setTimeout(() => (uploadError.value = ''), 5000)
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.doc-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-status {
  color: #409eff;
  font-size: 13px;
}

.upload-success {
  color: #67c23a;
  font-size: 13px;
}

.upload-error {
  color: #f56c6c;
  font-size: 13px;
}
</style>
```

- [ ] **Step 9: Verify frontend builds**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/frontend && npm run build
```
Expected: Build succeeds

- [ ] **Step 10: Commit**

```bash
cd D:/Projects/GeoSmart-Agent
git add frontend/
git commit -m "feat: add complete chat UI with session management, streaming display, and document upload"
```

---

## Task 8: End-to-End Integration

**Files:**
- Create: `backend/src/main/java/com/geosmart/config/AppConfig.java`

This task verifies the full stack works together. Before starting, ensure:
1. `DEEPSEEK_API_KEY` (or `OPENAI_API_KEY`) is set as environment variable
2. Backend compiles: `cd backend && mvn clean package -DskipTests`
3. Frontend builds: `cd frontend && npm run build`

- [ ] **Step 1: Create AppConfig for sample data loading**

Create `backend/src/main/java/com/geosmart/config/AppConfig.java`:

```java
package com.geosmart.config;

import com.geosmart.rag.DocumentIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public CommandLineRunner loadSampleDocuments(DocumentIngestionService ingestionService) {
        return args -> {
            Path sampleDir = Path.of("src/main/resources/sample-docs");
            if (Files.exists(sampleDir)) {
                try (var stream = Files.list(sampleDir)) {
                    stream.filter(Files::isRegularFile)
                          .forEach(file -> {
                              try {
                                  ingestionService.ingestDocument(file);
                                  log.info("Loaded sample document: {}", file.getFileName());
                              } catch (Exception e) {
                                  log.warn("Failed to load sample document: {}", file.getFileName(), e);
                              }
                          });
                }
            }
        };
    }
}
```

- [ ] **Step 2: Build backend**

Run:
```bash
cd D:/Projects/GeoSmart-Agent/backend && mvn clean package -DskipTests
```
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Start backend**

Run (in a separate terminal):
```bash
cd D:/Projects/GeoSmart-Agent/backend
DEEPSEEK_API_KEY=your-actual-key mvn spring-boot:run
```
Expected: Application starts on port 8080, logs show sample documents loaded

- [ ] **Step 4: Start frontend dev server**

Run (in a separate terminal):
```bash
cd D:/Projects/GeoSmart-Agent/frontend && npm run dev
```
Expected: Dev server starts on port 5173

- [ ] **Step 5: Verify end-to-end flow**

Open `http://localhost:5173` in a browser and:
1. Verify the chat UI loads with empty session
2. Type a message like "国土空间规划管理办法的第三条是什么？" and send
3. Verify streaming response appears
4. Upload a document via the upload button
5. Ask a question about the uploaded document

- [ ] **Step 6: Commit**

```bash
cd D:/Projects/GeoSmart-Agent
git add backend/src/main/java/com/geosmart/config/
git commit -m "feat: add sample data auto-loading and complete MVP integration"
```
