# AGENTS.md

## Project Type
LangChain4j progressive tutorial - 10 independently runnable steps teaching AI app development.

## Critical Execution Patterns

### Steps 00-06 (Pure Java)
- **Run single step**: `cd steps/step-NN-name && mvn compile exec:java`
- **Verify all steps**: `cd steps && mvn compile`
- **Constraint**: Each step must run independently - never break this

### Step 07 (Spring Boot Multi-Module)
- **Required order**: `cd steps/step-07-spring-boot-api && mvn clean install` then `cd smartdoc-api && mvn spring-boot:run`
- **Do NOT skip parent build** - modules depend on it
- **5 modules**: smartdoc-api (main), smartdoc-chat, smartdoc-tools, smartdoc-rag, smartdoc-llm

### Step 08 (Vue Frontend)
- **Location**: `steps/step-08-vue-frontend/frontend`
- **Run**: `npm install && npm run dev`

### Final/ Complete Project
- **Backend**: `cd final/backend && mvn clean install -DskipTests`
- **Frontend**: `cd final/frontend && npm install && npm run build`

## Configuration
- **LLM API Key**: Set env var `LLM_API_KEY` (overrides application.yml)
- **Config file**: `src/main/resources/application.yml` in each step
- **Default provider**: 智谱 GLM, supports DeepSeek/OpenAI switching

## Code Conventions
- **Package**: `com.smartdoc`
- **Module names**: `smartdoc-*`
- **Cross-step comments**: `// 【Step 06 对照】...` format for educational mapping
- **Business domain**: Smart document assistant (Java dev team handbook as example)

## Workflow for Changes
1. Modify step code → ensure `mvn compile exec:java` still works
2. Update step README.md to reflect changes
3. Sync changes to `final/` if concept changes
4. Validate: `cd steps && mvn compile`

## Key Files
- `steps/pom.xml` - Parent POM (LangChain4j version management)
- `steps/step-03-rag-retrieval/src/main/resources/knowledge-base.txt` - Sample knowledge base
- `steps/step-06-full-aiservice/src/main/java/com/tutorial/FullAssistant.java` - Core integration point