---
name: claude-api-java
description: "Claude API Java SDK skill. Use when: writing Java code that calls Claude API, using anthropic-java SDK, streaming messages, tool use, structured output, thinking, prompt caching, files API, MCP integration, managed agents in Java."
argument-hint: "What Claude API feature do you need in Java?"
---

# Claude API — Java

## Installation

Maven:
```xml
<dependency>
    <groupId>com.anthropic</groupId>
    <artifactId>anthropic-java</artifactId>
    <version>2.17.0</version>
</dependency>
```

Gradle:
```groovy
implementation("com.anthropic:anthropic-java:2.17.0")
```

## Client Initialization

```java
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;

// Default (reads ANTHROPIC_API_KEY from environment)
AnthropicClient client = AnthropicOkHttpClient.fromEnv();

// Explicit API key
AnthropicClient client = AnthropicOkHttpClient.builder()
    .apiKey("your-api-key")
    .build();
```

---

## Basic Message Request

```java
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.Model;

MessageCreateParams params = MessageCreateParams.builder()
    .model(Model.CLAUDE_OPUS_4_6)
    .maxTokens(16000L)
    .addUserMessage("What is the capital of France?")
    .build();

Message response = client.messages().create(params);
response.content().stream()
    .flatMap(block -> block.text().stream())
    .forEach(textBlock -> System.out.println(textBlock.text()));
```

---

## Streaming

```java
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.RawMessageStreamEvent;

MessageCreateParams params = MessageCreateParams.builder()
    .model(Model.CLAUDE_OPUS_4_6)
    .maxTokens(64000L)
    .addUserMessage("Write a haiku")
    .build();

try (StreamResponse<RawMessageStreamEvent> streamResponse = client.messages().createStreaming(params)) {
    streamResponse.stream()
        .flatMap(event -> event.contentBlockDelta().stream())
        .flatMap(deltaEvent -> deltaEvent.delta().text().stream())
        .forEach(textDelta -> System.out.print(textDelta.text()));
}
```

---

## Thinking

**Adaptive thinking is the recommended mode for Claude 4.6+ models.**

```java
import com.anthropic.models.messages.ThinkingConfigAdaptive;

MessageCreateParams params = MessageCreateParams.builder()
    .model(Model.CLAUDE_SONNET_4_6)
    .maxTokens(16000L)
    .thinking(ThinkingConfigAdaptive.builder().build())
    .addUserMessage("Solve this step by step: 27 * 453")
    .build();

for (ContentBlock block : client.messages().create(params).content()) {
    block.thinking().ifPresent(t -> System.out.println("[thinking] " + t.thinking()));
    block.text().ifPresent(t -> System.out.println(t.text()));
}
```

> **Deprecated:** `ThinkingConfigEnabled.builder().budgetTokens(N)` still works but is deprecated. Use adaptive thinking above.

---

## Tool Use (Beta — automatic loop)

```java
import com.anthropic.helpers.BetaToolRunner;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.function.Supplier;

@JsonClassDescription("Get the weather in a given location")
static class GetWeather implements Supplier<String> {
    @JsonPropertyDescription("The city and state, e.g. San Francisco, CA")
    public String location;

    @Override
    public String get() {
        return "The weather in " + location + " is sunny and 72°F";
    }
}

BetaToolRunner toolRunner = client.beta().messages().toolRunner(
    MessageCreateParams.builder()
        .model("claude-opus-4-7")
        .maxTokens(16000L)
        .addTool(GetWeather.class)
        .addUserMessage("What's the weather in San Francisco?")
        .build());

for (BetaMessage message : toolRunner) {
    System.out.println(message);
}
```

### Non-Beta Tool Declaration (manual JSON schema)

```java
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Tool;

Tool tool = Tool.builder()
    .name("get_weather")
    .description("Get the current weather in a given location")
    .inputSchema(Tool.InputSchema.builder()
        .properties(Tool.InputSchema.Properties.builder()
            .putAdditionalProperty("location", JsonValue.from(Map.of("type", "string")))
            .build())
        .required(List.of("location"))
        .build())
    .build();
```

### Tool Result Round-Trip

```java
List<ContentBlockParam> results = List.of(
    ContentBlockParam.ofToolResult(ToolResultBlockParam.builder()
        .toolUseId(toolUseBlock.id())
        .content(yourResultString)
        .build())
);

MessageParam toolResultMsg = MessageParam.builder()
    .role(MessageParam.Role.USER)
    .contentOfBlockParams(results)  // builder alias for Content.ofBlockParams(...)
    .build();
```

---

## Effort Parameter

Effort is nested inside `OutputConfig` — NO `.effort()` directly on `MessageCreateParams.Builder`.

```java
import com.anthropic.models.messages.OutputConfig;

.outputConfig(OutputConfig.builder()
    .effort(OutputConfig.Effort.HIGH)  // or LOW, MEDIUM, MAX
    .build())
```

---

## Prompt Caching

Use `.systemOfTextBlockParams(...)` — the plain `.system(String)` overload can't carry cache control.

```java
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.CacheControlEphemeral;

.systemOfTextBlockParams(List.of(
    TextBlockParam.builder()
        .text(longSystemPrompt)
        .cacheControl(CacheControlEphemeral.builder()
            .ttl(CacheControlEphemeral.Ttl.TTL_1H)  // also TTL_5M
            .build())
        .build()))
```

Verify hits via `response.usage().cacheCreationInputTokens()` / `response.usage().cacheReadInputTokens()`.

---

## Token Counting

```java
import com.anthropic.models.messages.MessageCountTokensParams;

long tokens = client.messages().countTokens(
    MessageCountTokensParams.builder()
        .model(Model.CLAUDE_SONNET_4_6)
        .addUserMessage("Hello")
        .build()
).inputTokens();
```

---

## Structured Output

Class-based overload auto-derives JSON schema from POJO:

```java
import com.anthropic.models.messages.StructuredMessageCreateParams;

record Book(String title, String author) {}
record BookList(List<Book> books) {}

StructuredMessageCreateParams<BookList> params = MessageCreateParams.builder()
    .model(Model.CLAUDE_SONNET_4_6)
    .maxTokens(16000L)
    .outputConfig(BookList.class)  // returns a typed builder
    .addUserMessage("List 3 classic novels")
    .build();

client.messages().create(params).content().stream()
    .flatMap(cb -> cb.text().stream())
    .forEach(typed -> {
        for (Book b : typed.text().books()) System.out.println(b.title());
    });
```

---

## PDF / Document Input

```java
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.ContentBlockParam;

DocumentBlockParam doc = DocumentBlockParam.builder()
    .base64Source(base64String)  // or .urlSource("https://...") or .textSource("...")
    .title("My Document")        // optional
    .build();

.addUserMessageOfBlockParams(List.of(
    ContentBlockParam.ofDocument(doc),
    ContentBlockParam.ofText(TextBlockParam.builder().text("Summarize this").build())))
```

---

## Server-Side Tools

```java
import com.anthropic.models.messages.WebSearchTool20260209;
import com.anthropic.models.messages.ToolBash20250124;
import com.anthropic.models.messages.ToolTextEditor20250728;
import com.anthropic.models.messages.CodeExecutionTool20260120;

.addTool(WebSearchTool20260209.builder()
    .maxUses(5L)
    .allowedDomains(List.of("example.com"))
    .build())
.addTool(ToolBash20250124.builder().build())
.addTool(ToolTextEditor20250728.builder().build())
.addTool(CodeExecutionTool20260120.builder().build())
```

### Beta Namespace (MCP, compaction)

Use `com.anthropic.models.beta.messages.*` — class names have `Beta` prefix:

```java
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.anthropic.models.beta.messages.BetaToolBash20250124;
import com.anthropic.models.beta.messages.BetaRequestMcpServerUrlDefinition;

MessageCreateParams params = MessageCreateParams.builder()
    .model(Model.CLAUDE_OPUS_4_6)
    .maxTokens(16000L)
    .addBeta("mcp-client-2025-11-20")
    .addTool(BetaToolBash20250124.builder().build())
    .addMcpServer(BetaRequestMcpServerUrlDefinition.builder()
        .name("my-server")
        .url("https://example.com/mcp")
        .build())
    .addUserMessage("...")
    .build();

client.beta().messages().create(params);
```

> `BetaTool*` types are NOT interchangeable with non-beta `Tool*` — pick one namespace per request.

---

## Files API (Beta)

```java
import com.anthropic.models.beta.files.FileUploadParams;
import com.anthropic.models.beta.messages.BetaRequestDocumentBlock;
import java.nio.file.Paths;

FileMetadata meta = client.beta().files().upload(
    FileUploadParams.builder()
        .file(Paths.get("/path/to/doc.pdf"))
        .build());

// Reference in a beta message:
BetaRequestDocumentBlock doc = BetaRequestDocumentBlock.builder()
    .fileSource(meta.id())
    .build();
```

Other methods: `.list()`, `.delete(fileId)`, `.download(fileId)`, `.retrieveMetadata(fileId)`.

---

## Managed Agents

See [managed-agents reference](./references/managed-agents.md) for full Managed Agents API (create agent, session, stream events, MCP, vaults, GitHub repo integration).
