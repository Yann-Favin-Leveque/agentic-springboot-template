# Environment Configuration (agentic-helper v1.5.2)

Quick reference for configuring the Spring Boot AgentService Template.

---

## How It Works

The template uses **spring-dotenv** (v4.0.0) to auto-load `.env` files. Simply create a `.env` file in the project root.

---

## Required Variables

### LLM_INSTANCES

JSON array of API instances. **Must be on a single line** in `.env` files.

```bash
LLM_INSTANCES=[{"id":"openai-main","url":"https://api.openai.com","key":"sk-proj-YOUR_KEY","models":"gpt-4o,gpt-4o-mini,text-embedding-3-small,dall-e-3","provider":"openai","enabled":true}]
```

### Instance Fields

| Field | Required | Description |
|-------|----------|-------------|
| `id` | Yes | Unique identifier (e.g., "openai-main") |
| `url` | Yes | Base URL for API |
| `key` | Yes | API key |
| `models` | Yes | Comma-separated model names |
| `provider` | Yes | `openai`, `azure-openai`, or `azure-anthropic` |
| `apiVersion` | Azure only | API version (e.g., "2024-08-01-preview") |
| `enabled` | No | true/false (default: true) |

### Provider URLs

```bash
# OpenAI Direct
"url": "https://api.openai.com"

# Azure OpenAI
"url": "https://YOUR-RESOURCE.openai.azure.com"

# Azure Anthropic (Claude)
"url": "https://YOUR-RESOURCE.services.ai.azure.com"
```

---

## Optional Variables

### Rate Limiting & Retry

```bash
CONCURRENT_STREAM_LIMIT_PER_INSTANCE=15    # Concurrent stream limit per instance (default: 15)
LLM_MAX_RETRIES=3             # Retry attempts (default: 3)
LLM_DEFAULT_RESPONSE_TIMEOUT=120000  # Timeout in ms (default: 120000)
```

### Async Thread Pool

```bash
ASYNC_POOL_SIZE=30               # Thread pool size (default: 30)
```

### Provider Filter

```bash
# Only load specific providers (optional)
ENABLED_PROVIDERS=openai,azure-openai,azure-anthropic
```

### Logging

```bash
# AgentService library logging
LOGGING_LEVEL_IO_GITHUB_YANNFAVINLEVEQUE_AGENTIC=DEBUG  # INFO for production

# HTTP client logging (very verbose at DEBUG)
LOGGING_LEVEL_IO_GITHUB_SASHIRESTELA_CLEVERCLIENT=ERROR

# Spring framework
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=INFO
```

---

## Configuration Examples

### Development (OpenAI only)

```bash
LLM_INSTANCES=[{"id":"dev","url":"https://api.openai.com","key":"sk-proj-XXX","models":"gpt-4o-mini","provider":"openai","enabled":true}]
CONCURRENT_STREAM_LIMIT_PER_INSTANCE=5
LOGGING_LEVEL_IO_GITHUB_YANNFAVINLEVEQUE_AGENTIC=DEBUG
```

### Production (Multi-provider)

```bash
LLM_INSTANCES=[{"id":"openai-1","url":"https://api.openai.com","key":"sk-XXX","models":"gpt-4o,text-embedding-3-small,dall-e-3","provider":"openai","enabled":true},{"id":"azure-gpt","url":"https://myresource.openai.azure.com","key":"XXX","models":"gpt-4o","provider":"azure-openai","apiVersion":"2024-08-01-preview","enabled":true},{"id":"claude","url":"https://myresource.services.ai.azure.com","key":"XXX","models":"claude-sonnet-4-5","provider":"azure-anthropic","apiVersion":"2023-06-01","enabled":true}]
CONCURRENT_STREAM_LIMIT_PER_INSTANCE=50
LLM_MAX_RETRIES=5
LOGGING_LEVEL_IO_GITHUB_YANNFAVINLEVEQUE_AGENTIC=INFO
```

### Claude Only

```bash
LLM_INSTANCES=[{"id":"claude","url":"https://YOUR.services.ai.azure.com","key":"XXX","models":"claude-sonnet-4-5,claude-haiku-4-5","provider":"azure-anthropic","apiVersion":"2023-06-01","enabled":true}]
ENABLED_PROVIDERS=azure-anthropic
```

---

## Troubleshooting

| Error | Cause | Solution |
|-------|-------|----------|
| No instances configured | Empty `LLM_INSTANCES` | Add at least one instance to `.env` |
| No instance found with model X | Model not in any instance's `models` | Add model to instance config |
| Failed to parse instancesJson | Invalid JSON | Validate JSON, ensure single line in `.env` |
| Claude: connection failed | Wrong URL format | Use `https://RESOURCE.services.ai.azure.com` |
| Azure OpenAI: 404 | Missing apiVersion | Add `"apiVersion": "2024-08-01-preview"` |

---

**Last Updated**: February 2026
