---
name: architecture-analyzer
description: Analyze gundam-core project architecture, generate Mermaid diagrams, and provide codebase navigation guides for migration from OpenAI SDK.
---

# Gundam Core Architecture Analyzer

Use this skill to perform deep architectural analysis of the `gundam-core` project. This is particularly useful when migrating logic from the OpenAI Agents SDK or onboarding new developers to the codebase.

## Capability Scope

* **Architecture Mapping**: Scans `/src` and `/designs` to generate Mermaid diagrams.
* **Migration Insight**: Compares the reference implementation in `/references` (OpenAI SDK) with the current `/src` to identify gaps.
* **Documentation Sync**: Updates `GUNDAM-core-Architecture.md` with the latest structural findings.

## Operational Workflow

### 1. Structure Analysis
When analyzing the project, prioritize the following sequence:
1.  **Definitions**: Read `gundam-core/README.md` for high-level intent.
2.  **Design Intent**: Inspect `gundam-core/designs` for existing architectural blueprints.
3.  **Reference Baseline**: Analyze `gundam-core/references` to understand the source patterns being migrated.
4.  **Implementation**: Audit `gundam-core/src` to map actual vs. intended structure.
5.  **Write Phase**: Only update the target architecture document after the four analysis steps above are complete.

### 2. Diagram Generation
Generate Mermaid **code blocks** using `graph TD` (do **not** use `classDiagram`) that highlight:
* Core Agent loop logic.
* Tool integration layers.
* State management flow.

### 3. Output Requirements
Always save the final architecture documentation to:
`designs/GUNDAM-core-Architecture.md`

When updating the target document:
1. Keep the existing top-level heading/section structure unchanged.
2. Update only the content under each existing heading.
3. Do not replace sections with placeholders (for example: "...", "TBD", or "existing content").
4. Keep output in plain ASCII markdown.
5. Perform exactly one final `write_file` call with the complete document content.
6. In that final `write_file` call, set `preserve_existing_headings=true`.
7. Prefer also passing `expected_headings` captured from the current document to enforce exact heading order.
8. Avoid partial writes. If you intentionally need to shrink a file substantially, set `allow_truncate=true`.
9. Do not call `write_file` again after a successful write; the runtime treats writes as single-final-write per file.

## Usage Examples

> "Analyze the gundam-core project structure and update the architecture doc."
> "Compare our current src with the OpenAI SDK references and draw the architecture diagram."
