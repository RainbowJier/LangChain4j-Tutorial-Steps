# Step 00: Environmental Setup

## Learning Target

- Configure the Java development environment (Java 17+ and Maven 3.8+)
- Obtain LLM API Key
- Verify the API connectivity.

## Prerequisites.

- Java 17 or a higher version (verified by `java -version`)
- Maven 3.8+（ verified by`mvn -version` ）
- Network access to the LLM API

## Running the Application

```bash
cd steps/step-00-setup
mvn compile exec:java
```

## Configure LLM API Key

Edit `src/main/resources/application.yml`, select a provider.

| Provider | Base URL         | Free quota | Registered Address             |
|----------|------------------|------------|--------------------------------|
| GLM      | open.bigmodel.cn | Yes        | https://open.bigmodel.cn/      |
| DeepSeek | api.deepseek.com | Yes        | https://platform.deepseek.com/ |

Here, I wanna recommend using Zhipu GLM (register and get free quota).

## What you'll see

**Success：**

```
=== LangChain4j Teaching environment inspection ===

Configured Provider: zhipu
API Key: ca185773...
Model: glm-4-flash
Base URL: https://open.bigmodel.cn/api/paas/v4/

Connecting to LLM API... Connection successful!
LLM responses: OK

✅ Environment insepection passed! you are now already to start your study.
```

**When failure：** It will display the common causes of errors and their solutions.

## Key Concepts

- **ChatModel**：The core interface for interacting with LLM in LangChain4j.
- **OpenAiChatModel**：OpenAI-compatible implementation, supportting providers like OpenAI, DeepSeek, and Zhipu GLM.
- **Base URL**：All providers offer OpenAI-compatible API interfaces, and you can switch by changing the base URL.

## Next Step

[Step 01: Hello LLM](../step-01-hello-llm/) — Send your first LLM message
