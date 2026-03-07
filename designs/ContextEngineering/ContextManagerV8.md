# ContextManager V8 Design (for GUNDAM-context)

**Version**: v8.0 (design-only)
**Status**: Draft for implementation
**Scope**: Architecture and behavior contract for a standalone `GUNDAM-context` repository built on `GUNDAM-core`

---

## 1. Purpose

ContextManager V8 defines a production-ready context service for one conversation thread at a time, with a single core API:

```java
public List<ContextItem> getContext(String chatThreadId);
```

V8 keeps the key strengths from previous versions and aligns them to the current `GUNDAM-core` runtime model:

1. **Long-term memory** via dual track: summary + recent turns.
2. **Low latency** via deterministic prompt prefix layout for provider prefix-cache reuse.
3. **Consistency** via real-time assembly from latest thread state.
4. **Multimodal support** via parsed/indexed files and images retrievable in later turns.

---

## 2. Goals and Non-Goals

### 2.1 Goals

- Provide a **clear contract** for context assembly per `chatThreadId`.
- Be directly implementable on top of existing `GUNDAM-core` abstractions (`ContextItem`, memory/session concepts, runner lifecycle).
- Keep behavior deterministic, observable, and testable.
- Support incremental evolution (POC → production) without breaking API.

### 2.2 Non-Goals

- Not a full implementation guide.
- Not a model-training or fine-tuning proposal.
- Not a UI, business workflow, or tenant-permission design.

---

## 3. Terminology (Normative)

- **Chat Thread**: A unique conversation timeline identified by `chatThreadId`.
- **Context Item**: A normalized unit returned by `getContext`, represented by `ContextItem` in `GUNDAM-core`.
- **Deterministic Layout**: Stable section ordering in prompt/context assembly so static prefix bytes are repeatable.
- **Prefix Cache**: LLM-provider-side cache that reuses prefill computation when prompt prefix matches.
- **Recent Window**: Most recent N turns retained in raw form.
- **Summary Segment**: Condensed text generated from raw historical turns for one fixed segment range.
- **Summary-of-Summary (SoS)**: Re-summarizing prior summaries; V8 avoids this by segmenting from raw turns.
- **Long-Term Memory**: Historical knowledge exposed through summaries + retrieval index.
- **State Overlay**: Latest canonical state snapshot assembled from persisted events/metadata before each call.
- **Multimodal Artifact**: Uploaded file/image/audio/video plus extracted text/description metadata.

---

## 4. Core Design Principles

1. **Deterministic before intelligent**: stable structure first, adaptive retrieval second.
2. **Raw history is source of truth**: summaries and indexes are derived artifacts.
3. **Real-time assembly**: every call rebuilds from current persisted state.
4. **Segment-level compaction**: trim summary segments, do not recursively summarize summaries.
5. **Graceful degradation**: context can be reduced by priority when token or dependency limits are hit.

---

## 5. Logical Architecture

`GUNDAM-context` is a standalone service that uses `GUNDAM-core` primitives.

### 5.1 Major Components

1. **Thread Store Adapter**
   - Reads raw turns and metadata by `chatThreadId`.
2. **Summary Manager**
   - Maintains segment summaries generated from raw turns.
3. **Retrieval Manager**
   - Retrieves relevant long-term snippets and multimodal descriptions.
4. **State Overlay Manager**
   - Produces latest canonical state (tool results, user profile, session flags).
5. **Token Budget Manager**
   - Enforces model budget and priority-based trimming.
6. **Layout Assembler**
   - Produces deterministic ordered `ContextItem` list.

### 5.2 Deterministic Layout (V8)

Recommended fixed order:

1. **L1 Static Instruction** (system policies, agent identity)
2. **L2 Stable Session Facts** (user profile, persistent preferences)
3. **L3 Summary Segments** (long-term memory)
4. **L4 Anchors + Recent Window** (first-turn anchor + last N turns)
5. **L5 Dynamic Retrieval** (RAG hits, multimodal artifact snippets)
6. **L6 Volatile Runtime State** (latest tool/runtime overlay)

Rationale: L1–L4 are relatively stable and should maximize prefix-cache hit rate; L5–L6 are expected to change frequently and are placed later.

---

## 6. API Contract

```java
public List<ContextItem> getContext(String chatThreadId);
```

### 6.1 Input

- `chatThreadId`: required, immutable conversation identifier.

### 6.2 Output

Ordered `List<ContextItem>` where each item includes:
- `type`: semantic category (`ContextItemType`)
- `content`: prompt-ready content
- `metadata`: machine-readable attributes (priority, source, timestamp, artifact id)

### 6.3 Behavioral Contract

- Order is deterministic for same state snapshot.
- Empty thread still returns minimal valid context (system + user input placeholders if applicable).
- Context generation is side-effect free; async tasks (e.g., summary refresh) are decoupled.

---

## 7. Data and Consistency Model

1. **Raw Turns**: append-only canonical history.
2. **Derived Summaries**: versioned by segment and source-turn range.
3. **Retrieval Index**: references raw turns and multimodal artifacts.
4. **Overlay State**: latest merged state snapshot used at read time.

Consistency target:
- **Read-your-latest-write at thread level** for normal operation.
- If async derivative data is stale (e.g., summary lag), system falls back to raw/recent window and marks metadata for observability.

---

## 8. Token Budget and Trimming Policy

Priority order for retention:

1. L1 static instruction (must keep)
2. L4 recent window + first-turn anchor
3. L3 summary segments (trim by whole segment)
4. L2 stable session facts (selective)
5. L5 retrieval chunks
6. L6 volatile runtime detail

Key rule: **Never replace raw recent turns with recursively compressed summary-of-summary artifacts.**

---

## 9. Multimodal Support Model

For uploaded artifacts, V8 stores:
- normalized artifact metadata,
- parsed text or generated description,
- retrievable references for later rounds.

At retrieval time, only relevant artifact snippets are injected into L5. This keeps multimodal memory available without bloating every request.

---

## 10. Failure and Degradation Strategy

- If retrieval backend fails: return L1–L4/L6, skip L5 with warning metadata.
- If summary backend fails: keep recent window and anchor; summary omitted.
- If token estimate exceeds limit: apply priority trimming policy in Section 8.
- If thread not found: return typed error or empty-context policy (decide at service API boundary, not in assembler).

---

## 11. POC Plan (Recommended)

### 11.1 POC-1 (Core behavior)

- Implement `getContext(chatThreadId)` with L1 + L3 + L4 only.
- Segment summaries from raw turns (no SoS).
- Deterministic ordering and token trimming.

### 11.2 POC-2 (Latency and consistency)

- Add L2/L6 overlay and request-level observability.
- Measure TTFT impact with and without deterministic layout.

### 11.3 POC-3 (Multimodal retrieval)

- Add L5 with artifact descriptions and retrieval ranking.
- Validate that multimodal context is available only when relevant.

---

## 12. Test Strategy

### 12.1 Functional Tests

- Deterministic order test for same snapshot.
- Window policy test (first-turn + recent N).
- Summary segment test (segment trim, no SoS).
- Degradation test under retrieval/summary failure.

### 12.2 Dataset-driven Evaluation

Use the following benchmarks as external evaluation suites:

1. **LongMemEval** (academic): long-range memory retention and factual continuity.
2. **MultiChallenge** (industry): robustness under multi-turn, multi-condition workflows.
3. **LoCoMo** (ACL 2024): long conversation memory and consistency stress tests.

For each dataset, compare:
- baseline (recent-window only),
- V8 dual-track (summary + recent),
- V8 dual-track + retrieval.

Primary metrics:
- answer correctness / task success,
- long-context recall,
- TTFT and end-to-end latency,
- token usage per request.

---

## 13. Mapping to Current GUNDAM-core

V8 is compatible with existing `GUNDAM-core` contracts:

- `ContextItem` as context transport object.
- `DefaultContextBuilder` pattern as baseline assembly reference.
- memory/session abstractions as storage integration points.

`GUNDAM-context` should remain an external service/repo and expose context through stable API, while `GUNDAM-core` continues to own runtime execution.

---

## 14. Open Decisions

1. Whether `getContext` should return only context items or include diagnostics payload.
2. Whether `chatThreadId` versioning is needed for strict snapshot replay.
3. Whether multimodal parsing is synchronous on first upload or always asynchronous.

---

## 15. Summary

ContextManager V8 preserves the strongest ideas from earlier versions—deterministic layout, dual-track memory, token-aware trimming, and multimodal retrieval—while reducing ambiguity with explicit term definitions and a clear API-centered architecture suitable for implementation in `GUNDAM-context` on top of `GUNDAM-core`.
