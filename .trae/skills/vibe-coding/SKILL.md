---
name: vibe-coding
description: "Enforce coding standards and best practices for vibe coding in generic-agent-core. Invoke at the start of every vibe coding session to ensure consistency and quality."
---

# Vibe Coding Standards

This skill enforces coding standards and best practices for vibe coding in generic-agent-core. **MUST be invoked at the start of every vibe coding session.**

## Project Context

### Reference Documents
- **README.md**: Design goals and implementation approach for generic-agent-core
- **Folder "designs/"**: Architecture design documents and implementation guides
- **Folder "references/openai-agents-python-main/"**: Source code of OpenAI's Agents SDK - used to build generic-agent-core
- **Folder "references/trae-agent/"**: Source code of trae-agent - used to build excalibur in generic-agent-core

### Key Principles
1. **Declarative-first**: Agent/workflow definitions can be loaded from JSON (`AgentDefinition.fromJson`, `WorkflowDefinition.fromJson`)
2. **Provider-agnostic**: Model invocation goes through `ILlmClient` / `LlmClientRegistry`
3. **Runtime-centric**: Kernel owns turn orchestration, tool looping, guardrails, retries, and session/memory flow
4. **Strict separation of concerns**: No business-domain coupling in core modules
5. **Extensible by interfaces**: Hooks, guardrails, memory backends, tool approval policies, tracing providers

## Mandatory Workflow

### Step 1: Read Reference Materials
Before writing any code, ALWAYS:
1. Read `README.md` to understand design goals and implementation approach
2. Check `designs/` folder for relevant architectural documents
3. Review `references/openai-agents-python-main/` for patterns being migrated
4. Review `references/trae-agent/` for trae-agent integration patterns

### Step 2: Follow Existing Code Style
- **DO NOT assume libraries are available** - check package.json, pom.xml, or existing imports
- **Mimic code style** of existing components in the same module
- **Follow naming conventions** - use existing patterns for classes, methods, interfaces
- **Use existing utilities** - check for existing helpers before creating new ones
- **DO NOT add comments** unless explicitly requested

### Step 3: Scan Before Implementation
Before implementing any feature:
1. Search the entire codebase to ensure no similar implementation exists
2. Check both `src/main/java` and `src/test/java`
3. Look for existing interfaces that can be extended
4. Verify the approach aligns with project architecture

### Step 4: Test Your Code
After implementation:
1. **Compile** the code using Maven (project uses Maven as build tool)
2. **Run relevant tests** to verify functionality
3. **Fix any bugs** immediately - repeat the compile/test cycle until no bugs remain
4. **DO NOT assume code works** - always verify with tests

### Step 5: Maven Configuration
For Java compilation and testing:
- Maven is installed in: `D:\Software\IntelliJ IDEA 2025.3.3\plugins\maven\lib`
- Local repository: `D:\DinoStark\Projects\Maven\Repository`
- Settings file: `D:\DinoStark\Projects\Maven\Settings\settings.xml`

Run tests with:
```bash
mvn test -Dtest=<TestClassName> -s "D:\DinoStark\Projects\Maven\Settings\settings.xml"
```

## Coding Rules

### Package Structure
- Core modules in `src/main/java/stark/dataworks/coderaider/genericagent/core/`
- Follow existing package naming (runner, agent, tool, llmspi, context, memory, session, etc.)

### Interface-first Design
- Always define `I<Name>` interface before implementation
- Place interfaces in appropriate module packages
- Use existing extension points (IRunHooks, ITool, IGuardrail, etc.)

### Error Handling
- Use typed error classification from `runerror` package
- Follow retry policies defined in `policy` package
- Handle provider-specific exceptions gracefully

### Testing
- Place tests in `src/test/java` matching main source structure
- Follow naming: `<ClassName>Test` or `<Feature>Test`
- Use existing test utilities and mocking patterns
- Include both positive and negative test cases

## When to Invoke

**This skill MUST be invoked:**
- At the start of every vibe coding session
- Before implementing any new feature
- Before modifying existing core functionality
- When unsure about coding approach or conventions

**This skill provides:**
- Context on project architecture and design goals
- Reference to existing implementations for patterns
- Testing and compilation guidelines
- Code style enforcement
