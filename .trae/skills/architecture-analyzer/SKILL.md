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

### 2. Diagram Generation
Generate a Mermaid `classDiagram` or `graph TD` that highlights:
* Core Agent loop logic.
* Tool integration layers.
* State management flow.

### 3. Output Requirements
Always save the final architecture documentation to:
`d:\DinoStark\Projects\CodeSpaces\coderaider\GUNDAM\gundam-core\designs\GUNDAM-core-Architecture.md`

## Usage Examples

> "Analyze the gundam-core project structure and update the architecture doc."
> "Compare our current src with the OpenAI SDK references and draw the architecture diagram."