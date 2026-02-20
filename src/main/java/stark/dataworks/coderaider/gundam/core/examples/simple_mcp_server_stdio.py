"""
A simple MCP server for testing purposes.
Provides basic tools: add, echo, kb_search, and policy_lookup.

Requirements:
    pip install mcp[cli]

Usage:
    python simple_mcp_server_stdio.py

The server uses stdio transport for communication.
"""

import os
from mcp.server.fastmcp import FastMCP

mcp = FastMCP(
    "Simple Test MCP Server",
)


@mcp.tool()
def add(a: int, b: int) -> int:
    """Add two numbers together."""
    return a + b


@mcp.tool()
def echo(message: str) -> str:
    """Echo back the provided message."""
    return f"echo: {message}"


@mcp.tool()
def kb_search(query: str, limit: int = 5) -> str:
    """Search the knowledge base for relevant documents.
    
    Args:
        query: The search query string
        limit: Maximum number of results to return (default: 5)
    
    Returns:
        A formatted string with search results
    """
    mock_results = [
        {"title": "Onboarding Policy", "content": "New employees must complete orientation within 30 days."},
        {"title": "Remote Work Policy", "content": "Employees may work remotely up to 3 days per week."},
        {"title": "Vacation Policy", "content": "Employees accrue 15 days of vacation per year."},
        {"title": "Expense Policy", "content": "Expenses must be submitted within 30 days of purchase."},
        {"title": "Code Review Policy", "content": "All code changes require at least one approval."},
    ]
    
    results = []
    for i, doc in enumerate(mock_results[:limit]):
        if query.lower() in doc["title"].lower() or query.lower() in doc["content"].lower():
            results.append(f"{i+1}. {doc['title']}: {doc['content']}")
    
    if not results:
        return f"No results found for query: '{query}'"
    
    return f"Found {len(results)} result(s) for '{query}':\n" + "\n".join(results)


@mcp.tool()
def policy_lookup(topic: str) -> str:
    """Look up company policies by topic.
    
    Args:
        topic: The policy topic to look up
    
    Returns:
        The policy content for the given topic
    """
    policies = {
        "onboarding": "New employees must complete orientation within 30 days. Required documents: ID, bank info, emergency contact.",
        "remote_work": "Employees may work remotely up to 3 days per week. Must be available during core hours (10am-3pm).",
        "vacation": "Employees accrue 15 days of vacation per year. Maximum carryover: 5 days. Blackout periods apply.",
        "expense": "Expenses must be submitted within 30 days of purchase. Approval required for amounts over $100.",
        "code_review": "All code changes require at least one approval. Breaking changes require two approvals.",
    }
    
    topic_lower = topic.lower().replace(" ", "_").replace("-", "_")
    if topic_lower in policies:
        return f"Policy for '{topic}': {policies[topic_lower]}"
    
    return f"No policy found for topic: '{topic}'. Available topics: {', '.join(policies.keys())}"


if __name__ == "__main__":
    import sys
    print("Starting MCP server with stdio transport", file=sys.stderr, flush=True)
    mcp.run(transport="stdio")
